# Feature: Campaign Close settle flow (US29 write, P70–P73)

## Objective
Let the producer settle the harvest of a plot for the current campaign from the app (P70 plot picker → P71 form → P72 confirm → P73 receipt), online or offline, following the Figma design.

## Problem / Why
The app can only read settlements (Logbook, release 0.11.0). There is no way to settle a campaign, so US29 is incomplete. The team decided that Figma is the source of truth and the backend is being extended in parallel by another agent.

## Sources
- Figma `Hg6RBFx6HAqYACFcGwDtjU`:
  - P70 plot picker sheet over Home: `349:75640`
  - P71 form: `348:26464`; P71 invalid kilos: `350:26949`
  - P72 confirm: `348:75622`; P72 409: `350:27032`
  - P73 closed: `349:26470`; P73 offline: `350:27129`
- Report: US29, TS39 and the tactical DDD offline sync (`pending_settlements`, `SettlementSyncWorker`, idempotency header). Pending report edits: `ma-viora-report/docs/us29-ts39-cambios-pendientes.md`.
- Backend contract (target, being implemented in `ma-viora-platform`):
  - `POST /api/v1/plots/{plotId}/harvest-settlements`, header `Idempotency-Key` (optional, ≤64).
  - Body: `{campaignYear, greenOlivesKg, blackOlivesKg, weighedOn (LocalDate, not future), millTicketNumber? (≤30), commercialFruitsPerKg?, notes?}`.
  - Responses:
    - 201 created; 200 replay with the same key (original settlement, no new receipt).
    - 400 validation (incl. future `weighedOn`); 403; 404; 422 key reused for another plot/campaign.
    - 409 `HARVESTSETTLEMENT_CONFLICT` with ProblemDetail property `existingSettlement {campaignYear, totalYieldKg, receiptNumber, weighedOn}`.
  - `HarvestSettlementResource` adds `receiptNumber` (`VR-{yy}-{nnnn}`), `weighedOn`, `millTicketNumber?`, `commercialSizeGrade?` ("101/110").

## Decisions
- Only the producer settles. P72-409 copy: "Ya asentaste la cosecha {year} de {plot} el {date}." (team decision; Figma still says "La almazara asentó…").
- Receipt number is issued by the server only. While pending offline, the app shows "Comprobante por emitir · Pendiente".
- `weighedOn` is editable, not in the future, and is the visible settlement date. `settledAt` stays as audit.
- New response fields are parsed as nullable so the app keeps working against the current backend until the new contract is deployed.
- Entry point: a Home "AHORA · COSECHA" card with "{n} lotes por registrar" and "Registrar cosecha →". It is contributed by `harvestsettlement` through the existing `HomeScreen` `sections` slot. The Logbook has no "+" menu and is not an entry point.
- "Por registrar" for campaign Y = active plot without a settlement for Y and without a pending local settlement for Y.
- Offline: an Offline/Timeout POST is stored in a Room `pending_settlements` table and sent by a WorkManager `SettlementSyncWorker` when the network is back. The `Idempotency-Key` is generated once per draft and reused on every retry. Pending entries can be corrected ("Corregir los kilos") or are replaced on edit; after sync they are immutable.
- DB goes to v4 with `MIGRATION_3_4`. It also creates `agroclimatic_incidents` IF NOT EXISTS: `52bd186` (develop) added that table to the v3 schema without a migration, so phones on 0.11.0 would fail to open the DB.
- Calibre de venta: grade dropdown ("101/110 · frutos por kg") sends a representative number (grade midpoint), plus free numeric entry as the helper text suggests.

## Scope
- In:
  - harvestsettlement domain, data and contract changes; offline queue; P70–P73 UI with every state in the frames.
  - Home entry card; navigation; en/es strings; unit tests.
- Out:
  - Mill actor; settlement edit/delete after sync; settlement detail screen beyond P73.
  - "2027 · año más estable" as a computed value (static copy unless the backend provides it).
  - Report and backend edits.

## Checks
- TDD: off (no project/session configuration). Runner: `./gradlew testDebugUnitTest`.
- Per task: `./gradlew testDebugUnitTest assembleDebug`.
- RDD: off (global).
- Delivery: forecast ~2500–3500 authored lines, over the 400 guideline. Same delivery as previous features: work-unit commits on `feature/campaign-close-settlement`, merged into `develop` via gitflow.

## Tasks
- [x] T1 — Contract and data (online): domain fields, DTOs (nullable new fields), settle request DTO, `@POST` with `@Header("Idempotency-Key")`, `ProblemDetailDto` + `AppError.Conflict` carrying `existingSettlement`, repository `settle` (upsert cache on success), use case; tests. Route: delegated writer (writer trigger: 2+ non-trivial files).
- [ ] T2 — Offline queue: WorkManager + Hilt work deps, `pending_settlements` entity/DAO, DB v4 + `MIGRATION_3_4` (incl. incidents), `SettlementSyncWorker`, enqueue on Offline/Timeout, observe/edit/discard pending; tests. Route: delegated writer.
- [ ] T3 — P70–P72 UI: Home harvest card + P70 sheet, P71 form (date picker, kilos, live total/t/ha/split bar, mill ticket, calibre), P72 confirm + 409 dialog, ViewModel, strings; tests. Route: delegated writer.
- [ ] T4 — P73: closed receipt (online) and offline pending screen, route, "Listo", "Ver expediente del lote", "Corregir los kilos", "Ver comprobante" from 409; tests. Route: delegated writer.

## Progress
- Branch `feature/campaign-close-settlement` created from `develop` (`52bd186`).
- T1 done in `92c07eb` (~622 authored lines, ~330 of them tests; 18 new tests). Writer: `./gradlew testDebugUnitTest assembleDebug` green (313 tests, 0 failures). Parent spot check: `harvestsettlement` + `ApiCallerTest` tests green.
  - `AppError.Conflict` gains `properties: JsonObject?` (non-standard ProblemDetail members on 409).
  - `settle(draft, idempotencyKey)` returns `SettleOutcome.Settled` (200/201, upserted in Room) or `AlreadySettled(existing: SettlementSummary?)` on 409 (from `existingSettlement`, else the cached row, else null). Other errors pass through; nothing is queued yet.
  - Null optionals are omitted from the request (`explicitNulls=false`).
  - Room schema unchanged: cached rows map the four new fields to null until T2 adds the columns.

## Next step
T2: add the four columns to `HarvestSettlementEntity` in v4 as well as the pending queue.
