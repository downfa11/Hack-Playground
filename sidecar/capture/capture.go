package capture

import (
	"encoding/json"
	"fmt"
	"log"
	"net"
	"os"
	"sidecar/config"
	"time"

	"github.com/google/gopacket"
	"github.com/google/gopacket/layers"
	"github.com/google/gopacket/pcap"
)

const snapshotLen = 1024
const promiscuous = true
const filter = "tcp[tcpflags] & (tcp-syn) != 0 and tcp[tcpflags] & (tcp-ack) == 0"

type IPTimestamp struct {
	IP        string    `json:"ip"`
	Timestamp time.Time `json:"timestamp"`
	Protocol  string    `json:"protocol"`
}

var ipTimestamps []IPTimestamp

func StartPacketCapture() {
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
		log.Println("Listening for TCP SYN packets on", device)

		packetSource := gopacket.NewPacketSource(handle, handle.LinkType())
		for packet := range packetSource.Packets() {
			tcpLayer := packet.Layer(layers.LayerTypeTCP)
			if tcpLayer == nil {
				continue
			}

			tcp, ok := tcpLayer.(*layers.TCP)
			if !ok {
				log.Println("Error: failed to parse TCP layer")
				continue
			}

			if tcp.SYN && !tcp.ACK {
				ipLayer := packet.Layer(layers.LayerTypeIPv4)
				if ipLayer == nil {
					continue
				}

				ip, _ := ipLayer.(*layers.IPv4)
				srcIP := ip.SrcIP.String()
				now := time.Now()

				ipTimestamps = append(ipTimestamps, IPTimestamp{
					IP:        srcIP,
					Timestamp: now,
					Protocol:  "TCP",
				})

				if err := writeToFile(); err != nil {
					log.Println("Failed to write to file:", err)
				}
			}
		}
	}()
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
			log.Println("Available interface:", iface.Name)
			return iface.Name, nil
		}
	}
	return "", fmt.Errorf("no suitable interface found")
}

func writeToFile() error {
	data, err := json.Marshal(ipTimestamps)
	if err != nil {
		return fmt.Errorf("failed to marshal IP timestamps: %v", err)
	}
	if err := os.WriteFile(config.FilePath, data, 0644); err != nil {
		return fmt.Errorf("failed to write file: %v", err)
	}
	return nil
}
