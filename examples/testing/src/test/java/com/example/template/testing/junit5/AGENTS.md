# AGENTS.md — `testing.junit5` (JUnit 5 feature reference)

Read the test-tree AGENTS.md one level up first.

`JUnit5FeaturesTest` is the canonical example for: lifecycle hooks
(`@BeforeAll`/`@AfterAll` static, `@BeforeEach`/`@AfterEach`), `@DisplayName`,
AssertJ fluent assertions, `assertAll` grouping, exception assertions
(`assertThatThrownBy`, typed shortcuts), parameterized tests (`@ValueSource`,
`@CsvSource`, `@MethodSource` returning `Stream<Arguments>`), `@Nested` state
grouping, `assertTimeout(Duration)`, conditional execution (`@EnabledOnOs`), and
`@Tag` filtering (`mvn test -Dgroups=slow`).

- Add new JUnit 5 feature demos HERE, as a new `@Nested` group, with a `@DisplayName`
  that names the feature.
- `@MethodSource` providers are `static Stream<Arguments>` next to their consumer.
- Conditional tests (`@EnabledOnOs` etc.) must still leave the suite green on every
  platform — they skip, never fail.
