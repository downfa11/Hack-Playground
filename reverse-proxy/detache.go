package main

import (
	"detache/config"
	"detache/proxy"
	"detache/wsclient"
)

func main() {
	cfg := config.LoadConfig()

	wsclient.StartWebSocketClient(cfg.WsServer, cfg.ProblemID, cfg.UserID)
	p := proxy.NewProxy("localhost", cfg.HttpPort)
	proxy.StartHTTPServer(cfg.HttpPort, p)

	select {}
}
