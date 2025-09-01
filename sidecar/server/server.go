package server

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"sidecar/config"
)

func StartHTTPServer(cfg config.Config) {
	http.HandleFunc("/health", healthHandler)
	http.HandleFunc("/last-connection", func(w http.ResponseWriter, r *http.Request) {
		lastConnectionHandler(w, r, cfg)
	})

	addr := ":" + cfg.HttpPort

	log.Printf("Starting HTTP server on %s", addr)
	if err := http.ListenAndServe(addr, nil); err != nil {
		log.Fatal("HTTP server error:", err)
	}
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintln(w, "OK")
}

func lastConnectionHandler(w http.ResponseWriter, r *http.Request, cfg config.Config) {
	data, err := os.ReadFile(cfg.FilePath)
	if err != nil {
		http.Error(w, "No connections", http.StatusNotFound)
		return
	}
	w.Write(data)
}
