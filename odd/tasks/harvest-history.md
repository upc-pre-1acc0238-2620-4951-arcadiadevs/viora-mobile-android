# Feature: Historical harvests & biennial bearing index (US20, P40–P41)

## Objective
Let the producer register past campaign yields (kg) per plot and see the plot's Hoblyn biennial bearing index (BBI): a gauge with its class, the yield per campaign chart (ON/OFF years), the index per interval and the list of registered campaigns, with add, correct and delete.

## Problem / Why
Viora's thinning plan depends on whether the plot alternates (ON/OFF years). Without past harvests the app cannot say how strong the alternation is. The backend already computes the BBI; the app has no harvest domain yet.

## Sources
- Figma `Viora202602_Mobile_App` → Mobile Mockups → "13 · Conocer la vecería de mi lote": P40 Alternancia (296:26108), P41 Cosecha histórica (298:26154), Campaña agregada (300:26176), Corregir campaña (300:26479), Historial insuficiente (301:26248), Fuera de rango (301:56328), Eliminar campaña (302:26319), ¿Qué es el BBI? (303:26345), Sin conexión (303:56438).
- Backend `viora-platform` phenology context:
  - `GET/POST /plots/{plotId}/harvest-records`, `PUT/DELETE /plots/{plotId}/harvest-records/{recordId}`. `HarvestRecordResource {id, plotId, campaignYear, totalYieldKg, greenKg, blackKg, bearingClassification (ON_YEAR|OFF_YEAR|BALANCED|INSUFFICIENT_DATA), calculatedBbi, recordedAt}`.
  - `GET /plots/{plotId}/metrics?metricName=BIENNIAL_BEARING_INDEX` → `MetricResource {metricName, value, qualitativeCategory, details{evaluatedYearsCount, sampleSufficiency}, evaluatedAt}`.

## Decisions
- **Figma is the reference.** BBI classes follow Figma: low < 0.20, moderate 0.20–0.40, severe > 0.40. The app classifies the index value itself and ignores the backend's `qualitativeCategory` (its thresholds are 0.25 / 0.50). Backend follow-up: align `HoblynBbiCalculatorService.classifyAlternation` to Figma.
- **Yield in kg** (user request). Figma shows t/ha; the app shows kg everywhere (chart, list, sheet). The index is a ratio, so kg or t/ha give the same value.
- The index is the backend's value; a pure domain `HoblynBbi` mirrors the formula for the live preview in the sheet (add, correct, delete) and the per-interval chips.
- Minimum 3 campaigns for the index (Figma "Historial insuficiente"); with fewer, the gauge is replaced by the "2 of 3 campaigns" card.
- Campaign year range: 2000 … current year (Figma "Fuera de rango"); kilos must be > 0.
- Offline-first like telemetry: Room caches the records; editing needs a connection (the index is recalculated on the server), as in Figma "Sin conexión".
- **Left out:** the 2026 "proy." projection bar (the backend has no projection), the P10 home card "Tu alternancia" and the suggestion chip (home is another feature).
- Entry point: the plot detail options sheet ("Alternancia"), next to sensors.

## Scope
- In: phenology domain, data layer (Retrofit, Room, mapper, repository, DI), P40 screen and its states, P41 add/correct sheet, delete dialog, BBI info sheet, route and entry from plot detail, en/es strings, unit tests.
- Out: projection, home card, backend change.

## Checks
- TDD: off (no project/session configuration). Runner: `./gradlew testDebugUnitTest`.
- Per task: `./gradlew testDebugUnitTest assembleDebug`.
- RDD: off (global).

## Tasks
- [x] T1 — Domain + data: `HarvestRecord`, `BbiClass`, `HoblynBbi` (index, intervals, classification), DTOs, `PhenologyService`, Room entity/DAO, mapper, repository (observe/refresh/add/rectify/remove), use cases, Hilt modules; tests. Route: delegated writer.
- [x] T2 — P40 screen: `HarvestHistoryRoute`, view model, UI state, gauge, chart, interval chips, campaign list, insufficient/offline/empty states, BBI info sheet, entry from plot detail, strings; tests. Route: delegated writer.
- [x] T3 — P41 sheet: add/correct with validation and live preview, delete dialog, "campaign added" toast; tests. Route: delegated writer.

## Decisions (T2)
- Insight cards are data-driven: the "after an ON year" card shows the real ratio (e.g. 31 %) of the latest ON to OFF pair instead of Figma's fixed 1/3; hidden when there is no such pair or the class is low. The "next campaign" card has no thinning-plan arrow (no target screen yet).
- Row subtitle shows "recorded on {date}" (server `recordedAt`) instead of Figma's static "mill receipt" text; chart bar labels are compact ("7,8k") so they fit the 44 dp bars, full kg stay in the list and in the accessibility description.
- Missing-year placeholders of the insufficient variant (dashed "?" bar, "Falta esta campaña" row) are left out; the progress card and the add button cover it.
- Only Offline/Timeout refresh errors switch to the offline variant; other errors keep the editable cache.
- `VioraSectionHeader` got an optional `count` badge (Figma "Contador"). Hand-authored Material paths for `ic_bar_chart` and `ic_warning` (no Figma asset URLs in code).

## Progress
- Branch `feature/harvest-history` created from `develop`.
- T1 done in `e399ec2` (1116 authored lines, 22 new tests). `./gradlew testDebugUnitTest assembleDebug` green; parent spot check of `features.phenology.*` tests green. Index cached in its own `bearing_indexes` table; metrics 404 → no index; `observeLastRefresh` added for the offline state (cache key `harvest:{plotId}`).
- T2 done in `a6adf99` (2393 authored lines, 22 new tests). `./gradlew testDebugUnitTest assembleDebug` green (226 tests); `lintDebug` reports 6 pre-existing NonObservableLocale errors in Home*/SensorsScreen, none in phenology. Delete (200 + body, 412) verified in `remove()` with two new repository tests; no code fix needed because `callUnit` ignores the body.
- T3 done in `68daf0c` (1392 authored lines, 31 new tests). `./gradlew testDebugUnitTest assembleDebug` green (257 tests); `lintDebug` still reports only the 6 pre-existing NonObservableLocale errors, none in phenology. Figma example verified in kg: adding 2021 = 10 500 kg to 24 000 / 7 750 / 22 000 / 6 750 gives 0,51 → 0,48 (also 2024 corrected to 20 500 → 0,49 and deleting 2021 → 0,51).

## Decisions (T3)
- Kilos field reads "20500", "20 500", "20.500" and "20,500" as 20 500 (a separator followed by exactly 3 digits groups thousands); the year of a registered campaign cannot be changed when correcting (the API only rectifies the yield); saving a correction needs a changed yield.
- Preview before-value is the server index (local formula if missing); with fewer than 3 campaigns the sheet says how many of 3 the campaign would reach.
- Confirmation after save/delete is the green pill in the header (Figma "Campaña agregada") for 3.5 s; only an added row is marked "agregada ahora".

## Next step
Parent review and device check.
