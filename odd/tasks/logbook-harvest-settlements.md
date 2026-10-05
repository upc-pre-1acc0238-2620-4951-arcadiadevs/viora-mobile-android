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
- [ ] T2 — P50 Logbook screen: header, filter chips, "Cosecha asentada" entries grouped by period, empty/offline/error states, replace `PlaceholderScreen` in `AppNavHost`, en/es strings; tests. Route: delegated writer (writer trigger).

## Progress
- Branch `feature/logbook-harvest-settlements` created from `develop`.
- T1 done in `b88c331` (~770 authored lines + regenerated Room schema, 12 new tests). `./gradlew testDebugUnitTest assembleDebug` green (writer); parent spot check of `features.harvestsettlement.*` tests green. Decisions: path `plots/{plotId}/harvest-settlements` like `PhenologyService`; unknown enums fall back to SETTLED / NOT_RECORDED / INSUFFICIENT_SETTLEMENTS; one `harvest_settlements` table with flattened value objects, `replaceForPlot` atomic so `[]` clears a plot; `refreshAll` keeps successful plots and returns the first failure; no refresh timestamp yet.

## Next step
T2.
