package com.pega.wschat;

import io.micronaut.http.annotation.*;

import java.util.List;

@Controller("/chat")
public class ChatController {

    private final ChatStreamService streams;

    public ChatController(ChatStreamService streams) {
        this.streams = streams;
    }

    @Get("/{roomId}")
    public List<ChatEvent> history(@PathVariable String roomId) {
        return streams.all(roomId);
    }
}
