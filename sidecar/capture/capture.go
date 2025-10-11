package capture

import (
	"bufio"
	"encoding/json"
	"fmt"
	"log"
	"net"
	"os"
	"strings"
	"sync"
	"time"

	"sidecar/config"
	"sidecar/utils"

	"github.com/google/gopacket"
	"github.com/google/gopacket/layers"
	"github.com/google/gopacket/pcap"
)

const snapshotLen = 1024
const promiscuous = true

type IPEvent struct {
	IP        string    `json:"ip"`
	Timestamp time.Time `json:"timestamp"`
	Protocol  string    `json:"protocol"`
	Payload   string    `json:"payload"`
}

var (
	ipActive       = make(map[string]IPEvent)
	ipHistory      = make([]IPEvent, 0)
	fileWriteMutex sync.Mutex
	targetPort     uint16
	targetPortHex  string
	globalConfig   config.Config
)

func StartPacketCapture(cfg config.Config, port int) {
	targetPort = uint16(port)
	targetPortHex = fmt.Sprintf("%04X", port)
	globalConfig = cfg

	go func() {
		device, err := findCaptureInterface()
		if err != nil {
			log.Fatal("Failed to find suitable network interface:", err)
		}
		log.Printf("🔍 Unified Monitor started on: %s (port %d)", device, targetPort)

		handle, err := pcap.OpenLive(device, snapshotLen, promiscuous, pcap.BlockForever)
		if err != nil {
			log.Fatal("Error opening device:", err)
		}
		defer handle.Close()

		bpfFilter := fmt.Sprintf("tcp port %d", targetPort)
		if err := handle.SetBPFFilter(bpfFilter); err != nil {
			log.Fatal("Error setting BPF filter:", err)
		}
		log.Printf("📡 Monitoring port %d (hex: %s)", targetPort, targetPortHex)

		packetSource := gopacket.NewPacketSource(handle, handle.LinkType())
		for packet := range packetSource.Packets() {
			processPacket(packet)
		}
	}()
}

func processPacket(packet gopacket.Packet) {
	ipLayer := packet.Layer(layers.LayerTypeIPv4)
	tcpLayer := packet.Layer(layers.LayerTypeTCP)
	if ipLayer == nil || tcpLayer == nil {
		return
	}

	ip := ipLayer.(*layers.IPv4)
	tcp := tcpLayer.(*layers.TCP)

	if uint16(tcp.DstPort) != targetPort {
		return
	}

	srcIP := ip.SrcIP.String()
	now := time.Now()
	connKey := fmt.Sprintf("%s:%d", srcIP, tcp.SrcPort)

	var proto string
	payload := string(tcp.Payload)

	if tcp.SYN && !tcp.ACK {
		proto = "SYN"
	} else if tcp.FIN {
		proto = "FIN"
	} else if tcp.RST {
		proto = "RST"
	} else if tcp.ACK {
		if len(tcp.Payload) == 0 {
			proto = "ACK"
		} else {
			proto = "DATA"
		}
	} else {
		proto = "OTHER"
	}

	event := IPEvent{
		IP:        srcIP,
		Timestamp: now,
		Protocol:  proto,
		Payload:   fmt.Sprintf("%s:%d -> :%d | %s", srcIP, tcp.SrcPort, tcp.DstPort, proto),
	}

	if proto == "SYN" {
		ipActive[connKey] = event
		log.Printf("🟢 [NEW] %s", event.Payload)

	} else if proto == "FIN" || proto == "RST" {
		ipHistory = append(ipHistory, event)
		delete(ipActive, connKey)
		log.Printf("🔴 [CLOSE] %s", event.Payload)

		activeCount := checkPortActiveConnections()
		log.Printf("   ↳ Remaining connections: %d", activeCount)

		if activeCount == 0 {
			log.Printf("🚨 [IDLE] All connections closed on port %d!", targetPort)
			writeIdleTimeoutEvent()
		}

	} else if proto == "DATA" {
		ipActive[connKey] = event
		if len(payload) > 0 {
			log.Printf("📦 [DATA] %s | payload_len=%d", event.Payload, len(payload))
		}
	}

	fileWriteMutex.Lock()
	writeEventsToFile()
	fileWriteMutex.Unlock()
}

// /proc/net/tcp을 통해서 활성 TCP Connections 개수 확인
func checkPortActiveConnections() int {
	activeCount := 0

	file, err := os.Open("/proc/net/tcp")
	if err != nil {
		log.Printf("⚠️  Error opening /proc/net/tcp: %v", err)
		return -1
	}
	defer file.Close()

	scanner := bufio.NewScanner(file)
	scanner.Scan() // Skip header

	for scanner.Scan() {
		line := scanner.Text()
		fields := strings.Fields(line)

		if len(fields) < 4 {
			continue
		}

		localAddrPort := fields[1]
		state := fields[3]

		if strings.HasSuffix(localAddrPort, ":"+targetPortHex) {
			// CLOSE(08), CLOSE_WAIT(07) 상태가 아니면 활성
			if state != "08" && state != "07" {
				activeCount++
			}
		}
	}

	return activeCount
}

func writeIdleTimeoutEvent() {
	idleEvent := IPEvent{
		IP:        "system",
		Timestamp: time.Now(),
		Protocol:  "IDLE_TIMEOUT",
		Payload:   fmt.Sprintf("No active connections on port %d", targetPort),
	}

	ipHistory = append(ipHistory, idleEvent)

	fileWriteMutex.Lock()
	writeEventsToFile()
	fileWriteMutex.Unlock()
}

func writeEventsToFile() {
	allEvents := append(ipHistory, mapToSlice(ipActive)...)
	data, err := json.MarshalIndent(allEvents, "", "  ")
	if err != nil {
		log.Printf("Failed to marshal events: %v", err)
		return
	}

	if err := utils.WriteToFile(globalConfig.FilePath, data); err != nil {
		log.Printf("Failed to write events to file: %v", err)
	}
}

func mapToSlice(m map[string]IPEvent) []IPEvent {
	results := make([]IPEvent, 0, len(m))
	for _, v := range m {
		results = append(results, v)
	}
	return results
}

func findCaptureInterface() (string, error) {
	interfaces, err := net.Interfaces()
	if err != nil {
		return "", err
	}

	var firstActive string
	for _, iface := range interfaces {
		if (iface.Flags & net.FlagUp) == 0 {
			continue
		}

		addrs, err := iface.Addrs()
		if err != nil || len(addrs) == 0 {
			continue
		}

		if iface.Name == "eth0" {
			return iface.Name, nil
		}

		if (iface.Flags&net.FlagLoopback) == 0 && firstActive == "" {
			firstActive = iface.Name
		}
	}

	if firstActive != "" {
		return firstActive, nil
	}
	return "", fmt.Errorf("no suitable interface found")
}
