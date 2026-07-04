# AGENTS.md — restful-api `mapper` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `mapper/AGENTS.md`.
Local: field-by-field mapping assertions. Behavior pin: `updateEntity` is a **full
overwrite** — a null request description clears the stored value, and id/timestamps
stay untouched; those tests are intentional, not oversights.
