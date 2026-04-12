# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-alpine AS build

RUN apk add --no-cache maven

WORKDIR /workspace

# Copy POMs first for dependency caching
COPY pom.xml .
COPY examples/restful-api/pom.xml examples/restful-api/
COPY examples/database/pom.xml examples/database/
COPY examples/hpc/pom.xml examples/hpc/
COPY examples/etl/pom.xml examples/etl/
COPY examples/systems/pom.xml examples/systems/
COPY examples/patterns/pom.xml examples/patterns/
COPY examples/simulation/pom.xml examples/simulation/
COPY examples/testing/pom.xml examples/testing/

RUN mvn dependency:go-offline -pl examples/restful-api -am -B -q

# Copy source and build
COPY . .
RUN mvn package -pl examples/restful-api -am -B -q -DskipTests \
    && mv examples/restful-api/target/template-restful-api-*.jar /app.jar

# ---- Stage 2: Extract layered jar ----
FROM eclipse-temurin:21-jre-alpine AS extract

COPY --from=build /app.jar /app.jar
RUN java -Djarmode=tools -jar /app.jar extract --layers --launcher --destination /extracted

# ---- Stage 3: Runtime ----
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="team@example.com" \
      org.opencontainers.image.title="java-enterprise-template" \
      org.opencontainers.image.description="Spring Boot application" \
      org.opencontainers.image.source="https://github.com/example/java-enterprise-template"

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=extract /extracted/dependencies/ ./
COPY --from=extract /extracted/spring-boot-loader/ ./
COPY --from=extract /extracted/snapshot-dependencies/ ./
COPY --from=extract /extracted/application/ ./

RUN chown -R app:app /app
USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+ExitOnOutOfMemoryError", \
  "org.springframework.boot.loader.launch.JarLauncher"]
