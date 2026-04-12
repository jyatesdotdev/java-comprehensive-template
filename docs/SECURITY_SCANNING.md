# Security Scanning

This project integrates four static analysis and vulnerability scanning tools into the Maven build, plus container/dependency scanning in CI. All tools are opt-in via Maven profiles to keep the default build fast.

## Quick Start

```bash
# Full security scan (SpotBugs + Checkstyle + PMD + OWASP Dependency-Check)
mvn verify -Psecurity-scan

# Quick scan without OWASP (faster, good for local dev)
mvn verify -Psecurity-scan-quick

# Run individual tools
mvn spotbugs:check
mvn checkstyle:check
mvn pmd:check pmd:cpd-check
mvn dependency-check:check
```

## Tools Overview

| Tool | Purpose | Config File | Suppression File |
|------|---------|-------------|------------------|
| [SpotBugs + Find Security Bugs](#spotbugs--find-security-bugs) | Security bug detection (SQLi, XSS, crypto) | `spotbugs-exclude.xml` | Same file |
| [Checkstyle](#checkstyle) | Code style + security patterns | `checkstyle.xml` | `checkstyle-suppressions.xml` |
| [PMD](#pmd) | Static analysis for bugs + security | `pmd-ruleset.xml` | Inline annotations |
| [OWASP Dependency-Check](#owasp-dependency-check) | CVE scanning of dependencies | pom.xml properties | `owasp-suppressions.xml` |
| [Trivy](#trivy-container-scanning) | Container image scanning | CI workflow | N/A |
| [Snyk](#snyk) | Dependency vulnerability scanning | CI workflow | N/A |

## Maven Profiles

### `security-scan` — Full Scan

Runs all four Maven-integrated tools bound to the `verify` phase:

- SpotBugs (`check`)
- Checkstyle (`check`)
- PMD (`check` + `cpd-check`)
- OWASP Dependency-Check (`check`)

```bash
mvn verify -Psecurity-scan
```

### `security-scan-quick` — Fast Local Scan

Same as `security-scan` but without OWASP Dependency-Check. Use this for local development — OWASP can take 5–10+ minutes on first run while it downloads the NVD database.

```bash
mvn verify -Psecurity-scan-quick
```

---

## SpotBugs + Find Security Bugs

**What it detects:** SQL injection, XSS, path traversal, weak cryptography, LDAP injection, command injection, deserialization vulnerabilities, and hundreds of other bug patterns.

**Configuration:**
- Effort: `Max` (most thorough analysis)
- Threshold: `Low` (reports all confidence levels)
- Find Security Bugs plugin (`com.h3xstream.findsecbugs:findsecbugs-plugin:1.13.0`) is included as a SpotBugs dependency

**Run standalone:**

```bash
mvn spotbugs:check          # Fail on findings
mvn spotbugs:spotbugs       # Generate report only
mvn spotbugs:gui            # Interactive GUI to browse findings
```

### Suppressing False Positives

Edit `spotbugs-exclude.xml` at the project root:

```xml
<!-- Suppress a specific bug in a specific class -->
<Match>
  <Class name="com.example.MyClass"/>
  <Bug pattern="SQL_INJECTION_JDBC"/>
</Match>

<!-- Suppress a bug pattern across generated code -->
<Match>
  <Source name="~.*Generated.*\.java"/>
</Match>

<!-- Suppress by annotation -->
<Match>
  <Class name="com.example.MyDto"/>
  <Bug pattern="SE_BAD_FIELD"/>
</Match>
```

You can also use the `@SuppressFBWarnings` annotation in code:

```java
@SuppressFBWarnings(value = "SQL_INJECTION_JDBC", justification = "Input is validated upstream")
public void query(String sql) { ... }
```

---

## Checkstyle

**What it enforces:** Naming conventions, import hygiene, method complexity limits, and security-relevant patterns:

- `System.out` / `System.err` usage (should use SLF4J)
- `Runtime.getRuntime().exec()` calls (potential command injection — severity: error)
- Empty catch blocks (swallowed security exceptions)
- `sun.*` internal API imports

**Key thresholds:**

| Check | Limit |
|-------|-------|
| MethodLength | 60 lines |
| ParameterNumber | 7 |
| CyclomaticComplexity | 15 |
| NestedIfDepth | 3 |
| NestedTryDepth | 2 |

**Run standalone:**

```bash
mvn checkstyle:check        # Fail on violations
mvn checkstyle:checkstyle   # Generate report only
```

### Suppressing False Positives

**Inline suppression:**

```java
// CHECKSTYLE:OFF
String longLine = "this line is exempt from all checks";
// CHECKSTYLE:ON
```

**Annotation-based suppression:**

```java
@SuppressWarnings("checkstyle:MemberName")
private String non_standard_name;
```

**File-level suppression** in `checkstyle-suppressions.xml`:

```xml
<!-- Relax checks for test classes -->
<suppress checks="MethodLength|CyclomaticComplexity"
          files=".*Test\.java|.*IT\.java"/>

<!-- Skip generated code entirely -->
<suppress checks=".*" files=".*Generated.*\.java"/>
```

---

## PMD

**What it detects:** Security issues (hardcoded credentials, insecure crypto), error-prone patterns, multithreading bugs, design problems, copy-paste code (CPD), and performance issues.

**Included rule categories:**
- `category/java/security.xml` — full
- `category/java/errorprone.xml` — minus noisy rules (`BeanMembersShouldSerialize`, `DataflowAnomalyAnalysis`, etc.)
- `category/java/bestpractices.xml` — minus overly strict test rules
- `category/java/multithreading.xml` — minus `UseConcurrentHashMap`
- `category/java/design.xml` — with tuned thresholds (CyclomaticComplexity=15, CognitiveComplexity=20)
- `category/java/performance.xml` — full

**Run standalone:**

```bash
mvn pmd:check               # Fail on violations
mvn pmd:pmd                 # Generate report only
mvn pmd:cpd-check           # Fail on copy-paste duplicates
```

### Suppressing False Positives

**Inline suppression:**

```java
String password = getFromVault(); // NOPMD - not a hardcoded credential
```

**Annotation-based suppression:**

```java
@SuppressWarnings("PMD.CyclomaticComplexity")
public void complexButNecessary() { ... }
```

**Ruleset-level exclusion** in `pmd-ruleset.xml`:

```xml
<rule ref="category/java/errorprone.xml">
    <exclude name="DataflowAnomalyAnalysis"/>
</rule>
```

---

## OWASP Dependency-Check

**What it detects:** Known CVEs in project dependencies by matching against the National Vulnerability Database (NVD).

**Configuration:**
- Default CVSS fail threshold: **7** (High + Critical)
- Output formats: HTML, JSON, SARIF
- Output directory: `target/dependency-check/`
- Non-Java analyzers disabled (assembly, node, retire.js) for speed

**Run standalone:**

```bash
# Standard scan
mvn dependency-check:check

# Multi-module aggregate scan
mvn dependency-check:aggregate

# Critical CVEs only (CVSS ≥ 9)
mvn dependency-check:check -Dowasp.failBuildOnCVSS=9

# Report-only mode (never fail)
mvn dependency-check:check -Dowasp.failBuildOnCVSS=11

# With NVD API key (recommended — much faster)
mvn dependency-check:check -DnvdApiKey=YOUR_KEY
```

**NVD API Key:** Without an API key, NVD rate-limits requests and scans are significantly slower. Get a free key at https://nvd.nist.gov/developers/request-an-api-key. Pass it via `-DnvdApiKey=KEY` or configure in `~/.m2/settings.xml`:

```xml
<profiles>
  <profile>
    <id>nvd</id>
    <activation><activeByDefault>true</activeByDefault></activation>
    <properties>
      <nvdApiKey>YOUR_KEY_HERE</nvdApiKey>
    </properties>
  </profile>
</profiles>
```

### Suppressing False Positives

Edit `owasp-suppressions.xml` at the project root:

```xml
<suppress>
    <notes><![CDATA[
        False positive: CVE-XXXX-XXXXX does not apply because we don't use
        the affected feature. Reviewed: 2024-01-15
    ]]></notes>
    <gav regex="true">^com\.example:my-lib:.*$</gav>
    <cve>CVE-XXXX-XXXXX</cve>
</suppress>

<!-- Suppress by CPE for misidentified dependencies -->
<suppress>
    <notes><![CDATA[
        OWASP misidentifies this library. Reviewed: 2024-01-15
    ]]></notes>
    <cpe>cpe:/a:vendor:product</cpe>
</suppress>
```

Always include a `<notes>` block with justification and review date. Suppressions should be reviewed periodically.

---

## CI/CD Integration

The GitHub Actions pipeline (`.github/workflows/ci.yml`) runs security scanning automatically.

### Pipeline Architecture

```
build ──┬── integration-tests
        ├── quality-gates ────────┐
        ├── dependency-scan ──────┼── docker (main only)
        ├── container-scan ───────┘
        └── snyk-scan (optional)
```

### Jobs

| Job | Tools | Trigger | Blocks Deploy |
|-----|-------|---------|---------------|
| `quality-gates` | SpotBugs, Checkstyle, PMD (via `-Psecurity-scan-quick`) | All pushes + PRs | Yes |
| `dependency-scan` | OWASP Dependency-Check | All pushes + PRs | Yes |
| `container-scan` | Trivy | Main pushes + PRs | Yes |
| `snyk-scan` | Snyk | All pushes + PRs | No (optional) |

### SARIF / GitHub Security Tab

Three tools upload SARIF reports to the GitHub Security tab:

| Tool | SARIF File | Category |
|------|-----------|----------|
| OWASP Dependency-Check | `target/dependency-check/dependency-check-report.sarif` | `owasp-dependency-check` |
| Trivy | `trivy-results.sarif` | `trivy-container-scan` |
| Snyk | `snyk-results.sarif` | `snyk-dependency-scan` |

Findings appear under **Security → Code scanning alerts** in the GitHub repository. Each tool has its own category so results are organized separately.

### Required Secrets and Variables

| Name | Type | Required | Purpose |
|------|------|----------|---------|
| `NVD_API_KEY` | Secret | No | Speeds up OWASP NVD database downloads |
| `SNYK_TOKEN` | Secret | No | Enables Snyk scanning (job skips gracefully without it) |
| `OWASP_CVSS_THRESHOLD` | Variable | No | Override CVSS fail threshold (default: 7) |

### Trivy (Container Scanning)

Trivy scans the built Docker image for OS and library vulnerabilities. In CI it:
- Fails on CRITICAL and HIGH severity findings
- Uploads SARIF to GitHub Security tab
- Prints a human-readable table to the Actions log

Trivy runs only in CI — there is no Maven plugin. To run locally:

```bash
# Build the image first
docker build -t myapp -f examples/restful-api/Dockerfile .

# Scan
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
  aquasec/trivy image --severity CRITICAL,HIGH myapp
```

### Snyk

Snyk provides dependency vulnerability scanning with its own vulnerability database (often catches issues before NVD). It requires a `SNYK_TOKEN` secret. The job is `continue-on-error: true` so it won't block the pipeline if Snyk is not configured.

To run locally:

```bash
npm install -g snyk
snyk auth
snyk test --severity-threshold=high
```

---

## Configuring Severity Thresholds

| Tool | How to Configure | Default |
|------|-----------------|---------|
| SpotBugs | `<threshold>` in pom.xml (`Low`/`Medium`/`High`) | Low |
| Checkstyle | `<violationSeverity>` in pom.xml | warning |
| PMD | Rule properties in `pmd-ruleset.xml` | Per-rule defaults |
| OWASP | `-Dowasp.failBuildOnCVSS=N` (0–10) | 7 |
| Trivy | `severity` in CI workflow | CRITICAL,HIGH |
| Snyk | `--severity-threshold` in CI workflow | high |

To make the build stricter, lower thresholds. To make it more lenient (e.g., during initial adoption), raise them:

```bash
# Strictest: fail on any CVE
mvn verify -Psecurity-scan -Dowasp.failBuildOnCVSS=0

# Lenient: only critical CVEs fail the build
mvn verify -Psecurity-scan -Dowasp.failBuildOnCVSS=9
```

---

## Adoption Strategy

When adding security scanning to an existing project, a phased approach avoids overwhelming developers:

1. **Week 1:** Run `mvn verify -Psecurity-scan-quick` in report-only mode. Review findings.
2. **Week 2:** Fix critical/high findings. Add suppressions for accepted false positives.
3. **Week 3:** Enable `security-scan-quick` in CI as a required check.
4. **Week 4:** Enable OWASP Dependency-Check in CI (`dependency-scan` job). Set threshold to 9 (critical only).
5. **Ongoing:** Gradually lower OWASP threshold to 7. Add Trivy/Snyk. Review suppressions quarterly.

---

## File Reference

| File | Purpose |
|------|---------|
| `pom.xml` | Plugin configuration in `<pluginManagement>`, profiles |
| `spotbugs-exclude.xml` | SpotBugs false positive exclusions |
| `checkstyle.xml` | Checkstyle rules |
| `checkstyle-suppressions.xml` | Checkstyle false positive suppressions |
| `pmd-ruleset.xml` | PMD rule categories and exclusions |
| `owasp-suppressions.xml` | OWASP CVE suppressions with justifications |
| `.github/workflows/ci.yml` | CI pipeline with security scanning jobs |


---

## See Also

- [Main README](../README.md) — Project overview and quick start
- [Toolchain](TOOLCHAIN.md) — Tool installation and IDE setup
- [Development Workflow](development-workflow.md) — CI/CD pipeline integration
- [Extending the Template](EXTENDING.md) — Adding quality rules and suppressions
