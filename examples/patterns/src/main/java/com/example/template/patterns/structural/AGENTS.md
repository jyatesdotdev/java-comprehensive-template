# AGENTS.md — `patterns.structural`

Read `examples/patterns/AGENTS.md` for the module rules.

`StructuralPatterns` — object *composition* patterns and their modern idioms:
Adapter → lambda over a `@FunctionalInterface`; Decorator → `andThen` default-method
composition (+ static `pipeline(...)`); Proxy → `java.lang.reflect.Proxy` dynamic
proxy; Composite → sealed `FileSystemEntry` + exhaustive switch; Facade → records
orchestrated by `OrderFacade`.

Local invariants:
- Functional-interface patterns compose via `default` methods — new decorators/
  adapters extend the composition chain, they don't subclass.
- The dynamic proxy's `unchecked` cast carries a method-level
  `@SuppressWarnings("unchecked")` — that's the sanctioned narrow scope; don't widen
  it to the class.
- Sealed hierarchies stay exhaustive: adding a `FileSystemEntry` subtype means the
  compiler will flag every switch — fix them all, never add a `default` arm to dodge it.
- Facade returns result records; it coordinates subsystems but owns no state.
