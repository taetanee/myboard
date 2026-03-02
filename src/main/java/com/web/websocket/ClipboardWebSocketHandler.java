package com.web.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ClipboardWebSocketHandler extends TextWebSocketHandler {

    // randomWord → 접속 중인 세션 목록
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    // sessionId → randomWord
    private final Map<String, String> sessionRooms = new ConcurrentHashMap<>();

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private String extractRandomWord(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) return null;
        for (String param : uri.getQuery().split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && "randomWord".equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String randomWord = extractRandomWord(session);
        if (randomWord == null || randomWord.isEmpty()) {
            try { session.close(CloseStatus.BAD_DATA); } catch (Exception ignored) {}
            return;
        }
        rooms.computeIfAbsent(randomWord, k -> ConcurrentHashMap.newKeySet()).add(session);
        sessionRooms.put(session.getId(), randomWord);
        log.info("WS 연결: {} ({}명)", randomWord, rooms.get(randomWord).size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String randomWord = sessionRooms.get(session.getId());
        if (randomWord == null) return;

        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        String type = (String) payload.get("type");

        if ("TEXT_UPDATE".equals(type)) {
            String content = (String) payload.getOrDefault("content", "");
            // Redis에 저장
            redisUtil.setValues(randomWord, content);
            // 같은 방의 다른 클라이언트에 브로드캐스트
            broadcastToOthers(randomWord, session, message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String randomWord = sessionRooms.remove(session.getId());
        if (randomWord != null) {
            Set<WebSocketSession> sessions = rooms.get(randomWord);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) rooms.remove(randomWord);
            }
            log.info("WS 종료: {} (남은 {}명)", randomWord, rooms.getOrDefault(randomWord, ConcurrentHashMap.newKeySet()).size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("WS 오류: {}", exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    private void broadcastToOthers(String randomWord, WebSocketSession sender, String payload) {
        Set<WebSocketSession> sessions = rooms.get(randomWord);
        if (sessions == null) return;
        TextMessage msg = new TextMessage(payload);
        for (WebSocketSession s : sessions) {
            if (s.isOpen() && !s.getId().equals(sender.getId())) {
                try { s.sendMessage(msg); } catch (Exception e) {
                    log.warn("브로드캐스트 실패: {}", e.getMessage());
                }
            }
        }
    }

    // 파일 목록 변경 시 REST 컨트롤러에서 호출
    public void broadcastFileList(String randomWord, List<String> files) {
        Set<WebSocketSession> sessions = rooms.get(randomWord);
        if (sessions == null || sessions.isEmpty()) return;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "FILE_LIST");
            payload.put("files", files);
            TextMessage msg = new TextMessage(objectMapper.writeValueAsString(payload));
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    try { s.sendMessage(msg); } catch (Exception e) {
                        log.warn("파일 목록 브로드캐스트 실패: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("파일 목록 직렬화 실패", e);
        }
    }
}
