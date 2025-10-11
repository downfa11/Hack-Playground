package main

import (
	"log"
	"sidecar/capture"
	"sidecar/config"
	"sidecar/ebpf"

	"strconv"
)

func main() {
	cfg := config.LoadConfig()
	targetPortInt, err := strconv.Atoi(cfg.TargetPort)
	if err != nil {
		log.Fatalf("Fatal Error: TARGET_PORT is invalid or missing. Value: %s, Error: %v", cfg.TargetPort, err)
	}

	log.Println("========================================")
	log.Println("🚀 Starting Attache Sidecar services...")
	log.Printf("📍 Target Port: %d", targetPortInt)
	log.Printf("📝 Output File: %s", cfg.FilePath)
	log.Println("========================================")

	go capture.StartPacketCapture(cfg, targetPortInt)
	log.Println("  [✅] Unified monitoring started.")

	go ebpf.StartEBPFMonitor(cfg)
	log.Println("  [✅] eBPF Monitor started. (Requires high privileges)")

	select {}
}
