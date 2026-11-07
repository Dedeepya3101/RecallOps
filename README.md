# RecallOps

RecallOps is an incident debugging assistant for teams using services such as Spring Boot, React, and MySQL. A Spring Boot API sanitizes incident details, retrieves prior incidents from Hindsight, asks an LLM for an evidence-based diagnosis, and stores a resolution only after a developer marks it successful.

## Architecture

- `src/`: React 18 + Vite dashboard. It calls only the backend; provider credentials never enter the browser.
- `backend/`: Spring Boot 3.4 REST API with validation, CORS, provider clients, redaction, in-process incident history, and workflow tests.
- Hindsight: documented HTTP API; one persistent bank ID (`recallops-incidents` by default) scopes incident memories.
- LLM: OpenAI Responses API with strict JSON Schema output. Model output separates current facts, hypotheses, missing evidence, and diagnostic steps.

The dashboard shows clearly labeled sample workspace data at first. Incident analysis always goes through the backend; API or provider failures are shown as errors and never replaced with sample diagnoses.

## Prerequisites

- Java 21 or newer (Java 24 was present during initial setup).
- Maven 3.9 or newer. A temporary Maven 3.10.0 distribution was used for local verification; install Maven or provide it on `PATH` for regular development.
- Node.js 20.19+ or 22.12+ (Node 24.4 was present) and npm 10+.
- A reachable Hindsight instance and API key for persistent memory.
- An OpenAI API key for live analysis. Provider usage may incur charges according to the provider account.

## Environment

Copy `.env.example` to `.env` for reference, then set real values in your local environment or a secret manager. Never commit `.env` or put provider keys in frontend variables. Only variables prefixed with `VITE_` are exposed to browser code.

| Variable | Required | Description |
| --- | --- | --- |
| `OPENAI_API_KEY` | Live analysis | Key used by the Spring Boot server. |
| `OPENAI_MODEL` | No | Model name; defaults to `gpt-4.1-mini`. |
| `HINDSIGHT_URL` | Memory | Hindsight API base URL; defaults to `http://localhost:8888`. |
| `HINDSIGHT_API_KEY` | Memory | Hindsight Bearer token. Leave empty to explicitly disable memory calls. |
| `HINDSIGHT_BANK_ID` | No | Persistent incident bank; defaults to `recallops-incidents`. |
| `PORT` | No | API port; defaults to `8080`. |
| `FRONTEND_ORIGIN` | No | Allowed frontend origin; defaults to `http://localhost:5173`. |
| `VITE_API_BASE_URL` | No | Browser-visible API URL; defaults to `http://localhost:8080`. |

## Run the applications

From the repository root:

```sh
npm install
npm run dev
```

In a second terminal, configure the environment above and run:

```cmd
cd /d D:\RecallOps\backend
set "OPENAI_API_KEY=your-key-here"
"D:\apache-maven-3.10.0\bin\mvn.cmd" spring-boot:run
```

Set the key in the same CMD window that starts Spring Boot. To persist it for future terminals, use `setx OPENAI_API_KEY "your-key-here"`, then open a new CMD window; `setx` does not update already-running processes. Check key presence without printing its value at `http://localhost:8080/api/config`.

The dashboard is at `http://localhost:5173`. API health is available at `http://localhost:8080/api/health`.

## API

- `POST /api/incidents/analyze`: validated current incident; returns diagnosis, evidence, historical context, and whether Hindsight retrieval was available.
- `POST /api/incidents/{id}/feedback`: records `successful` or `unsuccessful`. Only successful feedback triggers Hindsight retain.
- `GET /api/incidents/history`: current process incident history.
- `GET /api/health`: liveness response.
- `GET /api/config`: reports whether the backend received an OpenAI key; never returns its value.

## Memory and privacy

RecallOps calls Hindsight `POST /v1/default/banks/{bank_id}/memories/recall` and retains verified resolutions with `POST /v1/default/banks/{bank_id}/memories`. Recall is labeled unavailable when the service is not configured or cannot be reached. Unsuccessful feedback is never retained. The backend redacts common password, token, Bearer, AWS access-key, and MySQL URL credentials before provider calls and before retaining summaries.

Review your own data-handling requirements before sending logs to any hosted model or memory service. Sanitization is best-effort and cannot detect every secret format.

## Tests and repeatable workflow

Run backend tests from `backend/`:

```sh
mvn test
```

The service tests cover a database connection failure, retrieval context passed to the model, unavailable-memory behavior, secret redaction, and the feedback gate. For a live two-session verification, submit a first DB connection failure, verify its resolution through feedback, then submit a similar failure in a separate app process/session and inspect the returned related memories. Repeat with Hindsight unavailable and confirm `memoryAvailable=false` and an empty memory list. Live verification requires configured provider credentials and a running Hindsight service; no live-provider results are claimed here.

## Known limitations

- Incident history is in memory and resets when the API restarts; Hindsight stores retained verified resolutions durably.
- Hindsight unavailable errors are surfaced through `memoryAvailable`; the primary diagnosis can proceed without memories.
- Provider failures stop analysis and return an API error rather than showing a fabricated diagnosis.
- Secret redaction uses common patterns and is not a substitute for review or a dedicated DLP system.
- Live Hindsight/OpenAI integration was not exercised because no provider credentials or services were supplied.
