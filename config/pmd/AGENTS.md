# AGENTS.md — `config/pmd/`

Read `config/AGENTS.md` first — it explains how this ruleset binds to every module.

- `pmd-ruleset.xml` — enables whole categories (`security`, `performance` fully;
  `errorprone`, `bestpractices`, `multithreading`, `design` with named exclusions).
  Every `<exclude>` has a comment explaining why (noise or PMD 7 false positive) —
  keep that discipline for any exclusion you add.
- Complexity thresholds are overridden here: CyclomaticComplexity methodReportLevel=15,
  CognitiveComplexity reportLevel=20. Generated code (`**/generated/**`,
  `**/jmh_generated/**`, `target/`) is excluded via `<exclude-pattern>`.

Rules: to silence one finding, use `@SuppressWarnings("PMD.RuleName") // reason` or
`// NOPMD - reason` at the site instead of editing this file. Never exclude a rule
from the `security` category without documented proof it's a false positive. Test with
`./mvnw pmd:check -pl <module>`; CPD runs via `pmd:cpd-check` under the scan profiles.
