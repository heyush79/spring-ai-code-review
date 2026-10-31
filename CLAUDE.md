# spring-ai-code-review

## What this is
- Spring Boot web app that reviews pasted code with a local Ollama LLM. It adds context from guideline documents that users upload and that are stored in PostgreSQL. Retrieval is keyword `LIKE` search, not vectors.
- Java 17 (`java.version` in pom), Spring Boot 3.3.5, Spring AI 1.0.0 (`spring-ai-starter-model-ollama`), Spring Data JPA + PostgreSQL, Thymeleaf, Lombok 1.18.34 (from Boot parent). Wrapper runs Maven 3.9.12.
- Models: chat `orca-mini`, embedding `nomic-embed-text` (configured but not used by any code). The pom has no vector-store dependency.

## How to run it
Use JDK 17 for every Maven command. On the dev Mac `JAVA_HOME` defaults to JDK 26, and JDK 26 breaks Lombok (see Gotchas):
`export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`
- Build: `sh mvnw -B -ntp clean package -DskipTests` takes ~2 s warm. On a cold cache the first run took ~60 s: ~13 s to download the wrapper's Maven, the rest for dependencies. Output: `target/spring-ai-code-review-0.0.1-SNAPSHOT.jar`.
- All tests: `sh mvnw -B -ntp test` (~2 s). Currently fails, see Known issues.
- Single test: `sh mvnw -B -ntp test -Dtest=ApplicationTests#contextLoads`
- Run (unverified, not run): `sh mvnw spring-boot:run` or `java -jar target/spring-ai-code-review-0.0.1-SNAPSHOT.jar`. UI and REST are both on port 8080 (`server.port`).
- Lint/format: none configured (no Checkstyle, Spotless, or formatter plugin). No Dockerfile, compose file, or CI workflow.

External services, all set in `src/main/resources/application.yml` (setup unverified, not run):
- PostgreSQL `jdbc:postgresql://localhost:5433/ai_review` (port 5433, not 5432), user `postgres`. Create DB `ai_review`, then run `setup-rag.sql`. The script only runs `CREATE EXTENSION IF NOT EXISTS vector`. Tables come from Hibernate `ddl-auto: update`.
- Ollama `http://localhost:11434`: `ollama pull orca-mini` and `ollama pull nomic-embed-text`.
- No API keys needed (no paid provider). The code reads no custom env vars. The DB password is set in `application.yml` itself and is currently a masked placeholder (see Known issues). There is no example/template config file.

## Project layout
- `src/main/java/com/ai/reviewer/Application.java`: `@SpringBootApplication` entry point.
- `.../controller/`: `ReviewController` (REST `/review`), `DocumentController` (REST `/documents`), `WebController` (Thymeleaf pages).
- `.../service/`: `AIReviewService` (retrieval, prompt, ChatClient call), `DocumentIngestionService` (upload, list, keyword search).
- `.../model/`: JPA entities `Document` (table `documents`, nested enum `DocumentType`) and `CodeReview`.
- `.../repository/`: Spring Data `JpaRepository` interfaces. `.../dto/`: request/response objects.
- `.../config/`: `WebMvcConfig` (`/` -> `index`), `VectorStoreInitializer` (runs `CREATE EXTENSION vector` at startup), `VectorStoreConfig` (empty placeholder).
- `src/main/resources/application.yml`: all runtime config. `application.properties` is empty.
- `src/main/resources/templates/`: `index`, `upload`, `review`, `documents` .html. CSS is in `static/css/style.css`.
- `src/test/java/com/ai/reviewer/ApplicationTests.java`: the only test (`@SpringBootTest` context load).
- `setup-rag.sql`: pgvector extension setup.

## Architecture in short
1. `controller/WebController.java` serves the pages GET `/`, `/upload`, `/review`, `/view-documents`. Inline JS in each template calls the REST API with `fetch()`.
2. Upload: POST `/documents/upload` (multipart `file` + `type`) goes to `DocumentController`, then `DocumentIngestionService.uploadDocument`. That stores the full text in `documents` and sets `indexed=true`. There is no chunking or embedding.
3. Review: POST `/review` with JSON `{code, language}` goes to `ReviewController`, then `AIReviewService.reviewCode`.
4. Retrieval (`AIReviewService.retrieveRelevantContext`) tries keywords `[language, "best practices", "standard", "pattern", "guideline"]` with `DocumentRepository.findByContentContainingIgnoreCase`. `extractKeywords` ignores the code. The first keyword with any hits wins, and up to 3 docs are used, first 200 chars of each.
5. Prompt: the system and user text blocks are inline in `AIReviewService.reviewCode`. They go to a `ChatClient` built from the auto-configured Ollama `ChatClient.Builder` through a blocking `.call().content()`.
6. `ReviewController` saves a `CodeReview` row (code, language, review) and returns `{"review": "..."}`. `review.html` renders it with a small markdown-like formatter.

