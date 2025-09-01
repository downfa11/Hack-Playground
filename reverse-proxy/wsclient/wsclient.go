package wsclient

import (
	"log"
	"time"

	"github.com/gorilla/websocket"
)

func StartWebSocketClient(wsURL string, problemID string, userID string) {

	go func() {
		for {
			conn, _, err := websocket.DefaultDialer.Dial(wsURL, nil)
			if err != nil {
				log.Printf("WebSocket connection failed: %v. Retrying in 5s...", err)
				time.Sleep(5 * time.Second)
				continue
			}
			log.Printf("WebSocket connected: %s (Problem: %s, User: %s)", wsURL, problemID, userID)

			// todo. 접속시 사용자 정보를 전달한다.
			message := []byte("USER:" + userID + ":" + problemID)
			if err := conn.WriteMessage(websocket.TextMessage, message); err != nil {
				log.Println("Failed to send user info:", err)
			}

			done := make(chan struct{})

			go func() {
				defer close(done)
				for {
					_, message, err := conn.ReadMessage()
					if err != nil {
						log.Println("WebSocket read error:", err)
						return
					}
					log.Printf("Received from server: %s", message)
				}
			}()

			go func() {
				ticker := time.NewTicker(10 * time.Second)
				defer ticker.Stop()
				for {
					select {
					case <-done:
						return
					case t := <-ticker.C:
						msg := []byte("Ping " + t.String())
						if err := conn.WriteMessage(websocket.TextMessage, msg); err != nil {
							log.Println("WebSocket write error:", err)
							return
						}
					}
				}
			}()

			<-done
			conn.Close()
			log.Println("WebSocket connection closed, reconnecting")
			time.Sleep(2 * time.Second)
		}
	}()
}
