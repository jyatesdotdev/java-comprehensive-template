# AGENTS.md — `patterns.behavioral`

Read `examples/patterns/AGENTS.md` for the module rules.

`BehavioralPatterns` — object *interaction* patterns and their modern idioms:
Strategy → `@FunctionalInterface` constants (REGULAR/BULK/VIP) + `withSurcharge`
composition; Observer → generic `EventBus<E>` on `CopyOnWriteArrayList` + sealed
`OrderEvent`; Command → sealed `Command<T>` records + `CommandHistory` (Deque, LIFO
undo); Template Method → abstract `DataExporter<T>` with `final export()` and a
`footer()` hook; Chain of Responsibility → `Validator<T>` with `andThen`/`of`.

Local invariants:
- `EventBus` uses `CopyOnWriteArrayList` so listeners can (un)subscribe during
  publish — keep that if you touch it; a plain list breaks concurrent iteration.
- `Validator.andThen` **merges errors from every link** (documented; it does not
  short-circuit) — tests pin that; changing it is an API change, not a fix.
- Template method: the algorithm skeleton (`export`) stays `final`; variation points
  are the abstract/hook methods only.
- Documented demo simplification: `Command.RemoveItem.undo()` re-appends at the list
  end, not the original index.
