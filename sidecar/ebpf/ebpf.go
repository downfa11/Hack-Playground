package ebpf

import (
	"encoding/binary"
	"encoding/json"
	"fmt"
	"log"
	"net"
	"os"
	"time"
	"unsafe"

	"sidecar/capture"
	"sidecar/config"
	"sidecar/utils"
	// "github.com/cilium/ebpf/link"
	// "github.com/cilium/ebpf/perf"
	// "github.com/cilium/ebpf/rlimit"
)

//go:generate go run github.com/cilium/ebpf/cmd/bpf2go -type tcp_closed_event tcp_close tcp_close.c

type TcpClosedEvent struct {
	Saddr      uint32 // Source IP (Network Byte Order)
	Daddr      uint32 // Destination IP
	Sport      uint16 // Source port
	Dport      uint16 // Destination port
	DurationNs uint64 // Duration in nanoseconds
}

func StartEBPFMonitor(cfg config.Config) {
	go func() {
		if err := runEBPFMonitor(cfg); err != nil {
			log.Printf("eBPF Monitor error: %v", err)
		}
	}()
}

func runEBPFMonitor(cfg config.Config) error {
	// Allow the current process to lock memory for eBPF resources
	// if err := rlimit.RemoveMemlock(); err != nil {
	// 	return fmt.Errorf("failed to remove memlock: %w", err)
	// }

	// // Load pre-compiled eBPF programs
	// objs := tcp_closeObjects{}
	// if err := loadTcp_closeObjects(&objs, nil); err != nil {
	// 	return fmt.Errorf("failed to load eBPF objects: %w", err)
	// }
	// defer objs.Close()

	// // Attach kprobe to tcp_set_state
	// kprobeSetState, err := link.Kprobe("tcp_set_state", objs.KprobeTcpSetState, nil)
	// if err != nil {
	// 	return fmt.Errorf("failed to attach kprobe to tcp_set_state: %w", err)
	// }
	// defer kprobeSetState.Close()

	// // Attach kprobe to tcp_close
	// kprobeClose, err := link.Kprobe("tcp_close", objs.KprobeTcpClose, nil)
	// if err != nil {
	// 	return fmt.Errorf("failed to attach kprobe to tcp_close: %w", err)
	// }
	// defer kprobeClose.Close()

	// log.Println("eBPF Monitor: Successfully attached kprobes, monitoring TCP connections...")

	// // Open a perf event reader from the events map
	// rd, err := perf.NewReader(objs.Events, os.Getpagesize())
	// if err != nil {
	// 	return fmt.Errorf("failed to create perf reader: %w", err)
	// }
	// defer rd.Close()

	// // Read events in a loop
	// for {
	// 	record, err := rd.Read()
	// 	if err != nil {
	// 		if errors.Is(err, perf.ErrClosed) {
	// 			return nil
	// 		}
	// 		log.Printf("eBPF Monitor: Error reading event: %v", err)
	// 		continue
	// 	}

	// 	// Parse the event
	// 	if record.LostSamples != 0 {
	// 		log.Printf("eBPF Monitor: Lost %d samples", record.LostSamples)
	// 		continue
	// 	}

	// 	var event TcpClosedEvent
	// 	if err := binary.Read(bytes.NewBuffer(record.RawSample), binary.LittleEndian, &event); err != nil {
	// 		log.Printf("eBPF Monitor: Failed to parse event: %v", err)
	// 		continue
	// 	}

	// 	// Write event to file
	// 	writeEBPFClosedEvent(cfg, event)
	// }
	return nil
}

func writeEBPFClosedEvent(cfg config.Config, event TcpClosedEvent) {
	// Convert IP addresses from network byte order
	srcIP := intToIP(event.Saddr)
	dstIP := intToIP(event.Daddr)

	// Convert port from network byte order
	dport := binary.BigEndian.Uint16((*(*[2]byte)(unsafe.Pointer(&event.Dport)))[:])

	durationSec := float64(event.DurationNs) / 1e9

	log.Printf("eBPF Monitor: TCP connection closed: %s:%d -> %s:%d (duration: %.2fs)",
		srcIP, event.Sport, dstIP, dport, durationSec)

	// Read existing events
	data, _ := os.ReadFile(cfg.FilePath)
	var allEvents []capture.IPEvent
	if len(data) > 0 {
		if err := json.Unmarshal(data, &allEvents); err != nil {
			allEvents = []capture.IPEvent{}
		}
	}

	// Create new event
	newEvent := capture.IPEvent{
		IP:        srcIP,
		Timestamp: time.Now(),
		Protocol:  "TCP_CLOSE",
		Payload: fmt.Sprintf("Closed: %s:%d -> %s:%d (%.2fs)",
			srcIP, event.Sport, dstIP, dport, durationSec),
	}
	allEvents = append(allEvents, newEvent)

	// Write back to file
	updatedData, err := json.MarshalIndent(allEvents, "", "  ")
	if err != nil {
		log.Printf("eBPF Monitor: Failed to marshal events: %v", err)
		return
	}

	if err := utils.WriteToFile(cfg.FilePath, updatedData); err != nil {
		log.Printf("eBPF Monitor: Failed to write event: %v", err)
	}
}

// intToIP converts a uint32 IP address to string
func intToIP(ip uint32) string {
	return net.IPv4(
		byte(ip),
		byte(ip>>8),
		byte(ip>>16),
		byte(ip>>24),
	).String()
}
