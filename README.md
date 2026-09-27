# Viora — mobile app

Viora is the mobile client of an olive orchard crop-load and alternate-bearing
management platform for producers and cooperatives. It lets producers track
plots, telemetry, phenological stages and crop-load regulation, and lets
cooperatives coordinate operations and settlements across their members.

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
│   ├── navigation/    # AppNavHost, type-safe routes
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

- `iam` — identity and access management (authentication, sessions).
- `profiles` — producer and cooperative member profiles.
- `subscription` — plans and billing for producers/cooperatives.
- `plotmanagement` — orchard plots and their metadata.
- `telemetry` — sensor and field telemetry ingestion.
- `phenology` — phenological stage tracking for olive trees.
- `croploadregulation` — crop-load estimation and regulation actions.
- `harvestsettlement` — harvest volumes and settlement calculations.
- `cooperativeoperations` — cross-member cooperative coordination.

### Architecture rules

- Dependencies point inward: `presentation` → `application` → `domain`;
  `infrastructure` depends on `domain`/`application`, never the other way
  around.
- `domain` and `application` are pure Kotlin: no `android.*` imports.
- Prefer value objects over primitive types at domain boundaries.
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
