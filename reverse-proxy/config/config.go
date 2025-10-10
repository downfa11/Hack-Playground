package config

import (
	"log"
	"os"
)

type Config struct {
	ProblemID   string
	UserID      string
	ProblemKind string // "WEBHACKING", etc..
	HttpUrl     string
	HttpPort    string // container port:80
	NodePort    string // listening port:18889)
}

func LoadConfig() *Config {
	cfg := &Config{
		ProblemID:   os.Getenv("PROBLEM_ID"),
		UserID:      os.Getenv("USER_ID"),
		ProblemKind: os.Getenv("PROBLEM_KIND"),
		HttpUrl:     os.Getenv("HTTP_URL"),
		HttpPort:    os.Getenv("HTTP_PORT"),
		NodePort:    os.Getenv("NODE_PORT"),
	}

	log.Printf("PROBLEM_ID=%q, USER_ID=%q, PROBLEM_KIND=%q, NODE_PORT=%q", cfg.ProblemID, cfg.UserID, cfg.ProblemKind, cfg.NodePort)

	if cfg.ProblemID == "" || cfg.UserID == "" || cfg.ProblemKind == "" {
		log.Fatal("environment variables not set: PROBLEM_ID, USER_ID, PROBLEM_KIND")
	}

	if cfg.ProblemKind == "WEBHACKING" {
		if cfg.HttpUrl == "" {
			cfg.HttpUrl = "localhost"
		}

		if cfg.HttpPort == "" || cfg.NodePort == "" {
			log.Fatal("web problem environment not set: HTTP_PORT or NODE_PORT missing.")
		}
	} else {
		log.Fatal("problem is not webhacking.")
	}

	log.Printf("Loaded config: %+v", cfg)
	return cfg
}