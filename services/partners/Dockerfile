FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copia arquivos de configuração do Gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle

# Copia módulos necessários
COPY shared ./shared
COPY services/partners ./services/partners

# Build do módulo Partners
RUN gradle :services:partners:build -x test --no-daemon

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/services/partners/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
