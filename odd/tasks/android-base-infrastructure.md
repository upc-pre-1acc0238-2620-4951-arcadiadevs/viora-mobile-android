# Feature: Android base infrastructure (shared foundation)

## Objective
Give the three Android developers (Victor, Diana, Jahat) one shared base to branch from: build/dependencies aligned with the course reference, network and error handling, local database, session storage, design system and the navigation shell with the floating tab bar. No business features.

## Problem / Why
`feature/project-setup` only had an empty scaffold. Without a common network layer, database, theme and navigation shell, each developer would build their own and the merge would be painful.

## Sources
- Teacher reference `easysneaker-mobile-reference` (stack, Room 3, layering, conventions) — improvements applied: typed errors instead of swallowed exceptions, OkHttp client with interceptors, mapper separation, tests.
- Figma `Viora202602_Mobile_App` → "Mobile Prototipo" → App Productor · Kotlin. Tokens read with `get_variable_defs` (primitives: green/harvest/terracotta/neutral); tab bar from component "Editorial/Tapbar flotante".
- Backend `viora-platform` main (tag 0.16.0): RFC 7807 `ProblemDetail` with extra `code`, no auth, fixed producer.

## Decisions
- Scope: Productor only (Gestor is the Flutter app) — confirmed by the teacher via the user.
- Room 3 (`androidx.room3`) + its Gradle plugin; schema exported to `app/schemas/`; never destructive migration (offline queue holds unsynced data).
- Gson kept (as the teacher) with DTO dates as ISO `String`; kotlinx-serialization only for navigation routes.
- `AppResult` / `AppError` (pure Kotlin) + `ApiCaller` as the only way repositories call Retrofit.
- `API_BASE_URL` from `local.properties` (`viora.apiBaseUrl`), debug default `10.0.2.2:8080`; release is a `.invalid` placeholder until the Render URL exists. Cleartext only in the debug source set.
- `allowBackup=false` and backup rules exclude app storage (tokens, offline queue).
- Fonts bundled as variable TTF (Newsreader regular/italic, Roboto), licenses in `docs/licenses`.
- Tokens stored unencrypted for now (no IAM until Sprint 3) — documented gap in `SessionStore`.

## Scope
- In: dependency catalog, `core/{designsystem,navigation,network,database,datastore,domain,presentation,di}`, app shell, string resources (en/es), unit tests of the core, README guide.
- Out: any feature screen, feature DAOs/entities, authentication flow, Mapbox, WorkManager, Home content.

## Tasks
- [x] T1 — Dependencies, Room 3 plugin, buildConfig API URL, network security config.
- [x] T2 — Domain result/error types, ApiCaller, ApiErrorMapper, interceptors, DataStore SessionStore.
- [x] T3 — AppDatabase (Room 3) with `cache_metadata`, DI modules.
- [x] T4 — Design system: colors from Figma primitives, Newsreader/Roboto typography, spacing, shapes.
- [x] T5 — Navigation shell: 4 tab graphs, `VioraTabBar` (floating pill + action), placeholders.
- [x] T6 — Unit tests, README feature checklist.

## Acceptance criteria
- `./gradlew assembleDebug testDebugUnitTest` succeeds.
- App launches and the tab bar switches between the 4 placeholder tabs (verified on emulator Pixel_10_Pro_XL).
- No `android.*`/Room/Retrofit imports under any `domain` or `application` package.

## Progress / evidence
- Branch `feature/project-setup`. Build 1m30s; 21 unit tests pass (AppResult 5, ApiCaller 12, interceptors 4).
- Emulator check: Home renders Newsreader headline, tab bar matches Figma (bubble on active tab, yellow "+"); tapping "Plots" moves the bubble and swaps the screen; no crash in logcat.
- Not yet verified: Spanish locale on device, tab bar at 360 dp width, TalkBack, release build.

## Next step
Review + merge `feature/project-setup` into `develop`, then each developer branches `feature/<context>-<story>` from `develop`.
