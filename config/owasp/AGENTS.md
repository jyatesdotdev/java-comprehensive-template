# AGENTS.md — `config/owasp/`

Read `config/AGENTS.md` first — it explains how this file binds to every module.

- `owasp-suppressions.xml` — suppressions for OWASP Dependency-Check (fails the build
  at CVSS ≥ 7 under `-Psecurity-scan`). Currently **zero active suppressions**; the
  file contains commented-out templates for the three suppression styles (by CVE+gav,
  by CPE+packageUrl, by cvssBelow).

Rules: a suppression is a security decision — every `<suppress>` MUST contain a
`<notes>` block with (1) why it's a false positive or accepted risk, (2) a review
date, (3) a ticket reference if one exists. Suppress the narrowest match (specific CVE
+ specific gav), never a whole CPE unless the mapping itself is wrong. Prefer
upgrading the dependency in the root pom over suppressing. Verify with
`./mvnw verify -Psecurity-scan -DnvdApiKey=<key>`.
