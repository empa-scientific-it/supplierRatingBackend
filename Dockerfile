# --- Stage 1: Build ---
FROM gradle:9.2.1-jdk25-alpine AS builder
WORKDIR /app
USER root
RUN chown -R gradle:gradle /app
USER gradle
COPY --chown=gradle:gradle build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon
COPY --chown=gradle:gradle src ./src
RUN gradle bootJar -x test --no-daemon

# --- Stage 2: Run ---
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app
COPY --from=builder /app/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]