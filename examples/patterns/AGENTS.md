# AGENTS.md — `examples/patterns/`

Read the root and `examples/AGENTS.md` first. Fifteen GoF design patterns implemented
with **modern Java idioms** — the point of this module is that the 1994 pattern and the
2024 language feature are often the same thing.

## Layout — one final holder class per category, patterns as nested types

| File | Patterns → modern idiom used |
|------|------------------------------|
| `creational/CreationalPatterns` | Builder → record + step-builder interfaces; Factory Method → sealed `Shape` + static `of()` switch; Singleton → **enum** `AppConfig.INSTANCE`; Prototype → record "withers" (`withRecipient`); Abstract Factory → `Map<String, Supplier<Widget>>` registry |
| `structural/StructuralPatterns` | Adapter → lambda over a `@FunctionalInterface`; Decorator → `andThen` default-method composition; Proxy → `java.lang.reflect.Proxy` dynamic proxy; Composite → sealed `FileSystemEntry` + switch; Facade → records + `OrderFacade` |
| `behavioral/BehavioralPatterns` | Strategy → `@FunctionalInterface` constants (REGULAR/BULK/VIP); Observer → generic `EventBus<E>` on `CopyOnWriteArrayList` + sealed `OrderEvent`; Command → sealed `Command<T>` records + `Deque` history; Template Method → abstract class with `final export()`; Chain of Responsibility → `Validator<T>` with `andThen`/`of` |

## Rules when adding or modifying a pattern

- Keep the structure: nested records/sealed interfaces/static classes inside the
  category holder, one `// ── Pattern Name ──` banner section each, class Javadoc
  updated to list the pattern.
- Prefer the language-level expression of a pattern over the classical class
  hierarchy: sealed + switch beats visitor boilerplate, lambdas beat single-method
  subclasses, enums beat lazy-init singletons. If the classical form is shown, say why
  in Javadoc.
- Immutability first (`Map.copyOf`, records); thread-safe collections where the
  pattern implies sharing (`CopyOnWriteArrayList` in the event bus).
- The holder classes carry justified suppressions
  (`PMD.MissingStaticMethodInNonInstantiatableClass`, `PMD.UseUtilityClass`, and an
  `unchecked` cast on the dynamic proxy) — follow that precedent; don't widen it.

## Commands

```bash
./mvnw -pl examples/patterns compile
./mvnw -pl examples/patterns test    # 58 tests, one @Nested class per pattern
```

## Gotchas

- Tests (58): one test class per holder, one `@Nested` class per pattern. A new
  pattern is not done until it has its own `@Nested` test group covering its
  observable behavior.
- `Shape.of` validates its varargs and throws `IllegalArgumentException` for both
  unknown types and missing dimensions — keep factory methods validating their inputs.
- `HttpRequest.Builder.build()` deliberately copies into an unmodifiable
  `LinkedHashMap` to preserve header insertion order (`Map.copyOf` would discard it) —
  there's a comment on it; don't "simplify" it away.
- Known demo simplifications (documented, don't silently change): `AppConfig.get`
  returns `null` for absent keys per its Javadoc; `Command.RemoveItem.undo()`
  re-appends at the list end rather than the original position.
- No dependencies beyond the parent — keep it that way; this module must stay
  JDK-only.
- `jacoco.skip=true`.
