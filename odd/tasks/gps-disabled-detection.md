# Feature: Detect a disabled location service before marking a plot (M-02)

Objective: tell the producer when the device location service (GPS) is off, offer to turn it on, and never block them from tracing the plot on the map.
Branch: `feature/gps-disabled-detection` from `develop` · Delivery: single PR · Tests: `./gradlew testDebugUnitTest` (JUnit4, coroutines-test).

## Specs
- S1 (M-02, verbatim): "Verificar el estado del servicio de ubicación antes de iniciar el trazado. Si está apagado, informar al usuario y ofrecer abrir la configuración para activarlo. Mantener la opción de dibujar el contorno manualmente sobre el mapa."
- S2 (verbatim): "Comprobar permisos y estado del servicio de ubicación antes de iniciar el trazado." / "Mostrar un mensaje claro cuando el GPS esté desactivado." / "Ofrecer una acción para activar la ubicación desde los ajustes del dispositivo." / "Permitir continuar mediante el trazado manual sobre el mapa."
- S3 acceptance (verbatim): "Dado que el GPS está desactivado, cuando el usuario inicia la delimitación, entonces la aplicación informa el problema y muestra cómo resolverlo." / "Si el usuario no activa el GPS, podrá delimitar el lote manualmente sin quedar bloqueado."
- S4: when the producer comes back from the settings with the location on, the GPS method unlocks without restarting the wizard (same pattern as the permission re-check on resume in `odd/tasks/plot-gps-method.md`). If the service is turned off in the middle of a GPS walk, the producer is told the cause (not a generic "lost signal"), with the same two ways out: open settings or continue on the map keeping the corners.
- S5: all new UI copy in `values/strings.xml` (en) and `values-es/strings.xml` (es, neutral Spanish, field vocabulary, "tú" like the existing strings).

## Tasks
- T1 — S1-S5 · route: delegated (codex agent `gps`) · status: done (supervisor review x2, testDebugUnitTest + assembleDebug green)

## Log
- L1 (user, 2026-11-19): "en docs te estoy poniendo un md con mejoras para la app. de esas mejoras la 1 y la 4 las va a implementar una amiga, encargate del resto de mejoras de nivel alto y medio."
- L2 (2026-11-19): implemented by codex agent, two supervisor review rounds (design system reuse, copy, imports); testDebugUnitTest and assembleDebug pass; no device check yet.
