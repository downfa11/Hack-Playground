package config

import (
	"log"
	"os"
)

type Config struct {
	ProblemID string
	UserID    string
	HttpPort  string
	WsServer  string
}

func LoadConfig() *Config {
	cfg := &Config{
		ProblemID: os.Getenv("PROBLEM_ID"),
		UserID:    os.Getenv("USER_ID"),
		HttpPort:  os.Getenv("HTTP_PORT"),
		WsServer:  os.Getenv("WS_SERVER_URL"),
	}

	if cfg.ProblemID == "" || cfg.UserID == "" || cfg.HttpPort == "" || cfg.WsServer == "" {
		log.Fatal("environment not set")
	}

	log.Printf("Loaded config: %+v", cfg)
	return cfg
}
