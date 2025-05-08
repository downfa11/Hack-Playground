package server

import (
	"fmt"
	"log"
	"net/http"
	"os"
	"sidecar/config"
)

func StartHTTPServer() {
	http.HandleFunc("/health", healthHandler)
	http.HandleFunc("/last-connection", lastConnectionHandler)

	log.Printf("Starting HTTP server on %s", config.HttpPort)
	if err := http.ListenAndServe(config.HttpPort, nil); err != nil {
		log.Fatal("HTTP server error:", err)
	}
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintln(w, "OK")
}

func lastConnectionHandler(w http.ResponseWriter, r *http.Request) {
	data, err := os.ReadFile(config.FilePath)
	if err != nil {
		http.Error(w, "No connections recorded", http.StatusNotFound)
		return
	}
	w.Write(data)
}
