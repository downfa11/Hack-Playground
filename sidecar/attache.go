package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net"
	"net/http"
	"os"
	"sync"
	"time"

	"github.com/google/gopacket"
	"github.com/google/gopacket/layers"
	"github.com/google/gopacket/pcap"
)

const (
	snapshotLen = 1024
	promiscuous = true
	filter      = "tcp[tcpflags] & (tcp-syn) != 0 and tcp[tcpflags] & (tcp-ack) == 0"
)

var (
	ipTimestamps = make([]IPTimestamp, 0)
	mu           sync.Mutex
	filePath     = os.Getenv("FILE_PATH")
	httpPort     = os.Getenv("PORT")
)

type IPTimestamp struct {
	IP        string    `json:"ip"`
	Timestamp time.Time `json:"timestamp"`
	Protocol  string    `json:"protocol"`
}

func init() {
	if filePath == "" {
		filePath = "/tmp/last_connections.json"
	}
	if httpPort == "" {
		httpPort = ":8888"
	}
}

func main() {
	device, err := findCaptureInterface()
	if err != nil {
		log.Fatal("Failed to find suitable network interface:", err)
	}
	log.Println("Using network interface:", device)

	handle, err := pcap.OpenLive(device, snapshotLen, promiscuous, pcap.BlockForever)
	if err != nil {
		log.Fatal("Error opening device:", err)
	}
	defer handle.Close()

	if err := handle.SetBPFFilter(filter); err != nil {
		log.Fatal("Error setting BPF filter:", err)
	}
	log.Println("Listening for TCP SYN packets on", device)

	go startHTTPServer(httpPort)

	packetSource := gopacket.NewPacketSource(handle, handle.LinkType())
	for packet := range packetSource.Packets() {
		tcpLayer := packet.Layer(layers.LayerTypeTCP)
		if tcpLayer == nil {
			continue
		}
		tcp, _ := tcpLayer.(*layers.TCP)

		if tcp.SYN && !tcp.ACK {
			ipLayer := packet.Layer(layers.LayerTypeIPv4)
			if ipLayer == nil {
				continue
			}

			ip, _ := ipLayer.(*layers.IPv4)
			srcIP := ip.SrcIP.String()
			protocol := "TCP"
			now := time.Now()

			mu.Lock()
			ipTimestamps = append(ipTimestamps, IPTimestamp{
				IP:        srcIP,
				Timestamp: now,
				Protocol:  protocol,
			})
			mu.Unlock()

			if err := writeToFile(); err != nil {
				log.Println("Failed to write to file:", err)
			} else {
				log.Println("Connection from", srcIP, "detected, protocol:", protocol, "updated timestamp:", now)
			}
		}
	}
}

func findCaptureInterface() (string, error) {
	interfaces, err := net.Interfaces()
	if err != nil {
		return "", err
	}

	for _, iface := range interfaces {
		// loopback 제외
		if (iface.Flags&net.FlagLoopback) == 0 &&
			(iface.Flags&net.FlagUp) != 0 {
			addrs, err := iface.Addrs()
			if err != nil || len(addrs) == 0 {
				continue
			}
			log.Println("Available interface:", iface.Name)
			return iface.Name, nil
		}
	}
	return "", fmt.Errorf("no suitable interface found")
}

func writeToFile() error {
	mu.Lock()
	defer mu.Unlock()

	data, err := json.Marshal(ipTimestamps)
	if err != nil {
		return fmt.Errorf("failed to marshal IP timestamps: %v", err)
	}
	if err := os.WriteFile(filePath, data, 0644); err != nil {
		return fmt.Errorf("failed to write file: %v", err)
	}
	return nil
}

func startHTTPServer(httpPort string) {
	http.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		fmt.Fprintln(w, "OK")
	})

	http.HandleFunc("/last-connection", func(w http.ResponseWriter, r *http.Request) {
		data, err := os.ReadFile(filePath)
		if err != nil {
			http.Error(w, "No connections recorded", http.StatusNotFound)
			return
		}
		w.Write(data)
	})

	log.Println("Starting HTTP server on", httpPort)
	if err := http.ListenAndServe(httpPort, nil); err != nil {
		log.Fatal("HTTP server error:", err)
	}
}
