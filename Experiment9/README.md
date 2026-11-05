# Experiment 9: Continuous Integration (CI) Pipeline

## Aim

Automatically build and test the Spring Boot scheduled-post application on every push and pull request targeting `main`. The backend, frontend, CI tests, and experiment documentation are all in `Experiment9`.

## Technologies

- Java 17 and Spring Boot
- Maven
- JUnit 5 and Mockito
- Spring MockMvc
- Pact consumer and provider verification
- Testcontainers with PostgreSQL
- GitHub Actions

## Testing

- `PostSchedulerServiceTest` unit-tests due-post publishing, no-op behavior, and repository/Kafka failure paths using Mockito.
- `PostControllerTest` exercises the existing `GET /api/posts` and `POST /api/posts/schedule` endpoints with MockMvc, including malformed JSON and an unsupported update route.
- `ScheduledPostRepositoryPostgresTest` verifies repository persistence and due-post queries against a disposable PostgreSQL container.
- Pact consumer tests define the frontend's GET and POST expectations. Provider verification starts the Spring Boot API and verifies the generated Pact files against those expectations.
- The frontend sends the `datetime-local` value as a local date-time, matching the backend's `LocalDateTime` field.

The API currently supports GET and POST only; PUT and DELETE are not implemented. The integration test confirms that an unsupported PUT receives HTTP 405 rather than assuming an update endpoint exists.

## Run tests locally

Run Docker Desktop (or another Docker-compatible runtime) before testing. From the repository root:

```shell
cd Experiment9/backend
mvn clean verify
```

The PostgreSQL Testcontainers test uses a fresh disposable database and does not connect to or modify the development H2 database. If Docker is unavailable, that container-backed test is skipped; the remaining unit, MockMvc, and Pact tests still run.

The Pact consumer tests write contracts under `target/pacts`; provider verification runs afterward as part of the same Maven test suite. `mvn clean verify` ensures a clean contract-generation and verification run.

## Push and check GitHub Actions

Commit and push the changes to a GitHub branch, then open a pull request targeting `main` or push directly to `main`:

```shell
git add .github/workflows/ci.yml Experiment8 Experiment9
git commit -m "Add Experiment 9 CI pipeline"
git push -u origin <branch-name>
```

In GitHub, open the repository's **Actions** tab and select the **Spring Boot CI** workflow run for the push or pull request. A successful run indicates that `mvn clean verify` passed. Test and Pact reports are attached to the run as the `backend-test-reports` artifact, including when a test fails.
