package main

import (
	"detache/config"
	"detache/proxy"
	"detache/wsclient"
)

func main() {
	cfg := config.LoadConfig()

	wsclient.StartWebSocketClient(cfg.WsServer, cfg.ProblemID, cfg.UserID)
	p := proxy.NewProxy(cfg.HttpUrl, cfg.HttpPort)
	proxy.StartHTTPServer(cfg.HttpPort, p)

	select {}
}
