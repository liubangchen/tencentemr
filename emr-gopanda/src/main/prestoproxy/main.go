package main

import (
	"panda/conf"
	logger "github.com/shengkehua/xlog4go"
	"os"
	"os/signal"
	"syscall"
	"panda/reverseproxy"
	"fmt"
	"panda/util"
)

const conf_file = "prestoproxy.properties"

func init() {

}

func main() {
	if err := logger.SetupLogWithConf(fmt.Sprintf("%s/etc/log.json", util.GetRootDir())); err != nil {
		panic(err)
	}
	defer logger.Close()
	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)
	signal.Notify(c, syscall.SIGTERM)
	signal.Notify(c, syscall.SIGPIPE)
	go func() {
		s := <-c
		if s == syscall.SIGPIPE {
			logger.Warn("process accept sig pip,....")
			return
		}
		logger.Error("ctrl-c or SIGTERM found, exit.....")
		os.Exit(0)
	}()
	proxy := &reverseproxy.HttpProxy{}
	host := configure.GetConfStringValueWithDefault(conf_file, "proxy.host", "0.0.0.0")
	port := configure.GetConfIntValue(conf_file, "proxy.port", 3000)
	err := proxy.Create(host, port).Start()
	if err != nil {
		logger.Error("create http proxy server err:", err)
	}
}
