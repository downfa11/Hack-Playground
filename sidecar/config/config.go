package config

import (
	"log"
	"os"
)

type Config struct {
	ProblemID  string
	UserID     string
	FilePath   string
	HttpPort   string
	TargetPort string
}

func LoadConfig() Config {
	cfg := Config{
		ProblemID:  os.Getenv("PROBLEM_ID"),
		UserID:     os.Getenv("USER_ID"),
		FilePath:   os.Getenv("FILE_PATH"),
		HttpPort:   os.Getenv("PORT"),
		TargetPort: os.Getenv("TARGET_PORT"),
	}

	if cfg.FilePath == "" {
		cfg.FilePath = "/tmp/last_connections.json"
	}

	if cfg.HttpPort == "" {
		cfg.HttpPort = "1880"
	}

	log.Printf("Loaded config: %+v", cfg)
	return cfg
}
