package com.pega.wschat;

import io.lettuce.core.Limit;
import io.lettuce.core.Range;
import io.lettuce.core.RedisException;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ChatStreamService {

    private final StatefulRedisConnection<String, String> connection;

    @Value("${chat.stream.prefix:chat:room:}")
    String streamPrefix;

    @Value("${chat.stream.maxlen:0}")
    long maxLen;

    public ChatStreamService(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
    }

    private String key(String roomId) {
        return streamPrefix + roomId;
    }


    public String append(String roomId, String username, String text, long ts) {
        Map<String, String> body = new LinkedHashMap<>(4);
        body.put("ts", Long.toString(ts));
        body.put("username", username);
        body.put("text", text);

        RedisCommands<String, String> cmd = connection.sync();

        if (maxLen > 0) {
            return cmd.xadd(key(roomId), new XAddArgs().approximateTrimming().maxlen(maxLen), body);
        } else {
            return cmd.xadd(key(roomId), body);
        }
    }


    public List<ChatEvent> all(String roomId) {
        RedisCommands<String, String> cmd = connection.sync();
        List<StreamMessage<String, String>> entries =
                cmd.xrange(key(roomId), Range.unbounded(), Limit.unlimited());

        return entries.stream().map(e -> mapToEvent(roomId, e)).toList();
    }


    public List<ChatEvent> range(String roomId, String startId, String endId, long limit) {
        RedisCommands<String, String> cmd = connection.sync();
        List<StreamMessage<String, String>> entries = (limit > 0)
                ? cmd.xrange(key(roomId), Range.create(startId, endId), Limit.create(0, limit))
                : cmd.xrange(key(roomId), Range.create(startId, endId));
        return entries.stream().map(e -> mapToEvent(roomId, e)).toList();
    }

    private static ChatEvent mapToEvent(String roomId, StreamMessage<String, String> e) {
        String tsStr = e.getBody().getOrDefault("ts", "0");
        String username = e.getBody().getOrDefault("username", "");
        String text = e.getBody().getOrDefault("text", "");
        long ts = 0L;
        try { ts = Long.parseLong(tsStr); } catch (NumberFormatException ignored) {}
        return new ChatEvent(e.getId(), ts, roomId, username, text);
    }


    public boolean ping() {
        try {
            return "PONG".equalsIgnoreCase(connection.sync().ping());
        } catch (RedisException ex) {
            return false;
        }
    }
}
