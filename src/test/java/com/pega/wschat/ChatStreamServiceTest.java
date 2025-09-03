package com.pega.wschat;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatStreamServiceTest implements io.micronaut.test.support.TestPropertyProvider {

    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);


    @Override
    public Map<String, String> getProperties() {
        if (!redis.isRunning()) {
            redis.start();
        }
        int port = redis.getFirstMappedPort();
        return Map.of("redis.uri", "redis://localhost:" + port);
    }

    @Inject
    ChatStreamService streams;

    @Test
    void appendAndReadAll() {
        String room = "t-unit";
        long t1 = System.currentTimeMillis();
        streams.append(room, "Alice", "Hej!", t1);
        streams.append(room, "Bob", "Cześć!", t1 + 1);

        List<ChatEvent> all = streams.all(room);
        Assertions.assertTrue(all.size() >= 2);
        Assertions.assertEquals("Alice", all.get(0).username());
        Assertions.assertEquals("Bob",   all.get(1).username());
    }

    @Test
    void rangeWithLimit() {
        String room = "t-range";
        streams.append(room, "A", "1", System.currentTimeMillis());
        streams.append(room, "B", "2", System.currentTimeMillis());
        streams.append(room, "C", "3", System.currentTimeMillis());

        List<ChatEvent> firstTwo = streams.range(room, "-", "+", 2);
        Assertions.assertEquals(2, firstTwo.size());
        Assertions.assertEquals("A", firstTwo.get(0).username());
        Assertions.assertEquals("B", firstTwo.get(1).username());
    }
}
