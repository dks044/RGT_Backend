# Gradle을 사용하여 애플리케이션을 빌드
FROM gradle:7.6-jdk17-alpine as builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 빌드 파일 복사
COPY build.gradle settings.gradle ./
COPY src ./src

# 애플리케이션 빌드
RUN gradle build --no-daemon

# Java 런타임 이미지로 전환
FROM eclipse-temurin:17-jdk-alpine

# JAR 파일 복사
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder ${JAR_FILE} app.jar

# 애플리케이션 실행 및 타임존 설정
ENTRYPOINT ["java","-jar","-Duser.timezone=Asia/Seoul","/app.jar"]
