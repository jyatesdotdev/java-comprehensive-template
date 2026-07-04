# AGENTS.md — patterns `behavioral` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `behavioral/AGENTS.md`.
Local: `BehavioralPatternsTest`, one `@Nested` group per pattern. Behavior pins:
`Validator` chains merge ALL errors (no short-circuit — documented API, not a bug);
`RemoveItem.undo()` restores membership not position (`containsExactlyInAnyOrder`);
`CommandHistory` undo is LIFO and empty-history undo is a no-op; EventBus delivery is
asserted through captured events, including unsubscribe taking effect immediately.
