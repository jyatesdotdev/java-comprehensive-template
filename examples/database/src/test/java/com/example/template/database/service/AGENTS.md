# AGENTS.md — database `service` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `service/AGENTS.md`.
Local: Mockito-mocked `OrderRepository` (`@ExtendWith(MockitoExtension.class)`,
`@Mock`/`@InjectMocks`/`@Captor`); no Spring, no database. Every public method covers
success plus each exception path — including asserting `save` is `never()` called
when a state transition is rejected. Transaction annotations aren't exercised here;
that's the repository slice's job.
