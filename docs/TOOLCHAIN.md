# Toolchain

Required tools, installation, and IDE setup for the Java Enterprise Template.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
  - [macOS](#macos)
  - [Linux (Debian/Ubuntu)](#linux-debianubuntu)
  - [Windows](#windows)
- [Verify Installation](#verify-installation)
- [IDE Setup](#ide-setup)
  - [IntelliJ IDEA](#intellij-idea)
  - [VS Code](#vs-code)
- [Maven Wrapper](#maven-wrapper)
- [Quality Tools](#quality-tools)
- [Docker](#docker)
- [Related Docs](#related-docs)

---

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 21+ (Temurin recommended) | Build & runtime — source compatibility targets Java 17 |
| Maven | 3.9+ (wrapper included) | Build system — `./mvnw` is provided, no global install needed |
| Docker | 24+ | Container builds, Testcontainers for integration tests |
| Git | 2.x | Version control |

The project uses **Java 17 language features** (`maven.compiler.source=17`) but builds and runs on **JDK 21** in CI and Docker. Any JDK ≥ 17 works; JDK 21 is recommended for consistency with CI.

### Quality Tools (Maven-managed)

These run as Maven plugins — no separate installation required:

| Tool | Plugin Version | Purpose |
|------|---------------|---------|
| SpotBugs | 4.8.4.0 | Bug detection |
| Find Security Bugs | 1.13.0 | Security-focused static analysis |
| Checkstyle | 3.3.1 (engine 10.14.2) | Code style enforcement |
| PMD | 3.22.0 | Static analysis |
| OWASP Dependency-Check | 9.1.0 | CVE scanning of dependencies |
| JaCoCo | 0.8.12 | Code coverage |
| Spotless | 2.43.0 | Code formatting (Google Java Format) |

---

## Installation

### macOS

```bash
# JDK 21 (Eclipse Temurin)
brew install --cask temurin@21

# Docker Desktop
brew install --cask docker

# Git (usually pre-installed with Xcode CLI tools)
xcode-select --install
```

### Linux (Debian/Ubuntu)

```bash
# JDK 21 (Eclipse Temurin via Adoptium APT repo)
sudo apt-get install -y wget apt-transport-https gpg
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | \
  sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/adoptium.gpg
echo "deb https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | \
  sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt-get update && sudo apt-get install -y temurin-21-jdk

# Docker
sudo apt-get install -y docker.io
sudo usermod -aG docker $USER  # log out and back in

# Git
sudo apt-get install -y git
```

### Windows

```powershell
# Using winget
winget install EclipseAdoptium.Temurin.21.JDK
winget install Docker.DockerDesktop
winget install Git.Git
```

Or download installers from:
- JDK: https://adoptium.net/temurin/releases/?version=21
- Docker: https://www.docker.com/products/docker-desktop/
- Git: https://git-scm.com/download/win

---

## Verify Installation

```bash
# All of these should succeed before working with the project
java -version          # Expected: openjdk 21.x.x
./mvnw --version       # Expected: Apache Maven 3.9.x
docker --version       # Expected: Docker 24.x+
git --version          # Expected: git 2.x
```

Quick build test:

```bash
./mvnw -B clean verify -DskipTests
```

---

## IDE Setup

### IntelliJ IDEA

1. **Open project**: File → Open → select the root `pom.xml` → "Open as Project"
2. **Set JDK**: File → Project Structure → Project SDK → select Temurin 21
3. **Import Maven**: IntelliJ auto-detects the POM. If not, right-click `pom.xml` → "Add as Maven Project"
4. **Code style**: Install the [Checkstyle-IDEA](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea) plugin
   - Settings → Tools → Checkstyle → add `checkstyle.xml` from the project root
5. **Recommended plugins**:
   - Checkstyle-IDEA — real-time Checkstyle feedback
   - SpotBugs — in-IDE bug detection
   - MapStruct Support — for modules using MapStruct
6. **Run configurations**: Spring Boot modules (e.g., `restful-api`) can be run directly via the `Application` main class

### VS Code

1. **Required extension**: [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) (includes Language Support, Debugger, Maven, Test Runner)
2. **Open project**: File → Open Folder → select the project root
3. **Recommended extensions** (add to `.vscode/extensions.json`):
   ```json
   {
     "recommendations": [
       "vscjava.vscode-java-pack",
       "vscjava.vscode-maven",
       "vmware.vscode-spring-boot",
       "redhat.java",
       "SonarSource.sonarlint-vscode"
     ]
   }
   ```
4. **Settings**: Add to `.vscode/settings.json`:
   ```json
   {
     "java.configuration.updateBuildConfiguration": "automatic",
     "java.compile.nullAnalysis.mode": "automatic"
   }
   ```

---

## Maven Wrapper

The project includes a Maven Wrapper (`mvnw` / `mvnw.cmd`), so a global Maven install is optional. Always use the wrapper to ensure consistent Maven versions:

```bash
./mvnw clean verify              # Build + unit tests
./mvnw verify -Psecurity-scan    # Full quality + security scan
./mvnw verify -Pformat           # Auto-format code with Spotless
```

If you need to update the wrapper version:

```bash
mvn wrapper:wrapper -Dmaven=3.9.9
```

---

## Quality Tools

All quality tools are configured in the root `pom.xml` and activated via Maven profiles. No separate installation is needed.

| Command | What it runs |
|---------|-------------|
| `./mvnw verify` | Compile + unit tests + JaCoCo coverage |
| `./mvnw verify -Psecurity-scan-quick` | + SpotBugs + Checkstyle + PMD |
| `./mvnw verify -Psecurity-scan` | + all above + OWASP Dependency-Check |
| `./mvnw verify -Pformat` | Auto-format with Google Java Format |
| `./mvnw verify -Pintegration-tests` | Run integration tests (Failsafe) |

Configuration files in the project root:

| File | Tool |
|------|------|
| `checkstyle.xml` | Checkstyle rules |
| `checkstyle-suppressions.xml` | Checkstyle suppressions |
| `pmd-ruleset.xml` | PMD rules |
| `spotbugs-exclude.xml` | SpotBugs exclusions |
| `owasp-suppressions.xml` | OWASP false-positive suppressions |

For detailed usage, suppression guides, and CI integration, see [SECURITY_SCANNING.md](SECURITY_SCANNING.md).

---

## Docker

The `restful-api` module includes a Dockerfile using `eclipse-temurin:21-jre-alpine`:

```bash
# Build the JAR first
./mvnw package -pl examples/restful-api -am -DskipTests

# Build the Docker image
docker build -t java-template-api:latest -f examples/restful-api/Dockerfile examples/restful-api

# Run
docker run -p 8080:8080 java-template-api:latest
```

The image runs as a non-root user and includes a health check on `/actuator/health`.

Docker is also required for **Testcontainers** (used in integration tests for database and other modules). Ensure Docker is running before executing integration tests.

---

## Related Docs

- [README](../README.md) — Project overview and quick start
- [Development Workflow](development-workflow.md) — Day-to-day development, CI/CD, adding modules
- [Security Scanning](SECURITY_SCANNING.md) — Detailed security tool configuration and usage
- [Architecture Patterns](architecture-patterns.md) — Template structure and design patterns
- [Best Practices](best-practices.md) — Coding standards and conventions
