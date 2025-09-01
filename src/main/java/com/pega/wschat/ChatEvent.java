package com.pega.wschat;

public record ChatEvent(
        String id,
        long ts,
        String roomId,
        String username,
        String text
) {}