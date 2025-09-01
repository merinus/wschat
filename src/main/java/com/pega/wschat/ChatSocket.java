package com.pega.wschat;

import com.pega.wschat.ChatStreamService;
import io.micronaut.websocket.CloseReason;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.context.annotation.Value;
import io.micronaut.json.JsonMapper;
import io.micronaut.websocket.WebSocketBroadcaster;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.annotation.*;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@ServerWebSocket("/{room_id}")
@Singleton



public class ChatSocket {

    private static final Pattern USER_RE = Pattern.compile("^[A-Za-z0-9_-]{1,32}$");
    private static final Pattern ROOM_RE = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    @Value("${chat.maxMessageBytes:4096}")
    int maxMessageBytes;

    private final WebSocketBroadcaster broadcaster;
    private final JsonMapper json;
    private final ChatStreamService streams;

    private final ConcurrentHashMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public ChatSocket(WebSocketBroadcaster broadcaster, JsonMapper json, ChatStreamService streams) {
        this.broadcaster = broadcaster;
        this.json = json;
        this.streams = streams;
    }

    @OnOpen
    public void onOpen(WebSocketSession session,
                       @PathVariable("room_id") String roomId,
                       @QueryValue("username") String username) {
        if (!ROOM_RE.matcher(roomId).matches() || !USER_RE.matcher(username).matches()) {
            sendTo(session, new WsMessage("error", roomId, username, "invalid room_id or username", now()));
            session.close(new CloseReason(CloseReason.POLICY_VIOLATION.getCode(),
                          "invalid room_id or username"));

            return;
        }
        session.put("roomId", roomId);
        session.put("username", username);
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

        streams.append(roomId, username, "<joined>", now());
        sendTo(session, new WsMessage("welcome", roomId, username, "connected", now()));
        broadcastToRoom(roomId, new WsMessage("join", roomId, username, "<joined>", now()));
    }

    @OnMessage
    public void onMessage(byte[] payload,
                          WebSocketSession session,
                          @PathVariable("room_id") String roomId,
                          @QueryValue String username) {
        if (!session.isOpen()) return;

        if (payload == null || payload.length == 0) return;
        if (payload.length > maxMessageBytes) {
            sendJson(session, new WsMessage("error", roomId, username, "invalid_or_too_big", now()));
            session.close(CloseReason.MESSAGE_TO_BIG);
            return;
        }

        final String msg = new String(payload, java.nio.charset.StandardCharsets.UTF_8).trim();
        if (msg.isBlank()) return;

        streams.append(roomId, username, msg, now());
        broadcastToRoom(roomId, new WsMessage("message", roomId, username, msg, now()));
    }

    @OnClose
    public void onClose(WebSocketSession session,
                        @PathVariable("room_id") String roomId,
                        @QueryValue("username") String username) {
        var set = rooms.get(roomId);
        if (set != null) {
            set.remove(session);
            if (set.isEmpty()) rooms.remove(roomId);
        }
        streams.append(roomId, username, "<left>", now());
        broadcastToRoom(roomId, new WsMessage("leave", roomId, username, "<left>", now()));
    }

    @OnError
    public void onError(WebSocketSession session, Throwable error) {
        String roomId = session.get("roomId", String.class, null);
        String username = session.get("username", String.class, null);
        broadcastToRoom(roomId, new WsMessage("error", roomId, username, "session_error", now()));
    }

    // ===== helpers =====
    private void broadcastToRoom(String roomId, WsMessage message) {
        if (roomId == null) return;
        Predicate<WebSocketSession> sameRoom =
                s -> Objects.equals(roomId, s.get("roomId", String.class, null));
        try {
            broadcaster.broadcastSync(json.writeValueAsString(message), sameRoom);
        } catch (IOException ignored) { }
    }

    private void sendTo(WebSocketSession session, WsMessage message) {
        try {
            session.sendSync(json.writeValueAsString(message));
        } catch (IOException ignored) { }
    }

    private static long now() { return Instant.now().toEpochMilli(); }

    private void sendJson(WebSocketSession session, WsMessage message) {
        try {
            session.sendSync(json.writeValueAsString(message));
        } catch (IOException ignored) {
        }
    }

}
