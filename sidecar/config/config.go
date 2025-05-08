package config

import (
	"log"
	"os"
)

var ProblemID string
var UserID string
var UUID string
var FilePath string
var HttpPort string
var ProxyPort string
var ProxyIP string

func LoadConfig() {
	ProblemID = os.Getenv("PROBLEM_ID")
	UserID = os.Getenv("USER_ID")
	UUID = os.Getenv("UUID")
	FilePath = os.Getenv("FILE_PATH")

	if FilePath == "" {
		FilePath = "/tmp/last_connections.json"
	}

	HttpPort = os.Getenv("PORT")
	if HttpPort == "" {
		HttpPort = ":1880"
	}

	ProxyPort = os.Getenv("PROXY_PORT")
	if ProxyPort == "" {
		ProxyPort = "8080"
	}

	ProxyIP = os.Getenv("PROXY_IP")
	if ProxyIP == "" {
		ProxyIP = "180.83.48.182"
	}

	log.Printf("Loaded config: PROBLEM_ID=%s, USER_ID=%s, UUID=%s, FILE_PATH=%s, HTTP_PORT=%s, PROXY_PORT=%s, PROXY_IP=%s",
		ProblemID, UserID, UUID, FilePath, HttpPort, ProxyPort, ProxyIP)
}
