package com.edu.vitals.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Handshake interceptor for WebSocket connections logging client connections and session tokens.
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        String uri = request.getURI().toString();
        log.info("[WebSocket Handshake INBOUND] Client connecting from: {}", uri);
        attributes.put("connectTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {

        if (exception != null) {
            log.error("[WebSocket Handshake ERROR] Handshake failed: {}", exception.getMessage(), exception);
        } else {
            log.info("[WebSocket Handshake ESTABLISHED] Successfully connected to /ws-vitals");
        }
    }
}
