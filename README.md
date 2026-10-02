# Viora — mobile app (Android, Productor)

Viora is the native Android client of an olive orchard crop-load and
alternate-bearing (vecería) management platform. **This app is the Productor
experience**: producers register plots, follow agroclimatic alerts, phenology and
thinning prescriptions, and close each harvest. The Gestor (cooperative manager)
experience lives in the Flutter app (`viora-mobile-flutter`).

Design source: Figma file `Viora202602_Mobile_App`, page "Mobile Mockups", canvas
"Mobile Prototipo", section **App Productor · Kotlin**.

## Tech stack

- Kotlin 2.4.20, Jetpack Compose (Material 3), Compose BOM 2026.09.00
- AGP 9.4.1, Gradle 9.6, compileSdk / targetSdk 37, minSdk 26, Java 11
- Hilt (DI) + KSP, `hilt-navigation-compose`
- Retrofit 3 + OkHttp 4 with the kotlinx-serialization converter (logging in debug only)
- Navigation Compose (type-safe `@Serializable` routes)
- **Room 3** (`androidx.room3` — new group and package, not `androidx.room`) and DataStore
- Coroutines, kotlinx-serialization (JSON DTOs and navigation routes)
- Fonts: Newsreader (display/headline) and Roboto (interface), bundled in `res/font`
  (SIL OFL, licenses in `docs/licenses`)

## Run it

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Open the project in Android Studio and run the `app` configuration (minSdk 26).

### Backend

The API base URL comes from `BuildConfig.API_BASE_URL`:

| Build | Default |
|---|---|
| debug | `http://10.0.2.2:8080/api/v1/` (emulator → backend on your machine) |
| release | `https://viora-platform.onrender.com/api/v1/` (first request can take ~1 min: free-tier cold start) |

Override it per developer in `local.properties` (never committed):

```properties
viora.apiBaseUrl=http://192.168.1.50:8080/api/v1/
```

Run the backend locally from `viora-platform` (`./mvnw spring-boot:run`); it starts on H2
in memory and needs no login: every request is treated as one fixed producer until IAM ships.
On a physical device use `adb reverse tcp:8080 tcp:8080` and `viora.apiBaseUrl=http://localhost:8080/api/v1/`.
Cleartext HTTP is allowed only in the debug build and only for `10.0.2.2` / `localhost`.

## Architecture

Clean Architecture + DDD organised by bounded context under `features/`, with
cross-cutting code under `core/`.

```
app/src/main/java/pe/edu/upc/viora/
├── MainActivity.kt, VioraApplication.kt, VioraApp.kt   # app shell (tab bar + nav host)
├── navigation/                         # AppNavHost: composition root, the only code that knows every feature
├── core/
│   ├── designsystem/{theme,component}  # colors, Newsreader/Roboto, spacing, shapes, VioraTabBar
│   ├── navigation/                     # shared route objects (…Graph/…Route), TopLevelDestination, PlaceholderScreen
│   ├── network/                        # ApiCaller, ApiErrorMapper, interceptors, di/NetworkModule
│   ├── database/                       # AppDatabase (Room 3), shared DAOs
│   ├── datastore/                      # SessionStore (tokens)
│   ├── domain/                         # AppResult, AppError (pure Kotlin)
│   ├── presentation/                   # AppError → message resource
│   └── di/                             # Hilt modules: network, database, DataStore, dispatchers
└── features/
    └── <bounded-context>/
        ├── presentation/{ui,viewmodel,state,navigation}
        ├── application/{usecase,service}
        ├── domain/{entity,valueobject,repository}
        └── infrastructure/{local,remote,repository,mapper,di}
```

Bounded contexts (package → report name): `iam` IAM · `profiles` User Profiles ·
`subscription` Subscription & Cooperative Membership · `plotmanagement` Orchard ·
`telemetry` Agroclimatic Telemetry · `phenology` Phenology & Historical Bearing ·
`croploadregulation` Thinning (core) · `harvestsettlement` Harvest ·
`cooperativeoperations` Territory (Gestor — **not built in this app**).

