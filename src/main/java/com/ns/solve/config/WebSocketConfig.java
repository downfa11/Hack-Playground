package com.ns.solve.config;

import com.ns.solve.service.core.WebSocketSessionRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketSessionRegistry sessionRegistry;

    public WebSocketConfig(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new UserWebSocketHandler(sessionRegistry), "/ws")
                .setAllowedOrigins("*");
    }

    private static class UserWebSocketHandler implements WebSocketHandler {

        private final WebSocketSessionRegistry sessionRegistry;

        public UserWebSocketHandler(WebSocketSessionRegistry sessionRegistry) {
            this.sessionRegistry = sessionRegistry;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            System.out.println("New WebSocket connection: " + session.getId());
        }

        @Override
        public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
            String payload = message.getPayload().toString();

            // USER:<userId>:<problemId>
            if (payload.startsWith("USER:")) {
                String[] parts = payload.substring(5).split(":");
                if (parts.length != 2) {
                    System.err.println("Invalid registration message: " + payload);
                    return;
                }
                String userId = parts[0];
                String problemId = parts[1];

                sessionRegistry.register(problemId, userId, session);
                System.out.printf("Registered user %s for problem %s (session %s)%n", userId, problemId, session.getId());
            } else {
                String userId = sessionRegistry.findUserId(session).orElse("Unknown");
                System.out.printf("Message from user %s: %s%n", userId, payload);
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) {
            System.err.printf("Transport error in session %s: %s%n", session.getId(), exception.getMessage());
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
            sessionRegistry.unregister(session);
            System.out.printf("WebSocket connection closed for session %s%n", session.getId());
        }

        @Override
        public boolean supportsPartialMessages() {
            return false;
        }
    }
}
