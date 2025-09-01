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
const filter = "tcp[tcpflags] & (tcp-syn|tcp-fin|tcp-rst|tcp-ack) != 0"

type IPTimestamp struct {
	IP        string    `json:"ip"`
	Timestamp time.Time `json:"timestamp"`
	Protocol  string    `json:"protocol"`
}

var ipTimestamps = make(map[string]IPTimestamp)

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

			if tcp.SYN && !tcp.ACK {
				srcIP := ip.SrcIP.String()
				now := time.Now()
				ipTimestamps[srcIP] = IPTimestamp{
					IP:        srcIP,
					Timestamp: now,
					Protocol:  "TCP",
				}

				if tcp.FIN || tcp.RST {
					delete(ipTimestamps, srcIP)
				}

				data, err := json.MarshalIndent(getLatestTimestamps(), "", "  ")
				if err != nil {
					log.Printf("Failed to marshal timestamps: %v", err)
					continue
				}

				if err := utils.WriteToFile(cfg.FilePath, data); err != nil {
					log.Printf("Failed to write timestamp log to file: %v", err)
				}
			}
		}
	}()
}

func getLatestTimestamps() []IPTimestamp {
	results := make([]IPTimestamp, 0, len(ipTimestamps))
	for _, ts := range ipTimestamps {
		results = append(results, ts)
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