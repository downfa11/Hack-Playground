package main

import (
	"sidecar/capture"
	"sidecar/config"
)

func main() {
	cfg := config.LoadConfig()
	go capture.StartPacketCapture(cfg)
	// go server.StartHTTPServer(cfg) k8s 1.11+ Container 단위의 SecurityContext sysctls 미지원

	select {}
}
