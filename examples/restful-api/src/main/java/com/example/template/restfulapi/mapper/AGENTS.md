# AGENTS.md — `restfulapi.mapper`

Read `examples/restful-api/AGENTS.md` for the layering contract.

- Mappers are `final` classes with a private constructor and **static** methods —
  stateless, no Spring wiring. (For large mapping surfaces, MapStruct is the
  sanctioned alternative — see `docs/third-party-libraries.md`.)
- `toResponse` converts entity → DTO; `updateEntity` applies request → entity and is
  a **full overwrite** (a null description clears the stored value) — its javadoc and
  a test pin that; don't quietly make it partial.
- This is the only package allowed to touch both `domain` and `dto`; keeping the
  conversion here is what lets those two evolve independently.
- Tests: field-by-field mapping assertions in `src/test/.../mapper/`.
