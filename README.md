# ChatApp Backend

A real-time chat application backend built with Spring Boot, featuring WebSocket communication, Kafka event streaming, and Keycloak authentication.

---

## Features

* ✨ Real-time messaging via WebSocket
* 📊 Event-driven architecture with Kafka
* 🔐 JWT authentication via Keycloak
* 📷 File upload/download for media and profile pictures
* 📊 PostgreSQL database with JPA/Hibernate
* 🚨 Docker containerization for easy deployment
* 📄 RESTful APIs with OpenAPI documentation

---

## Architecture Overview

* **Controllers**: REST endpoints returning DTOs with byte\[] media data
* **Services**: Business logic with file handling and Kafka integration
* **Entities**: JPA entities storing file paths (not byte data)
* **DTOs**: Response objects that convert file paths to byte arrays
* **WebSocket**: Real-time messaging with JWT authentication headers
* **Kafka**: Event streaming for messages, reactions, and delivery status

---

## Quick Start with Docker

### Prerequisites

* Docker + Docker Compose installed
* At least 4GB RAM allocated to Docker

### 1. Clone and Navigate

```bash
cd /path/to/chatapp/backend
```

### 2. Start All Services

```bash
docker-compose up -d
```

This brings up:

* PostgreSQL (5432)
* Keycloak (9090)
* Kafka + Kafka UI (9092, 8088)
* ChatApp Backend (8080)

### 3. Verify Startup

```bash
docker-compose ps
```

```bash
docker-compose logs -f chatapp
```

### 4. Open Swagger

* Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* OpenAPI Spec: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## WebSocket Support

* **Endpoint**: `ws://localhost:8080/ws`
* **Protocol**: STOMP (SockJS supported)
* **JWT**: Pass token via `Authorization: Bearer <token>`

### Subscribed Topics:

* `/topic/chat/{chatId}`
* `/topic/message/{messageId}/reactions`
* `/topic/message/{messageId}/delivery`
* `/topic/chat/{chatId}/typing`

---

## Postman Setup

### 1. Import Collection

* Use `ChatApp_API.postman_collection.json`

### 2. Create Keycloak Client

* Client ID: `postman-client`
* Enable: Standard Flow + Direct Access Grants
* Add User: `testuser` with password `password`

### 3. Fetch Token

* Use `Authentication > Get Access Token`
* Token auto-saved to collection variable

---

## API Overview

### Auth

* Login via Keycloak
* All secured endpoints need Bearer JWT

### Users

* `GET /api/users/profile`
* `PUT /api/users/profile`
* `GET /api/users/search?query=...`

### Chats

* `GET /api/chats`
* `POST /api/chats/individual`
* `POST /api/chats/group`

### Messages

* `GET /api/messages/chat/{chatId}`
* `POST /api/messages/chat/{chatId}/send`
* `PUT /api/messages/{messageId}/read`
* `POST/DELETE /api/messages/{messageId}/reactions`

---

## File Storage

| Type             | Path                 |
| ---------------- | -------------------- |
| Profile Pictures | `/app/root/profile/` |
| Chat Images      | `/app/root/picture/` |
| Chat Videos      | `/app/root/video/`   |

Media files are returned as **byte arrays** in DTO responses.

---

## Kafka Topics

| Topic               | Purpose                       |
| ------------------- | ----------------------------- |
| `chat-messages`     | New messages from users       |
| `delivery-status`   | Delivery/read acknowledgments |
| `message-reactions` | Emoji reactions               |

Monitor with [Kafka UI](http://localhost:8088).

---

## Environment Variables

| Variable                  | Default                                                                                        |
| ------------------------- | ---------------------------------------------------------------------------------------------- |
| DB\_HOST                  | postgres\_sql                                                                                  |
| DB\_PORT                  | 5432                                                                                           |
| DB\_NAME                  | chatapp                                                                                        |
| DB\_USERNAME              | admin                                                                                          |
| DB\_PASSWORD              | password                                                                                       |
| KEYCLOAK\_ISSUER\_URI     | [http://localhost:9090/realms/chatspot-chatapp](http://localhost:9090/realms/chatspot-chatapp) |
| KAFKA\_BOOTSTRAP\_SERVERS | kafka\_broker:9092                                                                             |
| MEDIA\_PROFILE\_PATH      | /app/root/profile                                                                              |
| MEDIA\_PICTURE\_PATH      | /app/root/picture                                                                              |
| MEDIA\_VIDEO\_PATH        | /app/root/video                                                                                |

---

## Development

### Option A: Docker Only

```bash
docker-compose up --build
```

### Option B: Local Dev + Docker Infra

```bash
docker-compose up -d postgres keycloak kafka kafka-ui
./mvnw spring-boot:run
```

---

## Useful Docker Commands

```bash
docker-compose logs -f chatapp

docker-compose restart chatapp

docker-compose down && docker-compose up -d

docker exec -it postgres_sql psql -U admin -d chatapp
```

--
