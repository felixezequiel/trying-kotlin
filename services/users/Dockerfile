FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copia arquivos de configuração do Gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copia módulos necessários
COPY shared ./shared
COPY services/users ./services/users

# Build do módulo Users
RUN gradle :services:users:build -x test --no-daemon

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/services/users/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
