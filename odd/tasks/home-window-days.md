# Feature: Thinning window days consistent with the daily summary (M-06 follow-up)

Objective: the Home phase card and the thinning plan count the window days the same way as the server's daily summary, and never present the usual calendar as the plot's own window.
Branch: `feature/home-window-days` from `origin/develop` · Delivery: single PR · Tests: `./gradlew testDebugUnitTest`.

## Specs
- S1 (M-06, verbatim): "Corregir el texto y generar el mensaje desde el mismo estado fenológico que utiliza la tarjeta principal. Añadir una prueba para evitar mensajes duplicados o contradictorios."
- S2: days LEFT in an open thinning window count today, like the server (ma-viora-platform `ThinningWindow.daysLeft` = DAYS.between(today, closesOn) + 1, decision 2026-10-30 "Días de ventana. Cuentan el día de hoy: el último día de la ventana es 'queda 1 día'"). Applies to the Home THINNING card while NOW and to `ThinningPrescription` WindowPhase.Open.daysLeft (thinning plan screen). Days UNTIL a window opens keep excluding today (server `daysUntilOpen`). Other phases are out of scope.
- S3: when the focused plot has no full bloom (the thinning window comes from `OliveCalendar` usual dates, not from the plot), the Home THINNING card must say the count is the usual window, reusing the existing pattern of the bloom card: message like `phase_msg_bloom_missing` ("para la ventana habitual. Registra la plena flor para fijar la tuya.") and the RECORD_BLOOM action, instead of "para aclarar X. Completa el muestreo para saber cuánto."
- S4: tests pin S2 (last day = 1 day left; on closing day+1 closed) and S3, and the existing behaviour of the other cards stays unchanged.

## Tasks
- T1 — S1-S4 · route: delegated (codex agent `days`) · status: done (supervisor review, testDebugUnitTest + assembleDebug green)

## Log
- L1 (user, 2026-11-19): "va, mientras esperas la confirmacion corrige landiferencia en el backend" ... then "arregla ambos" (backend names the plot of the summary window; Android aligns the day count and the card without full bloom).
- L2 (evidence, demo account): phase card "11 days" (Cuartel San Jerónimo, FULL_BLOOM_MISSING → usual window closing Nov 30) vs daily summary "22 days of window left" (another plot, prescription closing Dec 10, today counted).
- L3 (2026-11-19): implemented by codex agent `days`; supervisor removed the now-unused "last day" strings; tests and assembleDebug pass. Backend counterpart: ma-viora-platform feature/daily-summary-window-plot 1e7c36f.