### Rules

- Dependencies point inward. `domain` and `application` are pure Kotlin: no `android.*`,
  Room, Retrofit, OkHttp or Gson imports.
- Business validation lives in domain entities and value objects (prefer
  `@JvmInline value class PlotId(val value: String)` over raw `String`).
- DTOs and Room entities never leave `infrastructure`; mappers convert them to domain types.
- Repositories return `AppResult<T>` (never raw `Response<T>`, never swallowed exceptions).
  Run network calls through `ApiCaller`.
- ViewModels expose a read-only `StateFlow<UiState>`; screens collect it with
  `collectAsStateWithLifecycle()`. Model screen state with sealed types when states are exclusive.
- `core/` never imports `features/`; only `navigation/AppNavHost` (composition root) does.
- DTOs are `@Serializable` data classes. Declare a field nullable only if the backend can really omit
  it: a missing required field fails the call (it becomes `AppError.Unknown`) instead of silently
  turning into `null`. Dates are `String` (ISO-8601) and become `java.time` types in the mapper.
- Offline-first pattern: Room is the source of truth. The repository exposes a `Flow` read from the
  DAO plus a `refresh()` that calls the API and writes to Room; stamp `cache_metadata` on every
  successful refresh and show that time in the UI. Never present cached data as current.
- Hilt: bind repositories with `@Binds` in `SingletonComponent`. In Compose use
  `hiltViewModel()` from `androidx.hilt.lifecycle.viewmodel.compose` (the old
  `androidx.hilt.navigation.compose` import is deprecated).
- All code, comments and identifiers in English. UI text goes in `strings.xml` (`values/`
  English, `values-es/` Spanish) — never hard-coded in composables.
- Every tab screen must reserve `VioraTabBarDefaults.ContentBottomPadding` at the bottom.

## Adding a feature (checklist)

1. Create the layers inside your bounded context package (see tree above).
2. **Remote:** a Retrofit interface + DTOs in `infrastructure/remote`; provide the service in
   `infrastructure/di` (`retrofit.create(...)`). Check the real field names against the
   backend/Swagger — they differ from the report in places.
3. **Local (if cached/offline):** a Room entity + DAO in `infrastructure/local`; register the
   entity in `core/database/AppDatabase` (one line), bump `version`, add a `Migration`, and
   commit the generated JSON in `app/schemas/`. Provide the DAO in your `infrastructure/di`.
   Do **not** use destructive migration.
4. **Repository:** interface in `domain/repository`, implementation in
   `infrastructure/repository` bound with `@Binds` in `SingletonComponent`. Use `ApiCaller` for
   network calls and map DTOs to domain types before returning.
5. **Use cases** in `application/usecase` (`suspend operator fun invoke`).
6. **Presentation:** `UiState`, `@HiltViewModel`, screens, and a
   `NavGraphBuilder.<feature>NavGraph(navController)` that wraps your routes in
   `navigation<XGraph>(startDestination = XRoute)`. Then replace the matching placeholder block
   in `navigation/AppNavHost.kt` with a call to your graph builder.
7. Unit-test domain rules, mappers, use cases and ViewModels (`kotlinx-coroutines-test`;
   `MockWebServer` for remote code — see `ApiCallerTest`).

## Known gaps (before the first Firebase distribution)

- Release builds still have R8 disabled (`optimization { enable = false }`); enable it, add the
  signing config and smoke-test a release APK.
- Session tokens are stored unencrypted until the sign-in flow exists (see `SessionStore`).
- No CI yet: run `./gradlew testDebugUnitTest lintDebug` before opening a pull request.

## Git workflow

Branches: GitFlow with `main` and `develop`. Every piece of work happens in a
`feature/<english-kebab-name>` branch created from `develop` and merged back into it.

Commits follow Conventional Commits and must be:

- a single line: `type(scope): description` (e.g. `feat(plots): add plot list screen`)
- in English, all lowercase, with no trailing period
- without a body and without a `Co-Authored-By` trailer

Types in use: `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`.
