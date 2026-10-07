# Feature: Winter chilling accumulation & Erez dynamic model (US22, P80–P81)

## Objective
Enable olive producers to track winter chilling accumulation (Erez portions) for their plots during the winter dormancy period (June 1st to August 31st), comparing portions against their olive variety threshold (30 portions for Sevillana/Criolla) to anticipate uniform flowering, and understanding why chill units are counted.

## Problem / Why
In olive farming, uniform spring budbreak and flowering require a critical amount of winter chill. Unusually warm winters (such as during coastal El Niño events) cause delayed or purely vegetative budding with zero fruit. While the backend computes the Erez dynamic model metrics, the mobile app currently lacks any winter chill domain, offline persistence, or user interface for tracking chill accumulation.

## Sources
- Figma `Viora202602_Mobile_App` (file `Hg6RBFx6HAqYACFcGwDtjU`) → Mobile Mockups / Prototipo → "15 · Seguir el frío invernal" (`389:80483`):
  - `P80 · Frío invernal` (`389:80486`) with 4 variants: Accumulating, Halted (`389:81492`), Completed (`389:81950`), Off-Season (`389:82409`).
  - `P81 · ¿Por qué cuento frío?` (`389:81359`) bottom sheet explaining the Erez model, daily cycles, and variety threshold.
- Backend `viora-platform` phenology context (`docs_otros/26-tactical-ddd.md`):
  - `GET /api/v1/plots/{plotId}/metrics?name=CHILLING` → `MetricResource { metricName: "CHILLING", value: Double, qualitativeCategory: String, details: Map<String, Object>, evaluatedAt: Instant }`.

## Decisions
- **Figma is the source of truth.** The UI presents the 4 distinct states according to dormancy progress:
  1. *Acumulando*: Hero showing portion count (e.g. 24 de 30), 30-snowflake grid (`ac_unit`), timeline (1 jun – 31 ago), insight cards (El Niño, days > 24°C, projection), and the cumulative curve chart.
  2. *Frío frenado*: Warm days halted portion formation; displays active risk indicators.
  3. *Estímulo completado*: Threshold achieved; success banner indicating complete floral induction.
  4. *Fuera de temporada*: Outside winter; displays dormancy paused and last winter's summary.
- **Offline-First:** Room 3 table `chill_trackers` caches the latest metrics, curve points, and season status. When offline or server fails, the screen displays cached data with a timestamp banner.
- **Variety Threshold:** 30 portions default for Sevillana/Criolla olive trees.
- **Scope limitation:** Thermal anomaly incident alerts (US23) will link to P80, but the standalone incident notification/sheet contract belongs to Fabrizio's delivery.

## Scope
- In: Phenology chill domain, data layer (Retrofit, Room 3 entity/DAO, mapper, repository, Hilt DI), Room 3 migration (v7 → v8), P80 screen with 4 states, P81 bottom sheet, cumulative curve canvas chart, route and navigation wiring, en/es strings, and unit tests.
- Out: Backend calculations (Erez differential equations are evaluated on the server).

## Checks
- Runner: `./gradlew testDebugUnitTest`.
- Verification command: `./gradlew testDebugUnitTest assembleDebug`.

## Tasks
- [x] T1 — Database & Room 3 migration: `chill_trackers` table entity, DAO, Room schema v8 migration `MIGRATION_7_8`, exported `8.json`, migration unit tests. (Commit `591f5`)
- [x] T2 — Domain + Data layer: `ChillTracker`, `WinterSeasonState`, `EnsoRiskLevel`, `ChillCurvePoint`, `ChillRepository`, use cases, Retrofit service, DTOs, mappers, Hilt modules; unit tests. (Commit `0c477`)
- [x] T3 — Presentation: `WinterChillScreen` (P80 with 4 states, 30 snowflake grid, cumulative chart), `WhyCountChillSheet` (P81), `WinterChillViewModel`, `WinterChillNavGraph`, strings (en/es); unit tests. (Commit `8c30f`)
- [x] T4 — Review fixes & integration: localized all hardcoded UI strings, wired winter chill navigation across `PlotDetailScreen`, `HarvestHistoryScreen`, `SensorsScreen`, and `PlotClimateScreen`. (Commit `8a6c7`)
- [x] T5 — Figma pixel-perfect alignment: aligned P80 & P81 screens 100% with Figma nodes `389:80486` & `389:81359`: `EditorialHeadline`, hero card (`Color(0xFF727272)`, 32dp), 3x10 snowflake grid, season timeline track, `VioraVoice`, asymmetric metric cards (tall projection card 252dp + stacked warm days & ENSO), curve chart callout badge ("Hoy · 24", "4 ago · 30"), and P81 equation pills with accent keys. (Commit `0cca0`)

## Progress
- Branch `feature/winter-chilling` created from `develop`.
- `922fd`: docs(odd): add winter chilling tracking specification
- T1 done in `591f5` (~140 authored lines, 1 new test). Added Room 3 schema v8 and MIGRATION_7_8.
- T2 done in `0c477` (~680 authored lines, 18 new tests). Pure domain entity, Room DAO/Entity, Retrofit DTOs, mapper, repository implementation, and use cases.
- T3 done in `8c30f` (~1200 authored lines, 5 new tests). Full Compose UI for P80 (4 states) and P81 (Why count chill sheet), ViewModel with StateFlow, navigation route and localized resources.
- `6b7ca`: docs(odd): complete winter chilling tasks
- T4 done in `8a6c7` (~86 authored lines). Localized all hardcoded UI strings, wired winter chill navigation from PlotDetail card and LotSectionsSheet across phenology and telemetry screens.
- `8aa63`: docs(odd): update winter chilling specification with review fixes
- T5 done in `0cca0` (~1240 authored lines across 7 files). Complete Figma pixel-perfect redesign of P80 & P81.
- `e892f`: fix(phenology): refine background and card colors to match figma palette exactly.
- `1efbf`: fix(phenology): localize date formatting and fallbacks to eliminate hardcoded texts.
- `f44da`: fix(phenology): display floating tab bar and align colors with figma tokens.
- Verification passed: `./gradlew testDebugUnitTest assembleDebug` (100% tests passing, clean debug APK build).

## Next step
Device check and PR review for merge into `develop`.


