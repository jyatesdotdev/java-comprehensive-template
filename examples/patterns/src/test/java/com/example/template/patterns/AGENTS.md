# AGENTS.md — `patterns` tests

Read `examples/patterns/AGENTS.md` first.

- 58 tests: one test class per category holder, one `@Nested` class per pattern —
  a new pattern is not done until its `@Nested` group exists.
- Test observable behavior, not structure: what the factory returns, what the
  decorator chain produces, what undo restores — plus the negative paths (unknown
  factory keys, missing dimensions, empty history undo as no-op).
- Behavior pins to respect: header assertions on `HttpRequest` are order-aware
  (`LinkedHashMap` copy preserves insertion order); `Validator` chains merge ALL
  errors (no short-circuit); `RemoveItem.undo()` restores membership, not position
  (`containsExactlyInAnyOrder`).
- Exhaustive-switch tests over sealed events double as compile-time regression guards
  when subtypes are added.
- PMD applies: every test asserts; repeated literals become constants; no `System.out`.
