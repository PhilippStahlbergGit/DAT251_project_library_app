# Spring Boot backend
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app

COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon
RUN cp build/libs/*.jar /app/app.jar

# Runtime image 
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=backend-build /app/app.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
