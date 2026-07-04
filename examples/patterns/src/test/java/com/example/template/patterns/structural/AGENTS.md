# AGENTS.md — patterns `structural` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `structural/AGENTS.md`.
Local: `StructuralPatternsTest`, one `@Nested` group per pattern. Behavior pins: the
dynamic proxy is verified both by delegation results and by its side-effect log
entries (exact CALL/RETURN lines) plus `Proxy.isProxyClass`; composite listings are
unmodifiable; decorator composition is associative through `andThen`/`pipeline`.
