# Testing Strategy

Comprehensive testing examples covering unit, integration, and architectural testing.

## Test Pyramid

```
        ╱  E2E  ╲          Few — Selenium, Playwright (see guide below)
       ╱─────────╲
      ╱ Integration╲       Some — TestContainers, REST Assured (*IT.java)
     ╱───────────────╲
    ╱   Unit Tests     ╲   Many — JUnit 5, Mockito, AssertJ (*Test.java)
   ╱─────────────────────╲
  ╱  Architecture Tests    ╲  Always — ArchUnit (runs with unit tests)
 ╱───────────────────────────╲
```

## Module Contents

| File | Framework | Type |
|------|-----------|------|
| `JUnit5FeaturesTest` | JUnit 5 + AssertJ | Unit |
| `MockitoFeaturesTest` | Mockito | Unit |
| `ArchitectureRulesTest` | ArchUnit | Unit |
| `PostgresContainerIT` | TestContainers | Integration |
| `RestAssuredIT` | REST Assured + TestContainers | Integration |

## How to Run

```bash
# Unit tests only (fast, no Docker needed)
./mvnw test -pl examples/testing

# Integration tests (requires Docker)
./mvnw verify -pl examples/testing -Pintegration-tests

# Run specific test class
./mvnw test -pl examples/testing -Dtest=JUnit5FeaturesTest

# Run tagged tests
./mvnw test -pl examples/testing -Dgroups=slow
```

JaCoCo is skipped in this module (`jacoco.skip=true`). Coverage for the runnable app is at `app/target/site/jacoco/index.html` after `./mvnw -pl app verify`.

## Frameworks

### JUnit 5

The standard Java testing framework. Key features demonstrated:

- **`@DisplayName`** — readable test names in reports
- **`@Nested`** — group related tests in inner classes
- **`@ParameterizedTest`** — run same test with different inputs (`@ValueSource`, `@CsvSource`, `@MethodSource`)
- **`@Tag`** — filter tests by category (`./mvnw test -Dgroups=slow`)
- **`@EnabledOnOs`** — conditional execution
- **`assertAll`** — grouped assertions that report all failures
- **`assertTimeout`** — fail if operation exceeds duration

### AssertJ

Fluent assertion library (preferred over JUnit's built-in assertions):

```java
assertThat(user.name()).isEqualTo("Alice");
assertThat(list).hasSize(3).contains("a", "b");
assertThatThrownBy(() -> riskyCall())
    .isInstanceOf(RuntimeException.class)
    .hasMessageContaining("failed");
```

### Mockito

Mock dependencies to isolate the unit under test:

- **`@Mock`** — create mock instances
- **`@InjectMocks`** — auto-inject mocks into constructor
- **`@Captor`** — capture arguments for inspection
- **`when(...).thenReturn(...)`** — stub return values
- **`verify(...)`** — assert interactions occurred
- **BDD style** — `given(...).willReturn(...)` / `then(...).should()`
- **`spy(...)`** — partial mock wrapping a real object

### TestContainers

Spin up real infrastructure (databases, message brokers, etc.) in Docker:

```java
@Testcontainers
class MyIT {
    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void test() {
        String url = pg.getJdbcUrl(); // real JDBC URL
    }
}
```

Available modules: `postgresql`, `mysql`, `mongodb`, `kafka`, `redis`, `elasticsearch`, `localstack`, and many more.

### REST Assured

Fluent HTTP API testing:

```java
given()
    .contentType(ContentType.JSON)
    .body("""{"name":"Alice"}""")
.when()
    .post("/api/users")
.then()
    .statusCode(201)
    .body("name", equalTo("Alice"));
```

### ArchUnit

Enforce architecture rules as tests — no special tooling needed:

```java
@ArchTest
static final ArchRule rule = noClasses()
    .that().resideInAPackage("..repository..")
    .should().dependOnClassesThat().resideInAPackage("..service..");
```

### Selenium (E2E Overview)

Selenium is not included as a dependency (it requires a browser), but here's the pattern:

```java
// pom.xml: org.seleniumhq.selenium:selenium-java:4.18.1
//          io.github.bonigarcia:webdrivermanager:5.7.0

@Test
void loginFlow() {
    WebDriverManager.chromedriver().setup();
    var driver = new ChromeDriver();
    try {
        driver.get("http://localhost:8080/login");
        driver.findElement(By.id("username")).sendKeys("admin");
        driver.findElement(By.id("password")).sendKeys("secret");
        driver.findElement(By.id("submit")).click();

        var welcome = driver.findElement(By.id("welcome"));
        assertThat(welcome.getText()).contains("Welcome");
    } finally {
        driver.quit();
    }
}
```

For modern projects, consider **Playwright** (`com.microsoft.playwright:playwright:1.42.0`) as a faster alternative with auto-wait and better API.

## Naming Conventions

| Pattern | Plugin | When |
|---------|--------|------|
| `*Test.java` | Surefire | `./mvnw test` |
| `*IT.java` | Failsafe | `./mvnw verify -Pintegration-tests` |

## Best Practices

1. **One assertion concept per test** — test one behavior, use descriptive names
2. **Arrange-Act-Assert** (or Given-When-Then) — consistent structure
3. **Don't mock what you don't own** — wrap third-party APIs, mock the wrapper
4. **Use TestContainers over H2** — test against real databases
5. **Keep unit tests fast** — mock I/O, no network, no disk
6. **Integration tests in CI** — run with Docker in pipeline, skip locally if needed
7. **Coverage as a guide, not a goal** — aim for meaningful coverage, not 100%
8. **Test behavior, not implementation** — tests should survive refactoring

## Related Documentation

- [Main README](../../README.md) — Project overview and quick start
- [Development Workflow](../../docs/development-workflow.md) — CI/CD, quality gates
- [Documentation Standards](../../docs/documentation-standards.md) — Test documentation conventions
- [Security Scanning](../../docs/SECURITY_SCANNING.md) — Quality tool configuration
- [Tutorial](../../docs/TUTORIAL.md) — New developer walkthrough
