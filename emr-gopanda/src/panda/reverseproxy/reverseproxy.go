// Copyright 2011 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// HTTP reverse proxy handler
package reverseproxy

import (
	"fmt"
	"io"
	"log"
	"net/http"
	"net/url"
	"strconv"
	"strings"
	"sync"
	"time"
	"io/ioutil"
)

// onExitFlushLoop is a callback set by tests to detect the state of the
// flushLoop() goroutine.
var onExitFlushLoop func()

type ProxyInfo struct {
	TargetHost string
	TargetPort int
	ProxyHost  string
	ProxyPort  int
}

// ReverseProxy is an HTTP Handler that takes an incoming request and
// sends it to another server, proxying the response back to the
// client.
type ReverseProxy struct {
	// Director must be a function which modifies
	// the request into a new request to be sent
	// using Transport. Its response is then copied
	// back to the original client unmodified.
	Director      func(*http.Request)

	// The transport used to perform proxy requests.
	// If nil, http.DefaultTransport is used.
	Transport     http.RoundTripper

	// FlushInterval specifies the flush interval
	// to flush to the client while copying the
	// response body.
	// If zero, no periodic flushing is done.
	FlushInterval time.Duration

	// ErrorLog specifies an optional logger for errors
	// that occur when attempting to proxy the request.
	// If nil, logging goes to os.Stderr via the log package's
	// standard logger.
	ErrorLog      *log.Logger

	// BufferPool optionally specifies a buffer pool to
	// get byte slices for use by io.CopyBuffer when
	// copying HTTP response bodies.
	BufferPool    BufferPool
	proxyInfo     *ProxyInfo
}

// A BufferPool is an interface for getting and returning temporary
// byte slices for use by io.CopyBuffer.
type BufferPool interface {
	Get() []byte
	Put([]byte)
}

func singleJoiningSlash(a, b string) string {
	aslash := strings.HasSuffix(a, "/")
	bslash := strings.HasPrefix(b, "/")
	switch {
	case aslash && bslash:
		return a + b[1:]
	case !aslash && !bslash:
		return a + "/" + b
	}
	return a + b
}

// NewSingleHostReverseProxy returns a new ReverseProxy that routes
// URLs to the scheme, host, and base path provided in target. If the
// target's path is "/base" and the incoming request was for "/dir",
// the target request will be for /base/dir.
// NewSingleHostReverseProxy does not rewrite the Host header.
// To rewrite Host headers, use ReverseProxy directly with a custom
// Director policy.
func NewSingleHostReverseProxy(target *url.URL, proxyInfo *ProxyInfo) *ReverseProxy {
	targetQuery := target.RawQuery
	director := func(req *http.Request) {
		req.URL.Scheme = target.Scheme
		req.URL.Host = target.Host
		req.URL.Path = singleJoiningSlash(target.Path, req.URL.Path)
		if targetQuery == "" || req.URL.RawQuery == "" {
			req.URL.RawQuery = targetQuery + req.URL.RawQuery
		} else {
			req.URL.RawQuery = targetQuery + "&" + req.URL.RawQuery
		}
		if _, ok := req.Header["User-Agent"]; !ok {
			// explicitly disable User-Agent so it's not set to default value
			req.Header.Set("User-Agent", "")
		}
		req.Host = target.Host
		req.Header.Set("Host", target.Host)
		//logging.Debug(fmt.Sprintf("new Host: %s", req.Header.Get("Host")))
	}
	return &ReverseProxy{Director: director, proxyInfo:proxyInfo, }
}

func copyHeader(dst, src http.Header) {
	for k, vv := range src {
		for _, v := range vv {
			if k=="Content-Length"{
				continue
			}
			dst.Add(k, v)
		}
	}
}

type requestCanceler interface {
	CancelRequest(*http.Request)
}

type runOnFirstRead struct {
	io.Reader // optional; nil means empty body

	fn func() // Run before first Read, then set to nil
}

func (c *runOnFirstRead) Read(bs []byte) (int, error) {
	if c.fn != nil {
		c.fn()
		c.fn = nil
	}
	if c.Reader == nil {
		return 0, io.EOF
	}
	return c.Reader.Read(bs)
}

