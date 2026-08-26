# 빌드
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /build

# - 의존성 먼저 복사 (캐싱 최적화)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon

COPY . .
RUN ./gradlew build -x test --no-daemon

# 실행
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /build/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
