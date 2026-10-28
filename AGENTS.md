# AGENTS.md — IFRN Help Desk API

## Project context and goal

This is an **individual academic project** for the IFRN course **Desenvolvimento de Sistemas Corporativos (2026.2)**. Build a small REST API for support tickets using **Java 21, Spring Boot 4, Maven, Spring Web, Spring Data JPA, PostgreSQL, Flyway and Docker**.

The goal is to demonstrate correct layered architecture, business rules, persistence, pagination/filtering, migrations, testing and reproducible execution with Docker. **Prefer the simplest correct implementation.** This is not a production platform: avoid overengineering and functionality outside the assignment.

## Source of truth — read before making changes

Consult both documents before implementing or modifying functionality:

1. **`Projeto_API_Sistema_de_Chamados_IFRN.pdf`** — original assignment, required behavior, entities, endpoints, HTTP responses and delivery requirements.
2. **`OVERVIEW_PROJETO.md`** — agreed implementation approach, development phases and explicit local scope adjustments.

If a filename differs in the repository, locate the corresponding document instead of assuming it is missing. **Do not silently invent requirements.**

**Precedence:** follow the original PDF for mandatory requirements **except for explicit changes documented in the overview**. The overview intentionally makes users **read-only API resources**: users are inserted through a Flyway seed, `GET /usuarios` and `GET /usuarios/{id}` expose user data, and the required `GET /usuarios/{id}/chamados` endpoint remains available for ticket queries. Do not implement user creation, update or deletion endpoints unless explicitly requested. If any other real conflict or ambiguity affects implementation, report it instead of deciding silently.

## Scope boundaries

- Implement only the five core entities: sector, category, user, ticket and interaction, plus the required enums and supporting DTOs.
- Preserve the specified business rules, status transitions, endpoint paths, HTTP status codes, filters and pagination behavior.
- Keep API routes and JSON contracts aligned with the documents (for example, `/setores`, `/categorias`, `/usuarios`, `/chamados`), even though Java identifiers are in English.
- No authentication: the acting user's ID is supplied in the request body, as defined in the assignment.
- Do not add frontend, notifications, uploads, authorization infrastructure, queues or extra features.
- Add Docker, GitHub Actions and delivery documentation in the phases described in the overview; do not introduce infrastructure early without need.

## Architecture and code organization

Use a **single Spring Boot application with traditional layers**:

- `controller`: HTTP input/output and response status only; no business rules.
- `service`: business rules, state transitions, actor checks and transactional operations.
- `repository`: `JpaRepository` and straightforward derived queries or `@Query` for filters.
- `model`: JPA entities and enums.
- `dto`: concise request/response types; do not expose JPA entities directly in API responses.
- `exception`: domain exceptions and one `@RestControllerAdvice` for standardized errors.

Use **constructor injection**; do not use field injection. Prefer concrete services without an interface/implementation pair unless there is a demonstrated need. Prefer straightforward methods over design-pattern frameworks or generic abstractions.

## Naming and coding conventions

- **Write code identifiers in English**: class, method, field, variable, enum type, test name and package names. Examples: `TicketService`, `TicketRepository`, `SupportSector`, `openTicket`, `assignedTechnician`.
- Use `com.ifrn.joaomanoelfontes.helpdesk` as the base package. The initial `com.ifrn.joaomanoelfontes.chamados` package must be renamed when the application structure is implemented.
- **Preserve the API's Portuguese paths, external field names and required enum values** when the specification requires them, except for the explicitly English error keys. Use explicit DTO names or JSON mapping (e.g. `@JsonProperty`) as needed; do not break the documented API just to translate a name. Required enum constants such as `ABERTO`, `MEDIA` and `TECNICO` are exceptions to the English-identifier rule because they are part of the external contract.
- Standard error JSON uses the English keys `timestamp`, `status`, `message` and `path`.
- Write SQL identifiers and Flyway migration descriptions in English; keep them consistent with JPA mappings. Do not rename existing persisted columns casually.
- Use clear, descriptive names; small cohesive methods; simple conditionals; minimal comments explaining *why*, not restating the code.
- Prefer `Long` IDs, `LocalDateTime` timestamps, `EnumType.STRING` for enums, as proposed in the overview.
- Use Jakarta Validation for request input and explicit service checks for business rules.
- Follow existing formatting and conventions where they do not conflict with these rules. Avoid introducing an automated formatter or new dependency without a concrete need.
- Avoid unrequested refactoring and unrelated file changes.

## Business rules and API behavior