func (p *ReverseProxy) ServeHTTP(rw http.ResponseWriter, req *http.Request) {
	transport := p.Transport
	if transport == nil {
		transport = http.DefaultTransport
	}

	outreq := new(http.Request)
	*outreq = *req // includes shallow copies of maps, but okay

	if closeNotifier, ok := rw.(http.CloseNotifier); ok {
		if requestCanceler, ok := transport.(requestCanceler); ok {
			reqDone := make(chan struct{})
			defer close(reqDone)
			clientGone := closeNotifier.CloseNotify()
			outreq.Body = struct {
				io.Reader
				io.Closer
			}{
				Reader: &runOnFirstRead{
					Reader: outreq.Body,
					fn: func() {
						go func() {
							select {
							case <-clientGone:
								requestCanceler.CancelRequest(outreq)
							case <-reqDone:
							}
						}()
					},
				},
				Closer: outreq.Body,
			}
		}
	}
	p.Director(outreq)
	outreq.Proto = req.Proto
	outreq.ProtoMajor = req.ProtoMajor
	outreq.ProtoMinor = req.ProtoMinor
	outreq.Close = req.Close
	outreq.URL = req.URL

	outreq.Header = make(http.Header)
	copyHeader(outreq.Header, req.Header)
	res, err := transport.RoundTrip(outreq)
	if err != nil {
		p.logf("http: proxy error: %v", err)
		rw.WriteHeader(http.StatusBadGateway)
		return
	}
	data, err := ioutil.ReadAll(res.Body)
	defer res.Body.Close()
	retdata := string(data)
	retdata = strings.Replace(retdata, fmt.Sprintf("%s:%d", p.proxyInfo.TargetHost, p.proxyInfo.TargetPort), fmt.Sprintf("%s:%d", "127.0.0.1", p.proxyInfo.ProxyPort), -1)
	copyHeader(rw.Header(), res.Header)
	rw.WriteHeader(res.StatusCode)
	if len(res.Trailer) > 0 {
		if fl, ok := rw.(http.Flusher); ok {
			fl.Flush()
		}
	}
	dataary := []byte(retdata)
	rw.Header().Set("Content-Length", fmt.Sprintf("%d", len(retdata)))
	rw.Write(dataary)
}

func (p *ReverseProxy) CopyResponse(dst io.Writer, src io.Reader, head http.Header) int {
	if p.FlushInterval != 0 {
		if wf, ok := dst.(writeFlusher); ok {
			mlw := &maxLatencyWriter{
				dst:     wf,
				latency: p.FlushInterval,
				done:    make(chan bool),
			}
			go mlw.flushLoop()
			defer mlw.stop()
			dst = mlw
		}
	}

	var srcreader = src
	contentlen, _ := strconv.Atoi(head.Get("Content-Length"))
	var buf []byte
	if p.BufferPool != nil {
		buf = p.BufferPool.Get()
	}
	io.CopyBuffer(dst, srcreader, buf)
	if p.BufferPool != nil {
		p.BufferPool.Put(buf)
	}
	return contentlen
}

func (p *ReverseProxy) logf(format string, args ...interface{}) {
	if p.ErrorLog != nil {
		p.ErrorLog.Printf(format, args...)
	} else {
		log.Printf(format, args...)
	}
}

type writeFlusher interface {
	io.Writer
	http.Flusher
}

type maxLatencyWriter struct {
	dst     writeFlusher
	latency time.Duration

	mu      sync.Mutex // protects Write + Flush
	done    chan bool
}

func (m *maxLatencyWriter) Write(p []byte) (int, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	return m.dst.Write(p)
}

func (m *maxLatencyWriter) flushLoop() {
	t := time.NewTicker(m.latency)
	defer t.Stop()
	for {
		select {
		case <-m.done:
			if onExitFlushLoop != nil {
				onExitFlushLoop()
			}
			return
		case <-t.C:
			m.mu.Lock()
			m.dst.Flush()
			m.mu.Unlock()
		}
	}
}

func (m *maxLatencyWriter) stop() {
	m.done <- true
}
