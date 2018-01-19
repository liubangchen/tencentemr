package reverseproxy

import (
	"net/http"
	"fmt"
	"time"
	"net/url"
	logger "github.com/shengkehua/xlog4go"
	"encoding/base64"
	"strings"
	"panda/conf"
	"encoding/hex"
	"crypto/sha256"
	"crypto/hmac"
)

var reverseproxy *ReverseProxy

const auth_file = "auth.properties"
const conf_file = "prestoproxy.properties"

func init() {
	host := configure.GetConfStringValueWithDefault(conf_file, "presto.host", "0.0.0.0")
	port := configure.GetConfIntValue(conf_file, "presto.port", 9000)
	backendURL, err := url.Parse(fmt.Sprintf("http://%s:%d", host, port))
	if err != nil {
		logger.Error("init proxy server err:", err)
		return
	}
	phost := configure.GetConfStringValueWithDefault(conf_file, "proxy.host", "0.0.0.0")
	pport := configure.GetConfIntValue(conf_file, "proxy.port", 3000)

	reverseproxy = NewSingleHostReverseProxy(backendURL, &ProxyInfo{
		TargetHost :host,
		TargetPort :port,
		ProxyHost :phost,
		ProxyPort :pport,
	})
}

type HttpProxy struct {
	proxyServer *http.Server
}

type ProxyProcessHandler struct {

}

func (this *ProxyProcessHandler) digistPwd(pwd string) string {
	hash := hmac.New(sha256.New, []byte("emr-presto"))
	hash.Write([]byte(pwd))
	return strings.ToLower(hex.EncodeToString(hash.Sum(nil)))
}

func (this *ProxyProcessHandler) checkAuth(w http.ResponseWriter, req *http.Request) bool {
	auth := req.Header.Get("Authorization")
	if auth == "" {
		return false
	}
	headAuth, err := base64.StdEncoding.DecodeString(strings.Split(req.Header.Get("Authorization"), " ")[1])
	if err != nil {
		logger.Error("check auth DecodeString err:", err)
		return false
	}
	authinfo := string(headAuth)
	authSlices := strings.Split(authinfo, ":")
	if len(authSlices) != 2 {
		logger.Error(fmt.Sprintf("invailed head auth![ %s ]", headAuth))
		return false
	}
	username := authSlices[0]
	pwd := authSlices[1]
	targetpwd := configure.GetConfStringValueWithDefault(auth_file, username, "")
	if pwd == this.digistPwd(targetpwd) {
		//logger.Info("authon Succ for user:%s", username)
		req.Header.Del("Authorization")
		return true
	} else {
		logger.Warn("error password for user:%s,received password:%s and target is:%s", username, pwd, this.digistPwd(targetpwd))
	}
	return false
}

func (this *ProxyProcessHandler) ServeHTTP(w http.ResponseWriter, req *http.Request) {
	defer func() {
		if e := recover(); e != nil {
			logger.Error("proxy panic:%+v", e)
			w.WriteHeader(http.StatusInternalServerError)
		}
	}()
	if !this.checkAuth(w, req) {
		w.Header().Set("WWW-Authenticate", "Basic realm=\"Secure Area\"")
		w.Header().Set("Auth-Message", "Password error")
		w.WriteHeader(http.StatusUnauthorized)
		return
	}
	if reverseproxy == nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	reverseproxy.ServeHTTP(w, req)
}

func (this *HttpProxy) Create(host string, port int) (*HttpProxy) {
	this.proxyServer = &http.Server{
		Addr:           fmt.Sprintf("%s:%d", host, port),
		Handler:        &ProxyProcessHandler{},
		ReadTimeout:    24 * time.Hour,
		WriteTimeout:   24 * time.Hour,
		MaxHeaderBytes: 1 << 20,
	}
	return this
}

func (this *HttpProxy)  Start() error {
	return this.proxyServer.ListenAndServe()
}