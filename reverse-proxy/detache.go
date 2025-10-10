package main

import (
	"detache/config"
	"detache/proxy"
)

func main() {
	cfg := config.LoadConfig()
	proxy.StartHTTPServer(cfg.NodePort, cfg.HttpUrl, cfg.HttpPort)

	select {}
}
