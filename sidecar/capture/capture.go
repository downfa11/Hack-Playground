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
const filter = "tcp[tcpflags] & (tcp-syn) != 0 and tcp[tcpflags] & (tcp-ack) == 0"

type IPTimestamp struct {
	IP        string    `json:"ip"`
	Timestamp time.Time `json:"timestamp"`
	Protocol  string    `json:"protocol"`
}

var ipTimestamps = make(map[string]IPTimestamp)

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
			log.Println("--- Attache: Packet Captured ---")
			log.Println("Timestamp      :", packet.Metadata().Timestamp)

			if ethLayer := packet.Layer(layers.LayerTypeEthernet); ethLayer != nil {
				eth := ethLayer.(*layers.Ethernet)
				log.Printf("Ethernet       : Src=%s, Dst=%s, Type=%s\n", eth.SrcMAC, eth.DstMAC, eth.EthernetType)
			}

			ipLayer := packet.Layer(layers.LayerTypeIPv4)
			if ipLayer == nil {
				continue
			}
			ip := ipLayer.(*layers.IPv4)
			log.Printf("IPv4           : Src=%s, Dst=%s, Protocol=%s\n", ip.SrcIP, ip.DstIP, ip.Protocol)

			tcpLayer := packet.Layer(layers.LayerTypeTCP)
			if tcpLayer == nil {
				continue
			}
			tcp := tcpLayer.(*layers.TCP)
			log.Printf("TCP            : SrcPort=%d, DstPort=%d, Seq=%d, SYN=%v, ACK=%v\n",
				tcp.SrcPort, tcp.DstPort, tcp.Seq, tcp.SYN, tcp.ACK)

			if tcp.SYN && !tcp.ACK {
				srcIP := ip.SrcIP.String()
				now := time.Now()
				ipTimestamps[srcIP] = IPTimestamp{
					IP:        srcIP,
					Timestamp: now,
					Protocol:  "TCP",
				}

				data, err := json.MarshalIndent(getLatestTimestamps(), "", "  ")
				if err != nil {
					log.Printf("Failed to marshal timestamps: %v", err)
					continue
				}

				if err := utils.WriteToFile(config.FilePath, data); err != nil {
					log.Printf("Failed to write timestamp log to file: %v", err)
				}
			}
			log.Println("------------------------")
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
			log.Println("Available interface:", iface.Name)
			return iface.Name, nil
		}
	}
	return "", fmt.Errorf("no suitable interface found")
}
