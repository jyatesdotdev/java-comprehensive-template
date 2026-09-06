# AGENTS.md — `config/` (shared static-analysis configuration)

Read the root `AGENTS.md` first.

## What this directory is

The **single source of truth** for every static-analysis tool in the repo. The parent
`pom.xml` wires these files into all modules via
`${maven.multiModuleProjectDirectory}/config/...` in `pluginManagement`.

**Editing any file here changes the rules for every module.** Never copy these configs
into a module; never point a module at a different config file.

| File | Tool | Bound to build via |
|------|------|--------------------|
| `checkstyle/checkstyle.xml` | Checkstyle 10.14.2 (Google-style base + security rules) | `-Psecurity-scan[-quick]` |
| `checkstyle/checkstyle-suppressions.xml` | Checkstyle file-pattern suppressions | same |
| `pmd/pmd-ruleset.xml` | PMD 3.22.0 "Enterprise Security Ruleset" | same |
| `spotbugs/spotbugs-exclude.xml` | SpotBugs 4.9.8.3 + FindSecBugs (effort=Max, threshold=Low) | same |
| `owasp/owasp-suppressions.xml` | OWASP Dependency-Check 9.1.0 (fails CVSS ≥ 7) | `-Psecurity-scan` only |

Note some docs claim these files live "at the project root" — they do not; this
directory is correct.

## What is currently enforced (summary)

- **Checkstyle**: fails on *warning*. Naming, import hygiene (no star/`sun.*` imports),
  no tabs/trailing whitespace, file ≤ 500 lines, method ≤ 60 lines, ≤ 7 params,
  cyclomatic ≤ 15, nested if ≤ 3 / try ≤ 2, `EqualsHashCode`, `MissingSwitchDefault`,
  `StringLiteralEquality`, braces required. Regex rules: `System.out/err.print` →
  "Use SLF4J" (warning); `Runtime.getRuntime().exec` → **error**. Public-type Javadoc
  is only *info* (non-blocking). Test sources are scanned (`includeTestSourceDirectory=true`).
- **PMD**: whole categories `security`, `performance`, plus `errorprone`,
  `bestpractices`, `multithreading`, `design` with named exclusions (each has an XML
  comment explaining why, e.g. `LooseCoupling` — PMD 7 false positives;
  `DoNotUseThreads` — HPC examples thread intentionally). CyclomaticComplexity
  methodReportLevel=15, CognitiveComplexity reportLevel=20. Generated code
  (`**/generated/**`, `**/jmh_generated/**`, `target/`) excluded. Test code scanned.
- **SpotBugs**: excludes generated code, `SE_BAD_FIELD` on `*Dto`,
  `CRLF_INJECTION_LOGS` (SLF4J false positive), and a block of *style* false-positives
  for `com.example.template.*`. Security-relevant patterns (`PREDICTABLE_RANDOM`,
  `EI_EXPOSE_REP`/`2`, `NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE`,
  `RV_RETURN_VALUE_IGNORED_BAD_PRACTICE`) are narrowed to the demo packages that
  need them (`simulation`, `hpc`, `patterns`, plus entity/REST demo packages for
  representation exposure). Do not restore a repo-wide package match for those.
- **OWASP**: zero active suppressions; the file is a documented template.

## Existing suppression patterns (don't re-add these)

`checkstyle-suppressions.xml` already relaxes:

- `.*Generated.*\.java`, `.*jmh_generated.*` — all checks (generated code).
- `.*Test\.java` / `.*IT\.java` — MethodLength, CyclomaticComplexity,
  MissingJavadocType, (Test only: MethodName, ConstantName).
- `.*systems[/\\].*` and `.*simulation[/\\].*` — the `System.out` regex rule
  (demo/benchmark output is intentional there).

## Rules for changing this directory

1. **Prefer the narrowest change.** Inline `@SuppressWarnings("PMD.X") // reason` or
   `// NOPMD - reason` in one file beats a config edit. A file-pattern suppression
   beats excluding a rule. Excluding a rule for everyone is a last resort.
2. **Every entry gets a comment** stating what it suppresses and *why* (follow the
   existing style in each file). OWASP suppressions additionally require `<notes>` with
   justification, a review date, and ideally a ticket reference.
3. **Never silence a security finding** (FindSecBugs, PMD security category, OWASP)
   without confirming it is a false positive — say how you confirmed it in the comment.
4. After editing, validate across the whole repo, not just one module:

   ```bash
   ./mvnw verify -Psecurity-scan-quick -DskipTests        # checkstyle + pmd + spotbugs, all modules
   ./mvnw verify -Psecurity-scan -DnvdApiKey=<key>        # if you touched owasp-suppressions.xml
   ```
5. Version bumps for the tools themselves happen in the **root pom** properties
   (`checkstyle-plugin.version`, `pmd-plugin.version`, `spotbugs-plugin.version`,
   `owasp-dependency-check.version`), not here.
