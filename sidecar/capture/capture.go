package capture

import (
	"encoding/json"
	"fmt"
	"log"
	"net"
	"time"

	"sidecar/config"
	"sidecar/utils"

	"github.com/google/gopacket"
	"github.com/google/gopacket/layers"
	"github.com/google/gopacket/pcap"
)

const snapshotLen = 1024
const promiscuous = true
const filter = "tcp"

type IPEvent struct {
	IP        string    `json:"ip"`
	Timestamp time.Time `json:"timestamp"`
	Protocol  string    `json:"protocol"` // SYN, ACK, FIN, RST, KeepAlive
	Payload   string    `json:"payload"`
}

var ipActive = make(map[string]IPEvent)
var ipHistory = make([]IPEvent, 0)

func StartPacketCapture(cfg config.Config) {
	go func() {
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

		packetSource := gopacket.NewPacketSource(handle, handle.LinkType())
		for packet := range packetSource.Packets() {
			ipLayer := packet.Layer(layers.LayerTypeIPv4)
			tcpLayer := packet.Layer(layers.LayerTypeTCP)
			if ipLayer == nil || tcpLayer == nil {
				continue
			}

			ip := ipLayer.(*layers.IPv4)
			tcp := tcpLayer.(*layers.TCP)

			srcIP := ip.SrcIP.String()
			now := time.Now()

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
					proto = "KeepAlive"
				} else {
					proto = "ACK"
				}
			} else {
				proto = "OTHER"
			}

			event := IPEvent{
				IP:        srcIP,
				Timestamp: now,
				Protocol:  proto,
				Payload:   payload,
			}

			if proto == "SYN" || proto == "ACK" || proto == "KeepAlive" {
				ipActive[srcIP] = event
				log.Printf("[Active Connections] src=%s proto=%s payload=%q", srcIP, proto, payload)
			} else if proto == "FIN" || proto == "RST" {
				ipHistory = append(ipHistory, event)
				delete(ipActive, srcIP)
				log.Printf("[Connection Closed] src=%s proto=%s", srcIP, proto)
			}

			allEvents := append(ipHistory, mapToSlice(ipActive)...)
			data, err := json.MarshalIndent(allEvents, "", "  ")
			if err != nil {
				log.Printf("Failed to marshal timestamps: %v", err)
				continue
			}

			if err := utils.WriteToFile(cfg.FilePath, data); err != nil {
				log.Printf("Failed to write timestamp log to file: %v", err)
			}
		}
	}()
}

// ipActive map -> slice
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
	for _, iface := range interfaces {
		if (iface.Flags&net.FlagLoopback) == 0 && (iface.Flags&net.FlagUp) != 0 {
			addrs, err := iface.Addrs()
			if err != nil || len(addrs) == 0 {
				continue
			}
			return iface.Name, nil
		}
	}
	return "", fmt.Errorf("no suitable interface found")
}
