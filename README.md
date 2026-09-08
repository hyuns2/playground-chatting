# 💬 playground-chatting

> [!Note]
> **분산 환경을 가정한, 채팅 서비스**

`WebSocket` · `STOMP` · `Pub/Sub` · `Pagination`

---

## 🛠️ 기술 스택

| 분류        | 사용                                 |
|-------------|--------------------------------------|
| Language    | Java 25                              |
| Framework   | Spring Boot 4                        |
| Build       | Gradle 9 · Kotlin DSL                |
| Security    | security-core (Spring Security, JWT) |
| Persistence | MySQL 9, Spring Data JPA             |
| Pub/Sub     | Redis 8, Spring Data Redis           |
| Messaging   | Kafka 4, Spring Kafka                |
| Infra       | Docker · Docker Compose              |

---

## 🏗️ 프로젝트 구조 및 설명

```
playground-chatting/
├─ chat-service/                # 채팅 서비스
├─ docker-compose.yml
└─ .env
```

* 채팅 서비스
  - 채팅방 생성 및 offset 기반 조회
  - 채팅 메시지 전송 및 cursor 기반 조회
* 공통 보안 모듈 구축 및 적용 -> [security-core](https://github.com/hyuns2/playground-auth/packages/3221307)
* [기술적 의사결정 과정](https://hyuns2.notion.site/playground-chatting-30f2ac90a22f80ddb3b7ed889134f8c7)

---

## 🚀 실행 방법

### ▶️ Run

```bash
docker compose up -d
```

### ⏹️  Stop

```bash
docker compose down
```
