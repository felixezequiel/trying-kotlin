FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copia arquivos de configuração do Gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copia módulos necessários
COPY shared ./shared
COPY services/events ./services/events

# Build do módulo Events
RUN gradle :services:events:build -x test --no-daemon

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/services/events/build/libs/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
