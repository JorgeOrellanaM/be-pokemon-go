# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Gradle wrapper and build scripts first, so dependencies are cached until they change
COPY ../gradlew settings.gradle.kts build.gradle.kts ./
COPY ../gradle gradle
RUN sh ./gradlew dependencies --no-daemon > /dev/null

# Tests are skipped here: contextLoads needs a running PostgreSQL. Run ./gradlew test before building.
COPY ../src src
RUN sh ./gradlew bootJar --no-daemon -x test

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /app/build/libs/pokemon-go-0.0.1-SNAPSHOT.jar app.jar
USER app

ENV SPRING_PROFILES_ACTIVE=qa
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
