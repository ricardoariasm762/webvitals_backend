package com.edu.vitals.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebSocketHandshakeInterceptorTest {

    @Mock
    private ServerHttpRequest request;

    @Mock
    private ServerHttpResponse response;

    @Mock
    private WebSocketHandler wsHandler;

    @Test
    void testBeforeHandshake() {
        WebSocketHandshakeInterceptor interceptor = new WebSocketHandshakeInterceptor();
        Map<String, Object> attributes = new HashMap<>();

        when(request.getURI()).thenReturn(URI.create("ws://localhost:8081/ws-vitals"));

        boolean allowed = interceptor.beforeHandshake(request, response, wsHandler, attributes);

        assertTrue(allowed);
        assertTrue(attributes.containsKey("connectTime"));
    }
}

