# Feature: Android project setup (Viora mobile)

## Objective
Leave the `viora-mobile-android` repository ready to open in Android Studio: Gradle configuration, dependency catalog, app entry points, core theme/navigation/network, and the DDD + Clean Architecture package skeleton for every bounded context. No business features.

## Problem / Why
The repository is empty. The team needs a shared base that follows the course architecture guide (`mobile-arquitecture-guide.pdf`) and the professor's sibling project (`zapa-facil`) before implementing features.

## Sources
- Architecture guide: bounded contexts under `features/`, 4 layers per context (`presentation/{ui,viewmodel,state}`, `application/{usecase,service}`, `domain/{entity,valueobject,repository}`, `infrastructure/{local,remote,repository,mapper,di}`), `core/` for cross-cutting concerns. `domain` and `application` are pure Kotlin.
- `zapa-facil`: Gradle version catalog, AGP 9.4.1, Kotlin 2.4.20, Compose BOM 2026.09.00, Hilt + KSP, Retrofit, Coil, compileSdk 37, Java 11.
- `ma-viora-report`: 9 bounded contexts, all consumed by the mobile app (C4 "Android Application Components" maps every Android feature to one of them); Room for offline cache; minSdk 26; brand colors Forest `#2E4A3A`, Shadow `#1F2C26`, Harvest `#E8B923`, Tierra `#C15A2E`; GitFlow + Conventional Commits.

## Decisions
- Package / applicationId: `pe.edu.upc.viora` (guide pattern `pe.edu.upc.<app>`).
- compileSdk/targetSdk 37: confirmed by the user (2026-09-26). Only `android-37` is installed locally and it matches `zapa-facil`; the report (which says 34) must be updated.
- Pending user decisions (do not change yet): final set of bounded contexts for mobile scope; cleartext/network security config for `API_BASE_URL`.
- minSdk 26 (report).
- Bounded context packages: `iam`, `profiles`, `subscription`, `plotmanagement`, `telemetry`, `phenology`, `croploadregulation`, `harvestsettlement`, `cooperativeoperations`.
- Empty layer folders are kept in Git with `.gitkeep`.

## Scope
- In: Gradle files, wrapper, version catalog, manifest, resources, `VioraApplication`, `MainActivity`, `core/{theme,navigation,network}`, package skeleton, `.gitignore`, README.
- Out: screens, use cases, entities, Room database, API endpoints.

## TDD
Mode: off (no project/session TDD configuration). Checks: `./gradlew assembleDebug` and `./gradlew testDebugUnitTest`.

## Tasks
- [x] T1 — Scaffold project configuration, core and bounded context skeleton (route: delegated direct — writer trigger: 2+ non-trivial files).

## Acceptance criteria
- `./gradlew assembleDebug` succeeds.
- The package tree matches the guide for the 9 bounded contexts.
- No `android.*` imports in any `domain`/`application` package.

## Progress / evidence
- Branch: `feature/project-setup`.
- T1 checks: `./gradlew assembleDebug` BUILD SUCCESSFUL; `./gradlew testDebugUnitTest` BUILD SUCCESSFUL; no `import android` under `features/`.
- Added versions: navigation-compose 2.9.0, room 2.7.2, coroutines 1.10.2, serialization-json 1.7.3, okhttp logging 4.12.0.
- Known follow-up: `API_BASE_URL` uses cleartext `http://10.0.2.2`; add a debug network security config when the first API call is wired.
- Review corrections (reviewer pass):
  - README: bounded context descriptions and app intro rewritten to match the report canvases (e.g. `profiles` is personal/contact data, not cooperative members; `harvestsettlement` is harvest closure and dossier, not financial settlement); architecture rules aligned with the guide (presentation and infrastructure depend on application/domain; no persistence/network libraries in domain/application; business validation in entities/value objects).
  - `NetworkModule`: debug logging interceptor redacts the `Authorization` header (report C4: no tokens in logs).
  - Re-verified: `assembleDebug testDebugUnitTest` BUILD SUCCESSFUL; no `import android` under `features/`.
- Engram mirror: pending (project `ma-android-app` not registered in Engram).

## Next step
Implement the first feature (suggested: `iam` sign-in) on a new `feature/*` branch.
