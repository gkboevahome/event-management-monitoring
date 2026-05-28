# Stage 1: Compile
FROM gradle:8-jdk17 AS builder
WORKDIR /app

# compile config files for caching of the dependencies
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY backend/build.gradle ./backend/

# compile the end code of the backend
COPY ./backend/src ./backend/src

RUN mkdir frontend

RUN ./gradlew :backend:bootJar

# Stage 2 : Start of the app
FROM openjdk:17.0.2-jdk
WORKDIR /app

# Copy and compile the code from the build in the previous stage
COPY --from=builder /app/backend/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
