package com.ns.solve.service.core;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionRegistry {

    private final Map<String, Map<String, WebSocketSession>> problemUserSessions = new ConcurrentHashMap<>();

    public void register(String problemId, String userId, WebSocketSession session) {
        problemUserSessions
                .computeIfAbsent(problemId, k -> new ConcurrentHashMap<>())
                .put(userId, session);
    }

    public void unregister(WebSocketSession session) {
        problemUserSessions.forEach((problemId, map) -> {
            map.entrySet().removeIf(e -> e.getValue().equals(session));
            if (map.isEmpty()) {
                problemUserSessions.remove(problemId);
            }
        });
    }

    public Optional<String> findUserId(WebSocketSession session) {
        return problemUserSessions.values().stream()
                .flatMap(map -> map.entrySet().stream())
                .filter(e -> e.getValue().equals(session))
                .map(Map.Entry::getKey)
                .findFirst();
    }


    public boolean isUserConnected(String problemId, String userId) {
        return problemUserSessions.containsKey(problemId) &&
                problemUserSessions.get(problemId).containsKey(userId);
    }
}

