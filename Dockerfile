FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
# gradlew из клона на Windows может прийти с CRLF и не запуститься в Linux
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

COPY src src

# cache mount: ~/.gradle сохраняется между сборками, зависимости не качаются заново.
# bootJar сам запускает openApiGenerate -> compileJava
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

# ---------- Этап 2: запуск ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=build /workspace/build/libs/app.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]