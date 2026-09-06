# AGENTS.md — restful-api `client` tests

Rules: `../AGENTS.md` (module test guide) and the main-side `client/AGENTS.md`.
Local: unit tests for client error paths (non-2xx → `ClientException`/
`ResourceNotFoundException` mapping) using Mockito and package-visible constructors;
no live HTTP and no `ReflectionTestUtils.setField`. Full request/response testing
against a real container belongs in a Testcontainers `*IT` modeled on
`examples/testing/RestAssuredIT`.
