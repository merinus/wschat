package com.pega.wschat;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.HttpClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import io.micronaut.core.type.Argument;

import java.util.List;
import java.util.Map;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatHistoryRestTest implements io.micronaut.test.support.TestPropertyProvider {

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

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void historyEndpointReturnsStreamedEvents() {
        String room = "t-rest";
        long now = System.currentTimeMillis();
        streams.append(room, "Marcin", "<joined>", now);
        streams.append(room, "Marcin", "Hello",    now+1);
        streams.append(room, "Piotr",   "Hi",       now+2);

        HttpRequest<?> req = HttpRequest.GET("/chat/" + room + "?limit=3");
        HttpResponse<List<ChatEvent>> resp =
                client.toBlocking().exchange(req, Argument.listOf(ChatEvent.class));

        @SuppressWarnings("unchecked")

        List<ChatEvent> body = resp.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(3, body.size());
        Assertions.assertEquals("Marcin", body.get(0).username());
        Assertions.assertEquals("Hello",  body.get(1).text());
        Assertions.assertEquals("Piotr",  body.get(2).username());
    }
}
