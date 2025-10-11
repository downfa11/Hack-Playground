//go:build ignore

#include <linux/bpf.h>
#include <bpf/bpf_helpers.h>
#include <linux/types.h>
#include <linux/socket.h>
#include <linux/in.h>

// TCP connection closed 이벤트 구조체
struct tcp_closed_event {
    __u32 saddr;      // Source IP address
    __u32 daddr;      // Destination IP address
    __u16 sport;      // Source port
    __u16 dport;      // Destination port
    __u64 duration_ns; // Connection duration in nanoseconds
};

// Perf event map for sending events to userspace
struct {
    __uint(type, BPF_MAP_TYPE_PERF_EVENT_ARRAY);
    __uint(key_size, sizeof(__u32));
    __uint(value_size, sizeof(__u32));
} events SEC(".maps");

// Map to track connection start times
struct {
    __uint(type, BPF_MAP_TYPE_HASH);
    __uint(max_entries, 10240);
    __type(key, __u64);  // sock pointer as key
    __type(value, __u64); // timestamp
} conn_start_time SEC(".maps");

// kprobe for tcp_set_state - track when connection becomes ESTABLISHED
SEC("kprobe/tcp_set_state")
int kprobe_tcp_set_state(struct pt_regs *ctx)
{
    struct sock *sk = (struct sock *)PT_REGS_PARM1(ctx);
    int new_state = (int)PT_REGS_PARM2(ctx);
    
    // TCP_ESTABLISHED = 1
    if (new_state == 1) {
        __u64 ts = bpf_ktime_get_ns();
        __u64 sock_ptr = (__u64)sk;
        bpf_map_update_elem(&conn_start_time, &sock_ptr, &ts, BPF_ANY);
    }
    
    return 0;
}

// kprobe for tcp_close - detect connection closure
SEC("kprobe/tcp_close")
int kprobe_tcp_close(struct pt_regs *ctx)
{
    struct sock *sk = (struct sock *)PT_REGS_PARM1(ctx);
    __u64 sock_ptr = (__u64)sk;
    
    // Get start time from map
    __u64 *start_ts = bpf_map_lookup_elem(&conn_start_time, &sock_ptr);
    if (!start_ts) {
        return 0; // No record of this connection
    }
    
    __u64 end_ts = bpf_ktime_get_ns();
    __u64 duration = end_ts - *start_ts;
    
    // Read socket information
    struct tcp_closed_event event = {};
    
    // Read IP addresses and ports from sock struct
    // Offsets vary by kernel version, these are common
    bpf_probe_read(&event.saddr, sizeof(event.saddr), 
                   (__u8 *)sk + offsetof(struct sock, __sk_common) + 
                   offsetof(struct sock_common, skc_rcv_saddr));
    
    bpf_probe_read(&event.daddr, sizeof(event.daddr),
                   (__u8 *)sk + offsetof(struct sock, __sk_common) + 
                   offsetof(struct sock_common, skc_daddr));
    
    bpf_probe_read(&event.sport, sizeof(event.sport),
                   (__u8 *)sk + offsetof(struct sock, __sk_common) + 
                   offsetof(struct sock_common, skc_num));
    
    bpf_probe_read(&event.dport, sizeof(event.dport),
                   (__u8 *)sk + offsetof(struct sock, __sk_common) + 
                   offsetof(struct sock_common, skc_dport));
    
    event.duration_ns = duration;
    
    // Send event to userspace
    bpf_perf_event_output(ctx, &events, BPF_F_CURRENT_CPU, 
                         &event, sizeof(event));
    
    // Clean up map entry
    bpf_map_delete_elem(&conn_start_time, &sock_ptr);
    
    return 0;
}

char _license[] SEC("license") = "GPL";