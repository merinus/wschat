package com.pega.wschat;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ChatEvent(
        String id,
        long ts,
        String roomId,
        String username,
        String text
) {}