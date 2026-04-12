# Development Workflow

Guide for day-to-day development, CI/CD, and quality enforcement.

---

## 1. Adding a New Module

```bash
# 1. Create module directory
mkdir -p examples/my-module/src/main/java/com/example/template/mymodule
mkdir -p examples/my-module/src/test/java/com/example/template/mymodule

# 2. Create module pom.xml
cat > examples/my-module/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.example.template</groupId>
        <artifactId>java-enterprise-template</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <artifactId>template-my-module</artifactId>
    <name>My Module</name>
    <dependencies>
        <!-- Add dependencies here; versions managed by parent POM -->
    </dependencies>
</project>
EOF

# 3. Register in root pom.xml <modules> section
#    <module>examples/my-module</module>

# 4. Add a README.md to the module
```

### Checklist for New Modules

- [ ] `pom.xml` inherits from root parent
- [ ] Registered in root `pom.xml` `<modules>`
- [ ] Package follows `com.example.template.<module>` convention
- [ ] `README.md` with purpose, usage, and key concepts
- [ ] Unit tests in `src/test/java`
- [ ] Integration tests suffixed `*IT.java` (picked up by Failsafe)

---

## 2. Adding a Feature to an Existing Module

1. Create classes in the appropriate package
2. Write unit tests first (TDD encouraged)
3. Run `./mvnw -pl examples/<module> test` to verify
4. Update the module's README if the feature is user-facing
5. Run full build before pushing: `./mvnw clean verify`

---

## 3. CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push to `main`/`develop` and on pull requests.

### Pipeline Stages

```
push/PR
  │
  ├─► build          ── compile + unit tests
  │     │
  │     ├─► integration-tests  ── TestContainers, DB tests
  │     │
  │     └─► quality-gates      ── SpotBugs, Checkstyle, JaCoCo
  │              │
  │              └─► docker    ── build image (main branch only)
```

| Stage | Trigger | What It Does |
|-------|---------|-------------|
| `build` | All pushes/PRs | Compile, unit tests via Surefire |
| `integration-tests` | After build | Integration tests via Failsafe (Docker required) |
| `quality-gates` | After build | Static analysis + coverage checks |
| `docker` | Main branch only | Build container image for REST API |

### Running Locally

```bash
# Full CI equivalent
./mvnw clean verify

# Unit tests only (fast)
./mvnw test

# Integration tests only
./mvnw verify -DskipUTs

# Single module
./mvnw -pl examples/restful-api test
```

---

## 4. Quality Gates

### Static Analysis — SpotBugs

Detects common bug patterns (null dereference, resource leaks, concurrency issues).

Add to root `pom.xml` `<build><plugins>`:

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.4.0</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Medium</threshold>
        <failOnError>true</failOnError>
    </configuration>
</plugin>
```

Run: `./mvnw spotbugs:check`

### Code Style — Checkstyle

Enforces consistent formatting and naming.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
        <violationSeverity>warning</violationSeverity>
        <failOnViolation>true</failOnViolation>
    </configuration>
</plugin>
```

Run: `./mvnw checkstyle:check`

### Code Coverage — JaCoCo

Enforces minimum coverage thresholds.

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
        <execution>
            <id>check</id>
            <phase>verify</phase>
            <goals><goal>check</goal></goals>
            <configuration>
                <rules>
                    <rule>
                        <element>BUNDLE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.70</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Run: `./mvnw verify jacoco:report` — report at `target/site/jacoco/index.html`

### Dependency Vulnerability Scanning — OWASP

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.10</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

Run: `./mvnw dependency-check:check`

---

## 5. Docker

### Building the REST API Image

```bash
# Build the JAR first
./mvnw -pl examples/restful-api -am package -DskipTests

# Build Docker image
docker build -t java-template-api:latest -f examples/restful-api/Dockerfile examples/restful-api

# Run
docker run -p 8080:8080 java-template-api:latest
```

### Dockerfile Highlights

- `eclipse-temurin:21-jre-alpine` — minimal JRE base image
- Non-root `app` user for security
- `-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0` — container-aware memory
- Health check via Spring Actuator

### Docker Compose (for full stack)

```yaml
services:
  api:
    build:
      context: examples/restful-api
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/appdb
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: appdb
      POSTGRES_USER: app
      POSTGRES_PASSWORD: changeme
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U app -d appdb"]
      interval: 5s
      retries: 5
```

---

## 6. Git Workflow

### Branch Strategy

| Branch | Purpose |
|--------|---------|
| `main` | Production-ready code, tagged releases |
| `develop` | Integration branch for features |
| `feature/<name>` | Individual feature work |
| `hotfix/<name>` | Urgent production fixes |

### Commit Convention

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(database): add optimistic locking to Order entity
fix(rest-api): handle null request body in ProductController
docs(patterns): add decision guide for creational patterns
test(testing): add TestContainers PostgreSQL integration test
chore(ci): add JaCoCo coverage threshold
```

### Pull Request Checklist

- [ ] All CI checks pass
- [ ] New code has unit tests
- [ ] Integration tests added for external dependencies
- [ ] README updated if public API changed
- [ ] No new SpotBugs/Checkstyle violations
- [ ] Coverage threshold met (≥70% line coverage)

---

## 7. Release Process

```bash
# 1. Update version
./mvnw versions:set -DnewVersion=1.0.0
./mvnw versions:commit

# 2. Tag and push
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin main --tags

# 3. Build release artifacts
./mvnw clean deploy -DskipTests

# 4. Bump to next snapshot
./mvnw versions:set -DnewVersion=1.1.0-SNAPSHOT
./mvnw versions:commit
```

---

## Quick Reference

| Task | Command |
|------|---------|
| Full build | `./mvnw clean verify` |
| Unit tests only | `./mvnw test` |
| Single module | `./mvnw -pl examples/<module> test` |
| Integration tests | `./mvnw verify -DskipUTs` |
| Static analysis | `./mvnw spotbugs:check checkstyle:check` |
| Coverage report | `./mvnw verify jacoco:report` |
| Dependency scan | `./mvnw dependency-check:check` |
| Docker build | `docker build -t api:latest -f examples/restful-api/Dockerfile examples/restful-api` |
| Run API | `./mvnw -pl examples/restful-api spring-boot:run` |


---

## See Also

- [Main README](../README.md) — Project overview and quick start
- [Toolchain](TOOLCHAIN.md) — Required tools and IDE setup
- [Security Scanning](SECURITY_SCANNING.md) — Quality tool configuration
- [Extending the Template](EXTENDING.md) — Adding modules and features
- [Tutorial](TUTORIAL.md) — New developer walkthrough
