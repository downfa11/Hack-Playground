package proxy

import (
	"fmt"
	"log"
	"net/http"
	"net/http/httputil"
	"net/url"
)

type Proxy struct {
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

	return &Proxy{reverseProxy: rp}
}

func (p *Proxy) Handler(w http.ResponseWriter, r *http.Request) {
	p.reverseProxy.ServeHTTP(w, r)
}

func StartHTTPServer(port string, proxy *Proxy) {
	http.HandleFunc("/", proxy.Handler)
	log.Printf("Starting HTTP proxy on %s", port)
	if err := http.ListenAndServe(port, nil); err != nil {
		log.Fatalf("HTTP server failed: %v", err)
	}
}
