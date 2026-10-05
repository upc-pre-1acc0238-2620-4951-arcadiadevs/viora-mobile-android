# Feature: Logbook with settled harvests (US29 query, P50)

## Objective
Show the campaigns the producer already settled ("Cosecha asentada") in the Logbook tab (P50 Bitácora), fed by the new backend query `GET /api/v1/plots/{plotId}/harvest-settlements`.

## Problem / Why
The Logbook tab is a `PlaceholderScreen` and `features/harvestsettlement` is empty. Settled campaigns can be created on the backend (POST) but the app cannot read them back.

## Sources
- Figma `Viora202602_Mobile_App` (file `Hg6RBFx6HAqYACFcGwDtjU`) → P50 Bitácora (`474:213589`).
- Backend `viora-platform` settlement context (query endpoints being built in parallel by another agent):
  - `GET /api/v1/plots/{plotId}/harvest-settlements` → `200 HarvestSettlementResource[]`, newest campaign first, `[]` when the plot has no settlement; `403` not owner, `404` plot not found/not active.
  - `GET /api/v1/plots/{plotId}/harvest-settlements/{campaignYear}` → `200 HarvestSettlementResource` (not used yet).
  - `HarvestSettlementResource {id, reportId, plotId, campaignYear, greenOlivesKg, blackOlivesKg, totalYieldKg, commercialFruitsPerKg?, notes?, status (SETTLED|AUDITED), settledAt, thinningBalance{status (EXECUTED_ON_TIME|EXECUTED_LATE|NOT_RECORDED), executedDate?, prescribedRemovalPercentage?, actualRemovalPercentage?, deviationPercentagePoints?}, stabilization{status (EVALUATED|INSUFFICIENT_BASELINE|INSUFFICIENT_SETTLEMENTS|NO_BASELINE_ALTERNATION), baselineCampaigns, settledCampaigns, baselineYieldKg?, baselineAlternationIndex?, managedAlternationIndex?, amplitudeReductionRate?, targetAchieved?, interannualVarianceKg2?, coefficientOfVariation?, requiredConsecutivePairs}}`.

## Decisions
- Scope decided by the assistant (user delegated "encárgate de la app" without picking): only the US29 slice of P50. The Logbook shows settled harvests across all the producer's plots; the Muestreos and Aclareos filters show their empty state until those features have data.
- **Left out:** the "en curso" sampling card, the "N en el teléfono" sync pill, sampling/thinning/note entries, the "+" actions menu targets, the settlement flow (POST, P70–P73) and a settlement detail screen.
- Offline-first like phenology: Room caches settlements; the list is read from the cache and refreshed per plot.

## Scope
- In: harvestsettlement domain, data layer (Retrofit, Room, mapper, repository, DI), use cases, P50 screen with the harvest entries and filter chips, route replacing the placeholder, en/es strings, unit tests.
- Out: everything under "Left out".

## Checks
- TDD: off (no project/session configuration). Runner: `./gradlew testDebugUnitTest`.
- Per task: `./gradlew testDebugUnitTest assembleDebug`.
- RDD: off (global).
- Delivery strategy: ask-on-risk; forecast ~1500–2500 authored lines (over the 400 guideline, as in harvest-history).

## Tasks
- [x] T1 — Domain + data: `HarvestSettlement` (+ thinning balance and stabilization value types), DTOs, `HarvestSettlementService`, Room entity/DAO in `AppDatabase`, mapper, repository (observe all, refresh per plot / all plots), use cases, Hilt module; tests. Route: delegated writer (writer trigger: 2+ non-trivial files).
- [x] T2 — P50 Logbook screen: header, filter chips, "Cosecha asentada" entries grouped by period, empty/offline/error states, replace `PlaceholderScreen` in `AppNavHost`, en/es strings; tests. Route: delegated writer (writer trigger).

## Progress
- Branch `feature/logbook-harvest-settlements` created from `develop`.
- T1 done in `67df5e6` (~770 authored lines + regenerated Room schema, 12 new tests). `./gradlew testDebugUnitTest assembleDebug` green (writer); parent spot check of `features.harvestsettlement.*` tests green. Decisions: path `plots/{plotId}/harvest-settlements` like `PhenologyService`; unknown enums fall back to SETTLED / NOT_RECORDED / INSUFFICIENT_SETTLEMENTS; one `harvest_settlements` table with flattened value objects, `replaceForPlot` atomic so `[]` clears a plot; `refreshAll` keeps successful plots and returns the first failure; no refresh timestamp yet.
- T2 done in `a6b31c7` (867 insertions incl. 305 test lines, 10 new tests). `./gradlew testDebugUnitTest assembleDebug` green (writer); parent spot check of `LogbookViewModelTest` green; `lintDebug` errors are only the 6 pre-existing NonObservableLocale (HomeHeader, HomeWeekStrip, SensorsScreen).

- Rebased on `origin/develop` (9 commits incl. `ffdb038` Room v2 migration). T1 now bumps `AppDatabase` to version 3 with `AppMigrations.MIGRATION_2_3` creating `harvest_settlements`; its SQL matches the exported `3.json` createSql byte for byte; `1.json`/`2.json` untouched. String conflicts were additive (kept both sides, CRLF preserved). After rebase `./gradlew testDebugUnitTest assembleDebug` green (295 tests, 0 failures).

## Decisions (T2)
- Groups by `settledAt` in the device zone: "Esta semana" (calendar week from Monday), "Este mes", "Anteriores"; newest first; empty groups hidden.
- Row: "Cosecha asentada", subtitle "{plot} · campaña {year} · {kg} kg" (phenology `formatKg`), " · auditada" when AUDITED, unknown plot shows "Lote". Rows are not clickable.
- Plot ids from the cached plots; with no cache the plots are refreshed first and a failure there stops the settlement refresh.
- Header reuses `HomeHeader`/`HomeHeadline`. Any refresh failure shows a tappable banner over the cached list (offline flag only for Offline/Timeout); pull-to-refresh supported.
- `strings.xml` files are CRLF in the repo; the commit preserves CRLF to avoid a whole-file diff.
- `ic_task_alt` hand-authored from the baseline Material path (Figma uses the Rounded variant).

## Next step
Shipped in release 0.11.0. Device check once the backend GET is deployed.
