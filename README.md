# 💬 playground-chatting

> [!Note]
> **MSA 및 이벤트 기반 비동기 구조 학습 목적의, 채팅 시스템**

`WebSocket` · `STOMP` · `Pub/Sub` · `Pagination`

---

## 🛠️ 기술 스택

| 분류         | 사용                                     |
|--------------|------------------------------------------|
| Language     | Java 17                                  |
| Framework    | Spring Boot 3, Spring Cloud Gateway      |
| Build · Test | Gradle 8, JUnit 5                        |
| Security     | Spring Security, JWT                     |
| Persistence  | MySQL 9, Spring Data JPA                 |
| Cache        | Redis 7, Spring Data Redis               |
| Infra        | Docker · Docker Compose, Github Packages |

---

## 🏗️ 프로젝트 구조 및 설명

```
playground-chatting/
├─ auth-service/            # 회원 인증 서비스
├─ user-service/            # 회원 프로필 서비스
├─ chat-service/            # 채팅 서비스
├─ gateway/                 # 인증 담당
├─ security-core/           # 인가 담당
├─ docker-compose.yml
└─ .local_env
```

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
