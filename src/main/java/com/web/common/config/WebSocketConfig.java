package com.web.common.config;

import com.web.websocket.ClipboardWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private ClipboardWebSocketHandler clipboardWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(clipboardWebSocketHandler, "/ws/clipboard")
                .setAllowedOrigins("https://mypad.kr", "http://localhost:3000", "http://124.53.139.229:3000");
    }
}
