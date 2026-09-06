# AGENTS.md — `restful-api` tests

Read `examples/restful-api/AGENTS.md` first; testing styles come from `examples/testing/`.

- Test packages mirror main packages: `controller/` (`@WebMvcTest` + `@MockBean`
  service — status codes, `Location` header, validation-400 field details, 404 via the
  advice, malformed JSON and bad UUID as 400 not 500), `service/` and `mapper/` and
  `dto/` (plain unit tests, no Spring context), `client/` (error-path tests via
  package constructors, no `ReflectionTestUtils`), `archunit/` (layering: controllers
  off persistence, module stays in `com.example.template.restfulapi`).
  `RestApiApplicationTest` is a `@SpringBootTest` context-load smoke test for the
  Docker-built app.
- JUnit 5 + AssertJ `assertThat` everywhere; `*Test` suffix (surefire). No Docker
  needed anywhere in this module's tests.
- PMD runs on tests: every test asserts something; repeated string literals get
  hoisted to constants (`AvoidDuplicateLiterals`); no `System.out`.
- Behavior pins to know before "fixing" code: `updateEntity` is a full overwrite
  (null description clears the value) and `findAll` returns an immutable snapshot —
  tests assert both intentionally.
