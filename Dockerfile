FROM arm64v8/gradle:7.6-jdk17 AS builder
WORKDIR /build

COPY build.gradle settings.gradle /build/
COPY gradlew /build/
RUN chmod +x gradlew
RUN ./gradlew build -x test --parallel --continue

COPY . /build
RUN ./gradlew build -x test --parallel

FROM arm64v8/openjdk:17.0-slim
WORKDIR /app

COPY --from=builder /build/build/libs/*-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["java", "-jar", "-Duser.timezone=Asia/Seoul", "/app.jar"]
