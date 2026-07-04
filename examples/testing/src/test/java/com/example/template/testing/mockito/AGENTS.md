# AGENTS.md — `testing.mockito` (Mockito feature reference)

Read the test-tree AGENTS.md one level up first.

`MockitoFeaturesTest` is the canonical example for: `@ExtendWith(MockitoExtension.class)`
with `@Mock`/`@InjectMocks`/`@Captor`, stubbing (`when...thenReturn`, `thenAnswer`),
argument matchers (`anyString()`, `any(User.class)` — all-or-nothing per call),
verification (`verify`, `never()`, `verifyNoMoreInteractions`), captor
capture-and-inspect, BDD style (`given.../then(...).should()`), and spies
(`spy(...)` + `doReturn().when()` to avoid invoking the real method while stubbing).

- The module uses `mockito-junit-jupiter` with the inline mock maker specifically so
  final classes and static methods are mockable (see `app`'s `ApplicationMainTest`
  for `mockStatic`). Don't add PowerMock or reflection hacks — inline covers it.
- MockitoExtension runs in strict-stubs mode: an unused stub fails the test. That's a
  feature; fix the stub, don't relax the strictness.
- Mock interfaces you own (`UserRepository`), not types you don't control — wrap
  third-party clients first.
