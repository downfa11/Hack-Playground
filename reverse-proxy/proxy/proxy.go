package proxy

import (
	"fmt"
	"log"
	"net"
	"net/http"
	"net/http/httputil"
	"net/url"
	"time"

	"github.com/gorilla/websocket"
)

type Proxy struct {
	targetURL    *url.URL
	reverseProxy *httputil.ReverseProxy
}

func NewProxy(targetHost string, targetPort string) *Proxy {
	targetURL, err := url.Parse(fmt.Sprintf("http://%s:%s", targetHost, targetPort))
	if err != nil {
		log.Fatalf("Failed to parse target URL: %v", err)
	}

	rp := httputil.NewSingleHostReverseProxy(targetURL)
	origDirector := rp.Director
	rp.Director = func(req *http.Request) {
		origDirector(req)
		if req.Header.Get("Upgrade") == "websocket" {
			req.Header.Set("Connection", "upgrade")
		}
	}

	return &Proxy{
		targetURL:    targetURL,
		reverseProxy: rp,
	}
}

func (p *Proxy) Handler(w http.ResponseWriter, r *http.Request) {
	// 브라우저용 HTML 제공
	if r.URL.Path == "/wsclient.html" {
		http.ServeFile(w, r, "/wsclient.html")
		return
	}

	// WebSocket 처리
	if websocket.IsWebSocketUpgrade(r) {
		p.handleWebSocket(w, r)
		return
	}

	// 나머지 HTTP 요청은 문제 컨테이너로 프록시
	p.reverseProxy.ServeHTTP(w, r)
}

func StartHTTPServer(listenPort, targetHost, targetPort string) {
	proxy := NewProxy(targetHost, targetPort)

	http.HandleFunc("/", proxy.Handler)
    log.Printf("Starting HTTP proxy on ListenPort %s -> problem container %s:%s", listenPort, targetHost, targetPort)

    if err := http.ListenAndServe(":"+listenPort, nil); err != nil {
        log.Fatalf("HTTP server failed: %v", err)
    }
}

func (p *Proxy) handleWebSocket(w http.ResponseWriter, r *http.Request) {
	upgrader := websocket.Upgrader{
		CheckOrigin: func(r *http.Request) bool { return true },
	}

	log.Printf("[WS] Upgrade request from %s path=%s", r.RemoteAddr, r.URL.Path)

	clientConn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Printf("[WS ERROR] Upgrade failed from %s path=%s: %v", r.RemoteAddr, r.URL.Path, err)
		return
	}
	defer func() {
		log.Printf("[WS] Client connection closed %s path=%s", r.RemoteAddr, r.URL.Path)
		clientConn.Close()
	}()

	// pong 핸들러 설정
	clientConn.SetPongHandler(func(appData string) error {
		log.Printf("[WS PONG] from client %s path=%s", r.RemoteAddr, r.URL.Path)
		return nil
	})

	dialer := websocket.Dialer{
		NetDial: func(network, addr string) (net.Conn, error) {
			c, err := net.Dial(network, addr)
			if err != nil {
				return nil, err
			}
			if tcpConn, ok := c.(*net.TCPConn); ok {
				_ = tcpConn.SetKeepAlive(true)
				_ = tcpConn.SetKeepAlivePeriod(10 * time.Second)
			}
			return c, nil
		},
	}

	backendURL := fmt.Sprintf("ws://%s%s", p.targetURL.Host, r.URL.Path)
	log.Printf("[WS] Dialing backend %s for client %s path=%s", backendURL, r.RemoteAddr, r.URL.Path)
	backendConn, _, err := dialer.Dial(backendURL, nil)
	if err != nil {
		log.Printf("[WS ERROR] Backend WS connection failed for client %s path=%s: %v", r.RemoteAddr, r.URL.Path, err)
		return
	}
	defer func() {
		log.Printf("[WS] Backend connection closed for client %s path=%s", r.RemoteAddr, r.URL.Path)
		backendConn.Close()
	}()

	log.Printf("[WS CONNECTED] client=%s backend=%s path=%s", r.RemoteAddr, backendURL, r.URL.Path)

	done := make(chan struct{})

	// heartbeat ticker (서버→클라이언트 ping)
	ticker := time.NewTicker(15 * time.Second)
	defer ticker.Stop()

	// 클라이언트 → 백엔드
	go func() {
		defer close(done)
		for {
			mt, message, err := clientConn.ReadMessage()
			if err != nil {
				log.Printf("[WS] Client read error (closing): %v", err)
				return
			}
			if err := backendConn.WriteMessage(mt, message); err != nil {
				log.Printf("[WS] Backend write error: %v", err)
				return
			}
		}
	}()

	// 백엔드 → 클라이언트
	go func() {
		for {
			mt, message, err := backendConn.ReadMessage()
			if err != nil {
				log.Printf("[WS] Backend read error: %v", err)
				clientConn.WriteControl(
					websocket.CloseMessage,
					websocket.FormatCloseMessage(websocket.CloseNormalClosure, ""),
					time.Now().Add(time.Second),
				)
				return
			}
			if err := clientConn.WriteMessage(mt, message); err != nil {
				log.Printf("[WS] Client write error: %v", err)
				return
			}
		}
	}()

	// ping loop
	go func() {
		for range ticker.C {
			if err := clientConn.WriteControl(websocket.PingMessage, []byte{}, time.Now().Add(time.Second)); err != nil {
				log.Printf("[WS] Ping failed to client %s path=%s: %v", r.RemoteAddr, r.URL.Path, err)
				clientConn.Close()
				return
			} else {
				log.Printf("[WS] Ping sent to client %s path=%s", r.RemoteAddr, r.URL.Path)
			}
		}
	}()

	<-done
	log.Printf("[WS CLOSED] client=%s path=%s backend=%s", r.RemoteAddr, r.URL.Path, backendURL)
}
