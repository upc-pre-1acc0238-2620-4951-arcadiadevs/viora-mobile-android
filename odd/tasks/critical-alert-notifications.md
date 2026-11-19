# Feature: Critical alert notifications and offline audit (M-07)

Objective: notify the producer of critical agroclimatic alerts (frost first) without opening the app, and document what can be consulted or recorded offline today.
Branch: `feature/critical-alert-notifications` from `develop` · Delivery: single PR · Tests: `./gradlew testDebugUnitTest` (JUnit4, coroutines-test).

## Specs
- S1 (M-07, verbatim): "Permitir configurar el canal de aviso para alertas críticas, considerando notificaciones de la aplicación y WhatsApp si la integración está disponible. Informar qué funciones y datos pueden consultarse o registrarse sin conexión."
- S2 (verbatim): "Ofrecer preferencias para recibir alertas críticas mediante notificaciones de la aplicación y, si se implementa la integración, WhatsApp." / "Permitir consultar información previamente sincronizada sin conexión." / "Permitir guardar registros de campo localmente y sincronizarlos cuando vuelva la conexión." / "Mostrar el estado de sincronización y distinguir datos guardados localmente de datos sincronizados."
- S3 acceptance (verbatim): "El productor podrá consultar los datos disponibles en caché aunque no tenga conexión." / "Los registros realizados sin conexión quedarán pendientes de sincronización y se enviarán al recuperar conectividad." / "La interfaz indicará claramente si un registro está pendiente o ya fue sincronizado."
- S4 (supervisor decision, client-only like M-05): app notifications come from a periodic WorkManager check of the producer's open incidents (telemetry `IncidentRepository`), following the existing daily-summary worker/notifier pattern. No backend change, no FCM. WhatsApp is out of scope (no official messaging channel yet; section 6 of the mapping).
- S5 (assumption): "critical" = FROST_WARNING of any severity plus any incident with the highest `IncidentSeverity`. Each incident is notified at most once (persist the ids already notified). Own channel "Alertas críticas" with high importance. Tapping the notification opens the alert detail.
- S6: a preference to turn critical alerts on/off inside the app (on by default), placed where the producer manages their account/preferences (profiles feature), and respecting the Android 13+ POST_NOTIFICATIONS permission the same way the daily summary does.
- S7: offline audit: write `docs/m07-sin-conexion.md` (Spanish, for the team) listing, per screen/record type, what can be consulted offline, what can be recorded offline, how it syncs, and whether the UI shows pending vs synced. List the gaps against S3 as a table. Do not implement the gaps in this task.

## Tasks
- T1 — S4-S6 · route: delegated (codex agent `alerts`) · status: implemented, pending supervisor and device verification
- T2 — S7 · route: delegated (codex agent `alerts`) · status: implemented, pending supervisor review

## Log
- L1 (user, 2026-11-19): "encargate de las bajas entonces" (M-06 and M-07 from docs/mejoras_app_android_viora.md).

- Worker: implemented T1 and T2 without commits. `./gradlew testDebugUnitTest assembleDebug -Dorg.gradle.jvmargs=-Xmx3g` passed. Notification permission, scheduling and deep-link behavior still require device verification. IDs are stored before posting to avoid duplicate posts; a process failure between those operations may miss an alert.
- Supervisor follow-up: session scheduling now lives in AppSessionViewModel and cancels on sign-out; notification permission/settings hint added; notified IDs pruned after successful refresh; filter simplified, channel description and accessible localized hint added, and audit terminology corrected. Focused tests, `./gradlew testDebugUnitTest -Dorg.gradle.jvmargs=-Xmx3g` and `./gradlew assembleDebug -Dorg.gradle.jvmargs=-Xmx3g` passed. Device verification remains pending.
- L2 (2026-11-19): implemented by codex agent `alerts`; supervisor moved the permission check to VioraNotificationChannels.canPostCriticalAlerts (one source for screen and notifier) and scheduling/cancel to AppSessionViewModel; tests and assembleDebug pass; no device check yet. Offline gaps listed in docs/m07-sin-conexion.md, not implemented.
