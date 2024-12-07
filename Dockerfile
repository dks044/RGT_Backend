FROM gradle:7.6-jdk17-alpine as builder
WORKDIR /build

# 그래들 파일이 변경되었을 때만 새롭게 의존패키지 다운로드 받게함.
COPY build.gradle settings.gradle /build/
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

# 빌더 이미지에서 애플리케이션 빌드
COPY . /build
RUN gradle build -x test --parallel

# APP
FROM openjdk:17.0-slim
WORKDIR /app

# 빌더 이미지에서 jar 파일만 복사
COPY --from=builder /build/build/libs/*-SNAPSHOT.jar ./app.jar


# Docker 컨테이너가 시작될 때 /app.jar 실행 
# 애플리케이션 timezone을 대한민국으로 설정
ENTRYPOINT ["java","-jar","-Duser.timezone=Asia/Seoul","/app.jar"]