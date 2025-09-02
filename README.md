# WS Chat (Micronaut + Redis Streams)

## Running redis container:
- docker run -d --name redis -p 6379:6379 redis:7-alpine

## Ping:
- docker exec -it redis redis-cli ping #expecting: PONG

## Checking port listening
- Test-NetConnection -ComputerName localhost -Port 6379

## Chat realtime backend:
- WebSocket: `ws://localhost:8080/{room_id}?username=...`
- Record of history: **Redis Streams** (`chat:room:{room_id}`)
- REST: `GET /chat/{room_id}` returns history
- Dockerfile + **docker compose** (app + redis)
- Health/metrics: `/health`, `/info` (Micronaut Management)

## Requirements
- **JDK 21** (LTS)
- Docker Desktop (WSL2 for Windows)
- (Dev) Redis locally via Docker: `docker run -d --name redis -p 6379:6379 redis:7-alpine`

## Configuration
Configuration in `application.yml` / `application.properties`:
```yaml
micronaut:
  application:
    name: wschat

redis:
  uri: ${REDIS_URI:`redis://localhost:6379`}

chat:
  maxMessageBytes: 4096
  stream:
    prefix: chat:room:
    maxlen: 0

## Micronaut 4.9.3 Documentation

- [User Guide](https://docs.micronaut.io/4.9.3/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.9.3/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.9.3/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
- [Shadow Gradle Plugin](https://gradleup.com/shadow/)
## Feature json-schema-validation documentation

- [Micronaut JSON Schema Validation documentation](https://micronaut-projects.github.io/micronaut-json-schema/latest/guide/index.html#validation)


## Feature serialization-jsonp documentation

- [Micronaut Serialization JSON-B and JSON-P documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)


## Feature validation documentation

- [Micronaut Validation documentation](https://micronaut-projects.github.io/micronaut-validation/latest/guide/)


## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)


## Feature management documentation

- [Micronaut Management documentation](https://docs.micronaut.io/latest/guide/index.html#management)


## Feature json-schema documentation

- [Micronaut JSON Schema documentation](https://micronaut-projects.github.io/micronaut-json-schema/latest/guide/)

- [https://json-schema.org/learn/getting-started-step-by-step](https://json-schema.org/learn/getting-started-step-by-step)


## Feature websocket documentation

- [Micronaut Websocket documentation](https://docs.micronaut.io/latest/guide/#websocket)


## Feature json-schema-generator documentation

- [Micronaut JSON Schema Generator documentation](https://micronaut-projects.github.io/micronaut-json-schema/latest/guide/index.html#generator)


## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)


## Feature jackson-xml documentation

- [Micronaut Jackson XML serialization/deserialization documentation](https://micronaut-projects.github.io/micronaut-jackson-xml/latest/guide/index.html)

- [https://github.com/FasterXML/jackson-dataformat-xml](https://github.com/FasterXML/jackson-dataformat-xml)