- A new ticket starts as `ABERTO`, with system-generated opening date and `MEDIA` priority when unspecified.
- Only a requester can open a ticket.
- A technician can assume only an `ABERTO` ticket whose category belongs to the technician's sector; then status becomes `EM_ATENDIMENTO`.
- Only the assigned technician may resolve it, providing a nonempty solution; record the closing date.
- Only the original requester may cancel it, and only while `ABERTO`.
- `RESOLVIDO` and `CANCELADO` tickets cannot be changed or receive interactions.
- Only the requester or assigned technician can add interactions.
- In the service layer, prevent deleting a sector when it has linked categories, technicians or tickets, and prevent deleting a category when it has linked tickets. Return `409` before attempting deletion.
- Allowed transitions only: `ABERTO → EM_ATENDIMENTO → RESOLVIDO` and `ABERTO → CANCELADO`.
- All list endpoints use pagination (`page`, `size`, `sort`), default size **10**, maximum **50**, and optional combinable filters defined in the documents.
- Return `400` for invalid input, `404` for missing resources and `409` for business-rule conflicts, with the standardized error body containing `timestamp`, `status`, `message` and `path`.

## Persistence and migrations

- PostgreSQL connection settings must come from environment variables; do not hardcode credentials in `application.properties`.
- Use Flyway migrations under `src/main/resources/db/migration`; never modify an already applied migration. Add a new migration for subsequent changes.
- Set `spring.jpa.hibernate.ddl-auto=validate`; Hibernate must not create/update database tables.
- Include initial seed data with at least **2 sectors, 4 categories and 4 users**. Users are seeded exclusively through Flyway, not through a controller, `CommandLineRunner` or startup side effects.
- Use `@ManyToOne`/`@OneToMany` as required, foreign keys and uniqueness constraints in the database.
- Keep foreign keys for referential integrity, but do not rely on a database constraint violation to implement deletion rules. Services must check dependencies first and return the domain-level `409`; normal API deletion flows must not reach a database-blocked delete.
- Keep database queries simple. Prefer derived repository queries or `@Query` to introducing QueryDSL or a generic filtering engine.

## Tests — required working practice

- **Write and maintain unit tests** for implemented business behavior, especially the service layer and all valid/invalid ticket transitions.
- Use **JUnit 5 and Mockito** (via the project's Spring Boot test dependencies, as applicable). Mock repositories/dependencies for service unit tests; do not start the Spring context or Docker for pure unit tests.
- Name tests descriptively in English, e.g. `shouldRejectTechnicianFromDifferentSector` or `shouldDefaultPriorityToMedium`.
- Cover happy paths, invalid actors, cross-sector assignment, missing resources, invalid transitions, closed-ticket restrictions and deletion conflicts.
- Test pagination/filtering behavior at an appropriate level; add integration tests only when they provide value for JPA queries, migrations or HTTP contracts. Keep the test suite practical and maintainable.
- For each behavioral change, add or adjust the relevant tests. Run the relevant tests and, when feasible, the full suite (`./mvnw test` or `./mvnw verify`). **Never claim tests passed unless they were actually executed.**
- If the environment prevents running tests, state exactly what was not verified and why.

## Simplicity rules — no overengineering

Do **not** add by default:

- Hexagonal/Clean Architecture scaffolding, CQRS, Event Sourcing, microservices or separate Maven modules.
- `IService`/`ServiceImpl` pairs, factories, complex mappers or abstract base classes for trivial use cases.
- Generic state machines, event buses, strategy registries or custom frameworks for simple ticket transitions.
- Spring Security, JWT, Redis, RabbitMQ, MapStruct, QueryDSL or other unrequested dependencies.
- Extra endpoints, optional domain fields or features without a direct requirement.

A few lines of understandable code are preferable to an abstraction used only once. However, **do not sacrifice correctness, validation or tests for brevity**.

## Delivery and workflow

1. Before a task, read the relevant sections of **both** project documents and inspect the affected code.
2. Make the **smallest focused change** that meets the requirement, following the overview's implementation sequence where possible.
3. Keep the project compiling and tests updated at each step; report what you executed.
4. Preserve the required Docker outcome: `docker compose up` must start API + PostgreSQL, and the API must respond at `http://localhost:8080` without manually installing Java, Maven or PostgreSQL.
5. Maintain the `.http`/Postman/Insomnia examples and `README.md` as features are completed, including the required short Flyway explanation.
6. Keep commits small and meaningful throughout development. Do not create commits, push, overwrite unrelated work or change repository history unless explicitly asked.
7. When a request is ambiguous, identify the specific uncertainty; otherwise make a minimal, document-grounded choice and explain it briefly.

**Priority order:** documented requirements and explicit scope decisions → correctness and tests → clarity and maintainability → simplicity → optional polish.
