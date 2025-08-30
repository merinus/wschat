package com.pega.wschat;

public record WsMessage(
        String type,     // "welcome" | "join" | "message" | "leave" | "error"
        String roomId,
        String username,
        String text,
        long ts
) {
}
