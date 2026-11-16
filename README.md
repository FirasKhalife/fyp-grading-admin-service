# admin-service

The core of Assay. It owns:

- teams, students, faculty, and each reviewer's role per team (jury or advisor)
- due dates and defense slots
- reviewers' grades, team grades and the final grade
- the release of results to students
- the visitors' demo copies

This service is part of **Assay**, a final-year-project grading platform. The architecture is described in
[fyp-grading-docker-compose/docs/architecture.md](https://github.com/FirasKhalife/fyp-grading-docker-compose/blob/master/docs/architecture.md).

## API

| Method and path | Who | What |
|---|---|---|
| `GET /api/demo/personas` | public | The four demo personas |
| `GET`, `PUT /api/demo/session`, `POST /api/demo/session/reset` | signed in | The visitor's private copy: read it, create it (idempotent), reset it |
| `GET /api/me` | signed in | Who is signed in |
| `GET /api/me/assignments` | reviewer | My teams, and what I must grade by when |
| `GET /api/me/team`, `GET /api/me/team/results/{assessment}` | student | My team, the timeline, the released results |
| `GET /api/assessments`, `PATCH /api/assessments/{a}`, `POST /api/assessments/{a}/release` | all / admin | Weights, due dates, release |
| `GET /api/overview`, `GET /api/overview/export` | admin | The team × assessment grid, the KPIs, a CSV export |
| `/api/teams` (CRUD), `/api/teams/{id}/students`, `PUT /api/teams/{id}/reviewers`, `PUT /api/teams/{id}/defense-slot` | admin | Teams and people, reviewer assignment, the schedule |
| `GET /api/reviewers`, `GET /api/schedule` | admin / all | The faculty list; the defenses, due dates and conflicts, filtered by role |
| `GET /internal/assignments/...`, `POST /internal/grades` | evaluation-service | The assignment check and the grade hand-off (the gateway doesn't route them) |

## Events and calls

- **Publishes** `assay.grades` when a grade arrives, and **consumes** it itself (`aggregate`):
  - It locks the team.
  - It recomputes the team grade (the plain average) and the final grade (weights 15/0/25/30/30; it needs all five).
  - It announces only state changes, so a redelivered message can't count a grade twice.
- **Publishes** `assay.notifications` with the recipients resolved (coordinators, the team's reviewers, its
  students), and `assay.activity` for the coordinator's feed.

## Rules it enforces

- Team composition: 1 to 5 reviewers, at least 1 jury member and at least 1 advisor, one role each.
- A reviewer who submitted for a team can't be removed from it or change role.
- Releasing an assessment releases it for every team whose grade is final.
- Students see the final grade once all five assessments are released.
- Submissions are permanent.

## Run it

The whole stack runs from [fyp-grading-docker-compose](https://github.com/FirasKhalife/fyp-grading-docker-compose).
To run this service on its own (Java 25, Maven):

```bash
./mvnw spring-boot:run
```

It needs Postgres (`admin-db`), RabbitMQ and Keycloak (realm `assay`). For the full screens it also needs the other three services, found through Eureka.

## Test

```bash
./mvnw test
```

Unit tests cover the grade maths and the composition rules.

`GradingPipelineTest` runs the event-driven pipeline end to end on real Postgres and RabbitMQ:

- Grades go in; team grades, the final grade and the notifications come out, exactly once.
- The seed matches the design: Alpha 88.0, Gamma 92.2, Beta spread 21.3.
- The release flow, roles and isolation between copies. Docker must be running for the Testcontainers tests.

## Notes for reviewers

- Spring Boot 4.0 and Spring Cloud 2025.1 on Java 25. The image is layered and runs as a non-root user.
- Every call needs a Keycloak token and an `X-Demo-Session` header.
- Each visitor gets a private copy of the data:
  - Every row is scoped to the visitor's session (Hibernate `@TenantId`).
  - A visitor's copy is created from `src/main/resources/demo/seed.json`.
  - Every copy is deleted at midnight (Europe/Paris).
- Errors are RFC 9457 problem details with a stable `code`.
- Roles are checked with method security. Identity always comes from the token.
- Actuator exposes only health, info and prometheus, and the gateway doesn't route it.
- The shared API contract lives in the workspace (`contract/openapi.yaml`). This service publishes its own part
  at `/v3/api-docs`.