## Conventions
- Package by layer under `com.ai.reviewer`. The Maven groupId is `com.example`, which does not match the package name.
- Constructor injection into `final` fields; no `@Autowired`. Logging is Lombok `@Slf4j` with `{}` placeholders.
- Lombok use is mixed. `Document` and `DocumentUploadResponse` use `@Data`/`@NoArgsConstructor`/`@AllArgsConstructor`. `CodeReview`, `ReviewRequest` and `ReviewResponse` have hand-written getters and setters. Match the file you edit.
- Indentation: 4 spaces, except the Initializr files `Application.java` and `ApplicationTests.java`, which use tabs.
- Errors: there is no `@ControllerAdvice`.
  - `DocumentController` catches exceptions and returns HTTP 200 with a failure `message`.
  - `AIReviewService` catches retrieval errors and uses a fallback context string.
  - `ReviewController` does no handling, so Ollama failures surface as HTTP 500.
- Prompts are inline Java text blocks in `service/AIReviewService.java`. There are no prompt resource files.
- Templates are plain HTML with vanilla JS and hard-coded absolute URLs. Thymeleaf is used only for `th:href="@{/css/style.css}"`.
- Tests: JUnit 5 through `spring-boot-starter-test`. The suite is one context-load test with no mocks, slices, or test DB.
- Commits: short, lowercase, no Conventional Commits prefix, e.g. `implemented RAG based pipeline for review`, `spring mvc with thymeleaf added for frontend`. README edits come from the GitHub web UI (`Update README.md`).

## Do NOT
- Put a real DB password in `application.yml`. If asked to fix the placeholder, use an env placeholder such as `${DB_PASSWORD}`, not a literal value.
- Add or rename a `DocumentType` without also updating the `<option>` lists in `upload.html` and `documents.html` and the error message in `DocumentController`.
- Change REST paths (`/review`, `/documents`, `/documents/upload`) without updating the `fetch()` calls in the templates.
- Change entity fields casually. `ddl-auto: update` alters the live schema, and `setup-rag.sql` comments describe the tables.
- Add pgvector, vector-store, or paid-provider (OpenAI etc.) dependencies, or switch away from Ollama, without asking. The README says local Ollama was chosen because it is free.
- Edit the system prompt in `AIReviewService` casually. It defines the summary/bugs/optimizations/rating format that users see.
- Bump Spring AI in only one place. The pom has a `spring-ai.version` property and also a hard-coded `1.0.0` on the BOM import.
- Reformat whole files or convert tabs and spaces across files.
- Treat `RAG_IMPLEMENTATION.md` or `THYMELEAF_FRONTEND_GUIDE.md` as a spec. Check the code.

## Gotchas
- `mvnw` is committed without the executable bit (git mode 100644), so `./mvnw` gives "permission denied". Use `sh mvnw ...`.
- With JDK 26 (the dev Mac's default `JAVA_HOME`), compilation fails with `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`. Lombok 1.18.34 does not support JDK 26. `mvnw` uses `JAVA_HOME`, not the JDK 17 `java` on PATH.
- `password: **********` in `application.yml` is invalid YAML because `*` starts an alias. Every app start and test fails while parsing config, before any env-var override is applied.
- The docs describe pgvector search, embeddings, chunking, `SimpleVectorStore` and `POST /documents/index-pending`. None of these exist in code. The Ollama `EmbeddingModel` bean is auto-configured but unused.
- `/review`: GET returns the page, POST is the JSON API. The documents page is `/view-documents`. `/documents` returns JSON (the frontend guide says otherwise).
- `upload.html` treats any HTTP 200 as success. Upload failures, which `DocumentController` returns with status 200, still show "Document uploaded successfully!".
- `GET /documents/{id}` returns 200 with an empty body for unknown IDs (service returns `null`).
- `src/test/resources/application-test.yml` is empty and no profile is activated, so the test runs against the main `application.yml` and needs a real DB.
- Uploads are decoded with `new String(file.getBytes())`, which uses the platform default charset.
- Templates build HTML with `innerHTML` template strings. `upload.html` inserts `doc.name` without escaping, while `review.html` and `documents.html` use an `escapeHtml` helper. Escape any new interpolated values.

## Known issues
- `sh mvnw -B -ntp test` (JDK 17): 1 run, 0 failures, 1 error, 0 skipped. `ApplicationTests.contextLoads` fails with `IllegalStateException: Failed to load ApplicationContext`. The cause is `org.yaml.snakeyaml.scanner.ScannerException: while scanning an alias ... password: ********** ... unexpected character found *(42)` at `application.yml` line 16.
- A second run used a scratch copy of the config with a dummy password (`SPRING_CONFIG_LOCATION=file:...`, repo files untouched). It then fails with `JDBCConnectionException: Unable to open JDBC Connection for DDL execution [Connection to localhost:5433 refused ...]`. The test needs a running Postgres.
- `sh mvnw test` with JDK 26 fails at compile (see Gotchas).

## Where things are documented
- `README.md`: author notes, screenshots, setup steps, endpoint overview. Its keyword-retrieval description matches the code. It also contains pasted chat-assistant text.
- `RAG_IMPLEMENTATION.md`: describes a pgvector/embedding pipeline and endpoints that are not implemented.
- `THYMELEAF_FRONTEND_GUIDE.md`: pages, data flows, styling. It was written on Windows (`c:\Users\Lenovo\...`) and mentions `SimpleVectorStore` (not in code).
- `setup-rag.sql`: enables the pgvector extension. Everything else in it is comments.

## Current status / next steps
- Last code commit (2026-03-28) added the Thymeleaf frontend. Everything after it changed only the README.
- The README "Future Improvements" list and the `VectorStoreConfig` placeholder both name pgvector semantic search as the planned next step.
