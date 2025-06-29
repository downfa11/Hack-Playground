package main

import (
	"sidecar/capture"
	"sidecar/config"
	"sidecar/server"
)

func main() {
	config.LoadConfig()

	go server.StartHTTPServer()
	go capture.StartPacketCapture()

	select {}
}
