package main

import (
	"bufio"
	"fmt"
	"io"
	"net"
	"os"
	"strconv"
	"strings"
	"time"
)

func main() {
	listenPort := getEnv("PROXY_INBOUND_PORT", "1337")
	targetPort := getEnv("PROXY_OUTBOUND_PORT", "1338")
	accessFile := getEnv("LAST_ACCESS_FILE", "/shared/last-accessed")

	ln, err := net.Listen("tcp", ":"+listenPort)
	if err != nil {
		panic(err)
	}
	fmt.Println("Proxy listening on", listenPort)

	for {
		conn, err := ln.Accept()
		if err != nil {
			continue
		}
		go handle(conn, "localhost:"+targetPort, accessFile)
	}
}

func handle(client net.Conn, targetAddr, accessFile string) {
	defer client.Close()

	ts := strconv.FormatInt(time.Now().Unix(), 10)
	_ = os.WriteFile(accessFile, []byte(ts), 0644)

	target, err := net.Dial("tcp", targetAddr)
	if err != nil {
		fmt.Println("Target dial failed:", err)
		return
	}
	defer target.Close()

	clientBuf := bufio.NewReader(client)
	firstLine, err := clientBuf.ReadString('\n')
	if err != nil {
		return
	}

	if strings.HasPrefix(firstLine, "GET") || strings.HasPrefix(firstLine, "POST") {
		fmt.Println("Detected HTTP request:", strings.TrimSpace(firstLine))
	}

	_, _ = target.Write([]byte(firstLine))

	go io.Copy(target, clientBuf)
	io.Copy(client, target)
}

func getEnv(key, fallback string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	return fallback
}