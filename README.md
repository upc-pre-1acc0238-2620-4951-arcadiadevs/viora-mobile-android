# Viora — mobile app

Viora is the native Android client of an olive orchard crop-load and
alternate-bearing (vecería) management platform for olive producers and
cooperative technical managers. Producers register plots, record field
samples, follow agroclimatic alerts, phenology and thinning prescriptions,
and close each harvest; cooperative managers oversee members, territorial
risk and intake forecasts.

## Tech stack

- Kotlin 2.4.20, Jetpack Compose (Material 3), Compose BOM 2026.09.00
- AGP 9.4.1, compileSdk / targetSdk 37, minSdk 26, Java 11
- Hilt (dependency injection) + KSP
- Retrofit + OkHttp (Gson converter, logging interceptor in debug)
- Navigation Compose (type-safe routes)
- Room (offline cache, added per bounded context as needed)
- Kotlinx Coroutines, Kotlinx Serialization

## Architecture

The project follows Clean Architecture + DDD, organized by bounded context
under `features/`, with cross-cutting concerns under `core/`.

```
app/src/main/java/pe/edu/upc/viora/
├── core/
│   ├── theme/        # Material 3 color scheme, typography
│   ├── navigation/   # AppNavHost, type-safe routes
│   ├── network/di/   # Retrofit / OkHttp Hilt module
│   └── database/     # Room database (added when the first entity lands)
└── features/
    └── <bounded-context>/
        ├── presentation/
        │   ├── ui/         # Composable screens
        │   ├── viewmodel/  # ViewModels
        │   └── state/      # UI state models
        ├── application/
        │   ├── usecase/    # Application use cases
        │   └── service/    # Application services
        ├── domain/
        │   ├── entity/       # Domain entities
        │   ├── valueobject/  # Value objects
        │   └── repository/   # Repository interfaces
        └── infrastructure/
            ├── local/       # Room DAOs / local data sources
            ├── remote/      # Retrofit services / DTOs
            ├── repository/  # Repository implementations
            ├── mapper/      # DTO / entity <-> domain mappers
            └── di/          # Hilt modules for this context
```

### Bounded contexts

Package names map to the bounded contexts of the Viora context map
(`ma-viora-report`, `docs/context-map/context-map.md`):

- `iam` — Identity & Access Management: sign-up, sign-in, JWT access/refresh
  tokens, roles, password recovery.
- `profiles` — User Profiles: personal data and contact channels of
  producers and technical managers.
- `subscription` — Subscription & Cooperative Membership: Producer Plan
  subscriptions, checkout, cooperative voucher codes and hectare quota.
- `plotmanagement` — Olive Orchard & Plot Management: georeferenced plots,
  olive variety and tree density.
- `telemetry` — Agroclimatic Telemetry & Sensor Monitoring: virtual sensor
  nodes, soil moisture series, weather forecast and stress/frost alerts.
- `phenology` — Phenology & Historical Bearing Analytics: phenological
  stages, winter chill accumulation, Biennial Bearing Index and yield history.
- `croploadregulation` — Crop Load Regulation & Thinning Advisory (primary
  core): field sampling, sustainable crop load, thinning prescriptions and
  execution window.
- `harvestsettlement` — Harvest Settlement & Performance Reporting: final
  harvested weights, interannual stabilization and technical dossier.
- `cooperativeoperations` — Cooperative Operations & Territorial
  Intelligence: member registry, territorial risk matrix and intake forecast.

### Architecture rules

- Dependencies point inward: `presentation` and `infrastructure` depend on
  `application` and `domain`; `domain` depends on nothing else.
- `domain` and `application` are pure Kotlin: no `android.*` imports and no
  persistence/network libraries (Room, Retrofit, OkHttp, Gson).
- Business validation lives in `domain` entities and value objects; prefer
  value objects over primitive types (e.g. `value class Email(val value: String)`).
- DTOs and Room entities never leave `infrastructure`; they are mapped to
  domain types before crossing into `application`/`presentation`.

## Git workflow

GitFlow branches (`main`, `develop`, `feature/*`, `release/*`, `hotfix/*`) and
Conventional Commits for every commit message.

## Running the app

```bash
export JAVA_HOME=/opt/android-studio/jbr
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

Open the project in Android Studio and run the `app` configuration on an
emulator or device (minSdk 26).
