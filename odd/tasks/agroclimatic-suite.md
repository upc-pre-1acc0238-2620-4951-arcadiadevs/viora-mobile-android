# Feature: Agroclimatic Suite — 7-Day Weather Forecast & Erez Chill Portions (US19, US22)

## Objective
Enable olive producers to inspect geolocalized 7-day meteorological forecasts (maximum/minimum temperatures, precipitation probability, wind speed, frost risk alerts) and track winter chill accumulation portions via the Erez dynamic model for their olive plots in Tacna, allowing early agricultural planning, irrigation management, and crop protection against thermal anomalies.

## Problem / Why
Olive cultivation in arid and semi-arid valleys (like La Yarada, Tacna) is highly sensitive to extreme thermal fluctuations:
- Frost events during late winter / early spring damage tender shoots and olive inflorescences.
- Heatwaves disrupt flowering and accelerate soil evaporation.
- Olive varieties (such as Sevillana and Criolla) require 25–30 chill portions (Erez dynamic model) during winter dormancy to break rest and achieve uniform budburst.
Previously, the app had no weather forecast domain, and the `CLIMATE` lot section remained greyed out as "Coming soon".

## Sources
- Backend `viora-platform` agroclimatic endpoints:
  - `GET /api/v1/plots/{plotId}/forecasts` → `WeatherForecastResponseDto { plotId, dailyForecasts: [ { forecastDate, maxTemperature, minTemperature, precipitationProbability, windSpeedKmh, isFrostRisk, syncedAt } ], generatedAt }`
  - Integrated with Open-Meteo API using plot polygon centroid coordinates.
- Figma design system: `Viora202602_Mobile_App`, "Capa · Más opciones", "Tu lote", "Today in your field" weather cards.

## Decisions
- **Bounded Context:** Located in `pe.edu.upc.viora.features.climate` matching `LotSection.CLIMATE`.
- **Database & Room 3 Migration:**
  - Table `weather_forecasts` with composite primary key `(plot_id, forecast_date)` and foreign key to `plots(id)` with `CASCADE` delete.
  - Bump Room database version from `3` to `4` with non-destructive `MIGRATION_3_4`.
- **Offline-First:**
  - Room acts as the single source of truth for the UI (`observeForecast()`).
  - Network fetch via `ApiCaller.call(...)` populates the database and updates `cache_metadata` with key `forecasts:{plotId}`.
  - Offline mode retains and displays cached forecast with last synchronized timestamp.
- **Entry Points:**
  - Home: `Today in your field` section → `7 days >` button and weather card open `ClimateRoute`.
  - Plot Detail: `Tu lote` section gains dedicated `LotSectionCard` for climate, and `LotSectionsSheet` activates `LotSection.CLIMATE` (removing "Coming soon").

## Scope
- In (US19): Domain entities (`DayForecast`, `WeatherForecast`), Retrofit service (`ClimateService`), Room entity and DAO (`WeatherForecastEntity`, `WeatherForecastDao`), mappers, repository, use cases, Hilt DI modules, `ClimateScreen` (Compose), 7-day carousel, metric tiles, frost risk badges, en/es strings, unit tests.
- In (US22): Erez chill accumulation tracking and variety threshold progress (to follow after US19).
- Out: Real-time physical IoT weather station hardware pairing (handled in telemetry nodes).

## Checks
- Runner: `./gradlew testDebugUnitTest`
- Build verification: `./gradlew assembleDebug`

## Tasks
- [x] T1 — Domain + Data layer (US19): Entities, DTOs, `ClimateService`, `WeatherForecastEntity`, `WeatherForecastDao`, `MIGRATION_3_4`, `WeatherForecastMapper`, `WeatherForecastRepository`, use cases, Hilt modules; tests.
- [x] T2 — Presentation Layer & Navigation (US19): `ClimateUiState`, `ClimateViewModel`, `ClimateScreen` (7-day forecast, metrics, frost warnings), navigation route, Home & PlotDetail wiring, en/es strings; tests.
- [ ] T3 — Erez Chill Portions (US22): Chill portions tracking, olive variety thresholds (25–30 portions for Sevillana/Criolla), progress gauges; tests.

## Progress
- Branch `feature/agroclimatic-forecast-erez` created from `develop`.
- T1 committed (`46150`): Weather forecast domain, Room 3 migration (v3->v4), repository and unit tests.
- T2 completed: `ClimateScreen` (Material 3 with 7-day carousel, metric tiles, thermal trend, frost alert), navigation routes from Home and Plot Detail, `ClimateViewModelTest` unit tests passing.
