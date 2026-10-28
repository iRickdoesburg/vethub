# AGENTS.md

VetHub (package `dev.ilionx.workshop`, artifact `ilionx-pet-store`) is a Pet Clinic-style demo/workshop
app: a Spring Boot REST API (`server/`) + a SvelteKit frontend (`client/`) kept in sync via generated
OpenAPI types. No root README or prior AGENTS.md existed before this file.

## Repo layout

- `server/` — Spring Boot (Java 25) backend, Gradle/Kotlin DSL build.
- `client/` — SvelteKit 5 + TypeScript frontend, Vite + Bun.
- `scripts/` — bash automation (`common.sh`, `openapi-sync.sh`) for starting the backend and
  regenerating frontend API types.
- `mise.toml` — pins toolchain versions: Java `temurin-25`, Bun `1.3.0`, Node `22.20.0`.

Both apps are organized **by domain feature**, not by technical layer: `api/{owner,pet,vet,visit}`
on the backend, `src/lib/api/{owner,pet,vet,visit,specialty,pet-type}` on the frontend. Each backend
domain repeats `controller/service/repository/model/{request,response,mapper,validator}`.

## Tech stack

- **Backend:** Spring Boot (web, data-jpa, validation, actuator, security) on H2 (in-memory).
  Lombok reduces entity/DTO boilerplate. `springdoc-openapi-starter-webmvc-ui` auto-generates the
  OpenAPI spec from controller/`@Schema` annotations (no hand-written spec) — exposed at
  `/api/v1/public/docs`, which is also the input to the frontend codegen pipeline below.
- **Auth:** Spring Security with HTTP Basic (`http.httpBasic()`); credentials are the demo defaults
  (`user`/`password`, passed by the frontend via `Authorization` header). See the `permitAll()` note
  further down — auth is configured but not currently enforced.
- **Frontend HTTP client:** `openapi-fetch` (`client/src/lib/api/client.ts`) — a thin, fully
  type-safe wrapper over `fetch`, parameterized by the generated `paths` type so endpoint paths,
  request bodies, and responses are all compile-time checked. Not Axios/ky/etc.
- **Frontend UI:** Tailwind CSS v4 + shadcn-svelte/bits-ui components (`src/lib/components/ui/`).
- **Backend test libs:** JUnit 5, Mockito (`UnitTest`), Spring `MockMvc` (`IntegrationTest`), and
  Hamcrest matchers (`assertThat(..., is(equalTo(...)))`) — not AssertJ.
- No ORM auto-DDL: Liquibase is the sole source of schema truth (see further down).

## Backend — build/run/test

- Run dev server: `./server/gradlew bootRun` (workdir `server/`). Serves on `localhost:8080`,
  context path `/api`. Health check: `http://localhost:8080/api/v1/public/actuator/health`.
- Run all tests: `./gradlew test`.
- Run a single test class: `./gradlew test --tests "dev.ilionx.workshop.api.owner.service.OwnerServiceTest"`.
- Run a single test method: add `.methodName` to the `--tests` filter.
- `./gradlew build` runs Checkstyle, PMD, SpotBugs, and CodeNarc (via the `ru.vyarus.quality` plugin)
  in addition to compiling and testing — expect it to be slower than `test` alone.
- **Compiling auto-formats code**: `JavaCompile` tasks depend on `spotlessApply`, so `./gradlew build`
  will rewrite Java files in place (import order, trailing whitespace, etc.) before compiling. Don't
  hand-fix formatting that Spotless already owns.
- Compiler flags use `-Xlint:all -Werror` (with `serial`, `processing`, `this-escape` excluded) —
  **warnings fail the build**. Avoid introducing deprecated API usage or raw-type warnings.
- `H2` is in-memory; schema/seed data reset on every JVM restart. No DB setup needed, but also no
  state persists across restarts.

## Backend — testing conventions

Base classes live in `server/src/test/java/dev/ilionx/workshop/support/`:

- `UnitTest` — `@ExtendWith(MockitoExtension.class)` only, no Spring context. Provides shared
  `aValidX()` fixture factories (Owner, Pet, Vet, Visit, Specialty). Extend this for service-layer
  tests; mock the repository manually (`mock(OwnerRepository.class)`), construct the service directly.
