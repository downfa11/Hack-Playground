.PHONY: all generate build run clean install-deps

all: generate build

generate:
	@echo "Generating eBPF programs..."
	cd ebpf && go generate ./...

build:
	@echo "Building sidecar..."
	go build -o sidecar .

run: build
	@echo "Running sidecar (requires root)..."
	sudo ./sidecar

clean:
	@echo "Cleaning..."
	rm -f sidecar
	rm -f ebpf/tcp_close_bpfel.go ebpf/tcp_close_bpfeb.go
	rm -f ebpf/tcp_close_bpfel.o ebpf/tcp_close_bpfeb.o

install-deps:
	@echo "Installing Go dependencies..."
	go get github.com/cilium/ebpf/cmd/bpf2go
	go get github.com/cilium/ebpf
	go mod tidy
	@echo "Installing bpf2go..."
	go install github.com/cilium/ebpf/cmd/bpf2go@latest

check-system:
	@echo "Checking system requirements..."
	@which clang > /dev/null || echo "⚠️  clang not found"
	@which llvm-strip > /dev/null || echo "⚠️  llvm-strip not found"
	@ls /usr/include/linux/bpf.h > /dev/null 2>&1 || echo "⚠️  kernel headers not found"
	@echo "✓ System check complete"

test-server:
	@echo "Starting test TCP server on port 8080..."
	nc -l 8080

test-client:
	@echo "Connecting to localhost:8080 (press Ctrl+C to close)..."
	nc localhost 8080