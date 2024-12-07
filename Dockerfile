# 사용할 base 이미지 선택
FROM arm64v8/eclipse-temurin:17-jdk-focal AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 빌드 파일 복사
COPY build.gradle settings.gradle ./
COPY src ./src

# 애플리케이션 빌드 (테스트 스킵)
RUN ./gradlew build -x test --no-daemon

# build/libs/ 에 있는 jar 파일을 JAR_FILE 변수에 저장
ARG JAR_FILE=build/libs/*.jar

# JAR_FILE을 app.jar로 복사
COPY ${JAR_FILE} app.jar

# Docker 컨테이너가 시작될 때 /app.jar 실행 
# 애플리케이션 timezone을 대한민국으로 설정
ENTRYPOINT ["java","-jar","-Duser.timezone=Asia/Seoul","/app.jar"]