- `IntegrationTest` (extends `WebMvcConfigurator` → `TestContextInitializer`) — full
  `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `@ActiveProfiles("test")`, real H2 DB, real
  autowired repositories, and a `MockMvc` instance. Extend this for controller tests; use
  `mockMvc.perform(...)` plus `jsonPath(...)` assertions. It auto-cleans owner/pet/visit tables (and
  any non-seed vet/pet-type/specialty rows) in `@BeforeEach`/`@AfterEach`, and exposes shared
  `aSavedX()` / `aCreateXRequest()` / `anUpdateXRequest()` fixtures for every domain — reuse these
  instead of rebuilding request objects per test.
- Test config: `server/src/test/resources/application-test.yml` uses a separate H2 DB
  (`jdbc:h2:mem:testdb`) and Liquibase context `tst` (vs `prd` for the main app).
- Validation in this codebase uses a custom `io.github.jframe` `Validator<T>` framework
  (`result.rejectField(...).whenNull(...).orWhen(...)`), **not** Jakarta Bean Validation annotations
  — don't reach for `@NotBlank`/`@Valid` expecting them to be wired up; they aren't used here.

## Known intentional gaps (workshop bugs — don't "fix" unprompted)

Several entities have `// BUG:` comments marking deliberately missing validation, e.g.
`Owner.firstName`/`lastName` lack `@NotBlank` and `Pet.birthDate` lacks `@Past`
(`server/src/main/java/.../owner/model/Owner.java`, `.../pet/model/Pet.java`). These look like
training exercises for whoever runs this repo — treat them as intentional unless explicitly asked
to fix validation gaps.

Also note: `WebSecurityConfig` currently calls `.anyRequest().permitAll()` — Basic Auth is configured
(`http.httpBasic()`) and the frontend sends an `Authorization` header, but the backend does not
actually enforce authentication on any endpoint right now.

## Frontend — build/run

- Install: `bun install` (workdir `client/`). Lockfiles for both `bun` and `npm` exist; prefer `bun`
  since `mise.toml` pins it and scripts use `bun run`.
- Dev server: `bun run dev` (Vite/SvelteKit, expects backend at `http://localhost:8080/api` via
  `VITE_SERVER_BASE_URL`, default credentials `user`/`password` via `VITE_API_USERNAME`/`VITE_API_PASSWORD`).
- Type/lint check: `bun run check` (runs `svelte-kit sync && svelte-check`). There is no separate
  lint/test script defined — `check` is the only verification command available for the client.
- Build: `bun run build`.

## Keeping frontend types in sync with the backend (critical, easy to miss)

`client/src/lib/types/api.d.ts` is **auto-generated** (header literally says "Do not make direct
changes to the file") from the backend's live OpenAPI spec. Whenever backend request/response DTOs
or endpoints change, regenerate it — don't hand-edit it and don't assume it already reflects backend
changes:

```sh
bun run sync:api   # from client/ — orchestrates the full cycle below
```

This runs `scripts/openapi-sync.sh`, which: stops any gradle daemon → starts the backend via
`./gradlew bootRun` → waits for `/api/v1/public/actuator/health` → downloads
`http://localhost:8080/api/v1/public/docs` to `server/openapi.json` → runs
`bun run generate:api` (`openapi-typescript`) to regenerate `api.d.ts` → stops the backend again.
After syncing, re-run `bun run check` to confirm the frontend still compiles against the new types.

`client/src/lib/api/models.ts` re-exports the generated `components['schemas'][...]` types under
friendlier names (`OwnerResponse`, `CreatePetRequest`, etc.) — import types from there, not directly
from `$lib/types/api`.

## Cross-cutting conventions

- Backend REST paths are centralized in `server/src/main/java/.../api/Paths.java` — add new
  endpoint paths there rather than inlining string literals in controllers.
- Entity → response DTO mapping uses MapStruct (compile-time codegen, abstract mapper classes per
  domain under `model/mapper/`). Request DTO → entity mapping, by contrast, is hand-written in the
  service layer (e.g. `OwnerService.create`) — this asymmetry is consistent throughout the codebase,
  not a one-off oversight.
- DB schema changes go through Liquibase changesets in
  `server/src/main/resources/db/changelog/changesets/`, named with a date prefix and a `PRD`
  (schema) vs `TST` (seed data) marker. There is no `ddl-auto` schema generation — raw `<sql>`
  changesets are the source of truth; keep them in sync with entity `@Column` constraints manually.
