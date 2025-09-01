package com.pega.wschat;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class RedisConfig {
    @Singleton
    StatefulRedisConnection<String, String> stringConnection(RedisClient client) {
        return client.connect(StringCodec.UTF8);
    }
}
