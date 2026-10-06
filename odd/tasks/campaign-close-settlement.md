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
- DB goes to v5 with `MIGRATION_4_5` (develop's v4 is the teammate's telemetry migration and stays untouched). It also creates `agroclimatic_incidents` IF NOT EXISTS: `52bd186` (develop) added that table to the v3 schema without a migration, so installs from 0.11.0 (v3) lack it, and a 3→4→5 upgrade gets it in 4→5 before Room validates.
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
- [x] T2 — Offline queue: WorkManager + Hilt work deps, `pending_settlements` entity/DAO, DB v5 + `MIGRATION_4_5` (incl. incidents), `SettlementSyncWorker`, enqueue on Offline/Timeout, observe/edit/discard pending; tests. Route: delegated writer.
- [x] T3 — P70–P72 UI: Home harvest card + P70 sheet, P71 form (date picker, kilos, live total/t/ha/split bar, mill ticket, calibre), P72 confirm + 409 dialog, ViewModel, strings; tests. Route: delegated writer.
- [x] T4 — P73: closed receipt (online) and offline pending screen, route, "Listo", "Ver expediente del lote", "Corregir los kilos", "Ver comprobante" from 409; tests. Route: delegated writer.

## Progress
- Branch `feature/campaign-close-settlement` created from `develop` (`52bd186`).
- T1 done in `92c07eb` (~622 authored lines, ~330 of them tests; 18 new tests). Writer: `./gradlew testDebugUnitTest assembleDebug` green (313 tests, 0 failures). Parent spot check: `harvestsettlement` + `ApiCallerTest` tests green.
  - `AppError.Conflict` gains `properties: JsonObject?` (non-standard ProblemDetail members on 409).
  - `settle(draft, idempotencyKey)` returns `SettleOutcome.Settled` (200/201, upserted in Room) or `AlreadySettled(existing: SettlementSummary?)` on 409 (from `existingSettlement`, else the cached row, else null). Other errors pass through; nothing is queued yet.
  - Null optionals are omitted from the request (`explicitNulls=false`).
  - Room schema unchanged: cached rows map the four new fields to null until T2 adds the columns.

- T2 done in `8865fd8` (DB v5: receipt columns, `pending_settlements`, `MIGRATION_4_5` incl. `agroclimatic_incidents` IF NOT EXISTS; SQL asserted byte for byte against 5.json) and `e5226fd` (offline queue + WorkManager). ~1770 lines incl. ~400 generated schema and ~450 tests; 26 new tests. Writer: `./gradlew testDebugUnitTest assembleDebug` green (339 tests). Parent spot check: `AppMigrationsTest` + `SettlementQueueTest` green.
  - Deps: `work-runtime-ktx` 2.12.0, `hilt-work` 1.4.0, `hilt-compiler` 1.4.0 (ksp). `VioraApplication` provides `HiltWorkerFactory`; default initializer removed in the manifest.
  - `settle(draft, key?)` → `Settled` | `AlreadySettled(existing)` | `Queued(pending)`. Key is stable per plot/year draft and generated before the first online attempt.
  - Sync rules in `HarvestSettlementRepositoryImpl.syncPending()`; worker is a thin shell (no worker test: no Robolectric/work-testing). Offline/timeout/5xx/401 retry; 400/403/404/422 → FAILED (editing resets to PENDING); 409 → CONFLICT row with the server summary, dismissed via `discardPending`. Stale results never overwrite an edit made mid-sync.
  - "Por registrar" must exclude plots with a PENDING or FAILED row; CONFLICT rows show the P72-409 content.

- T3 done in `cf077e2` (Home harvest card + P70 picker) and `c2669b1` (P71 form + P72 confirm/409 dialogs). ~2460 authored lines (UI + 35 new tests). Writer: `./gradlew testDebugUnitTest assembleDebug` green (374 tests); `lintDebug` 0 errors, no warnings in new files besides es plural `MissingQuantity` (same as existing). Parent spot check: settle/entry/home tests green. Not checked on a device.
  - Two ViewModels: `HarvestEntryViewModel` (Home card + P70) and `SettleHarvestViewModel` (P71 route `SettleHarvestRoute(plotId, campaignYear)` + P72 dialogs). One idempotency key per form screen.
  - Grade table: 17 grades from the backend `CommercialSizeScale` (60/70 … 381/410); a grade sends its midpoint; typed 1–999.
  - Deviations: Home card without the illustration and "Siguiente" tab, progress = registered/total; "kg" suffix at the field edge; split caption under the bar; calibre placeholder; "Entendido" also closes the form; validation shows the generic mapped message.
  - Temporary wiring in `AppNavHost`: `onSettled`/`onQueued` pop to Home, `onViewReceipt` opens the Logbook.

- T4 done in `a01d353` (P73 closed/offline receipt, `CampaignClosedRoute`) and `90f2264` (edit pending before sync). ~1150 lines incl. ~310 tests; 16 new tests. Parent check on the final tree: `./gradlew testDebugUnitTest assembleDebug` green, 390 tests, 0 failures. Writer: `lintDebug` no errors (warnings not counted).
  - States: Loading, Closed, Pending (failed flag), Conflict, Missing. CONFLICT wins over a cached settlement, which wins over PENDING/FAILED. Pending switches to Closed live when the sync lands.
  - Fallbacks: no `receiptNumber` → number pill hidden; no `weighedOn` → `settledAt` (device zone). Calibre tile from `commercialSizeGrade`, else the grade containing `commercialFruitsPerKg`; hidden when absent.
  - Navigation: settle/queue/409 receipt open `CampaignClosedRoute` popping the form; "Listo" → Home; "Ver expediente del lote" → `PlotDetailRoute`; alternation tile → `HarvestHistoryRoute`; "Corregir los kilos" → form in edit mode (`UpdatePendingSettlementUseCase`, same key; `NotFound` → closed receipt). Logbook "Cosecha asentada" rows now open the receipt.
  - Extras not in Figma: "Por corregir" pill and error footer for FAILED rows; Missing screen ("No encontramos este comprobante").
- Branch total vs `52bd186` (excluding schemas): 70 files, +5343 / −26.

## Follow-ups
- Logbook does not list pending local settlements ("N en el teléfono").
- Edit mode keeps the normal form title/button; closing it returns to Home, not P73.
- Validation errors show the generic message, not the backend detail.
- Device check of P70–P73 against Figma, and an end-to-end run once the backend contract (receipt, `weighedOn`, `Idempotency-Key`, 409 body) is deployed.
- Teammate notice: `52bd186` changed the v3 schema without a migration; fixed here by `MIGRATION_4_5`.
- Rebased onto develop `d272dfc` (teammate's v4 = `telemetry_readings` + `forecast_days`, untouched). Our schema moved to v5: `MIGRATION_4_5` + exported `5.json`; `AppMigrationsTest` asserts against v5. Strings and `AppNavHost` conflicts kept both sides.

## Next step
Merge into `develop` (gitflow) when the user approves.
