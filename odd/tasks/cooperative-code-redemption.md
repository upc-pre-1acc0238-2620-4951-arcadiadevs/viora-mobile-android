# Feature: Redeem a cooperative code (US07 · P05, P06)
Objective: the producer redeems the invitation code of their cooperative (P05) and sees the active membership (P06). Branch `feature/cooperative-code-redemption` from `develop`. Delivery: ask-on-risk. Tests: `./gradlew testDebugUnitTest`.

## Specs
- S1 Producer only (backend feature doc S1). Backend contract: `POST /api/v1/cooperative-code-redemptions`, body `{ code }`; 201 subscription with `cooperativeName`, `quotaHa`; 400 not redeemable ("Código vencido o ya canjeado. Pide uno nuevo a tu cooperativa."); 409 already has an active subscription; 403 not a producer; 401 no session; 429 too many failed attempts.
- S2 Figma wins over the report. P05 "Canjea tu *código de socio.*", field "Código de cooperativa" prefilled "COOP-", hint "Está en el mensaje de tu cooperativa. Ej.: COOP-7K2Q", buttons "Canjear código" and "Pagar mi Plan Productor". P05 invalid: "Este código *no funcionó.*", voice "Puede haber vencido o ya fue usado por otro socio.", red field, "Código vencido o ya canjeado. Pide uno nuevo a tu cooperativa.", button "Probar otro código". P06: "● MEMBRESÍA ACTIVA", "Tu cooperativa *cubre tu plan.*", "Te uniste con tu código de socio. Empecemos por registrar tu primer lote.", rows Cooperativa / Cupo "12 ha cubiertas" / Vigencia "Según tu convenio", buttons "Registrar mi primer lote" and "Ir al inicio".
- S3 Disconnected flow (L2): "de momento no asumamos nada respecto a desde donde llegara. supongo que sea algun apartado en ajustes o algo asi. se puede hacer el flo desconectado de forma temporal? configurariamos la app para que inicie en la pantalla que queramos para las pruebas". No screen links to P05; a debug-only setting chooses the start screen.
- S4 Out of scope: payment of the individual plan (teammate), so "Pagar mi Plan Productor" is shown as in the design and does nothing yet; sign-up/onboarding (P02).

## Tasks
- [x] T1 — S1: data layer in `features/subscription` (service, DTOs, domain, repository mapping 400/409/429, use case, DI) + repository tests. Route: inline. Commit 1d11bdd.
- [x] T2 — S2, S4: P05 view model and screen + tests. Route: inline. Commit 552c8fc.
- [x] T3 — S2: P06 screen; nav graph `cooperativeCodeNavGraph`. Route: inline. Commit 552c8fc.
- [x] T4 — S3: debug start screen (`viora.debugStartScreen` in local.properties) and docs. Route: inline. Commit 552c8fc.
- [ ] T5 — End-to-end check against the backend branch with the user.

## Log
- L1 (2026-10-20) "va. ya hemos coregido el back. que sigue?"
- L2 (2026-10-20) S3 quote above.
- L3 Decisions: the redemption result reaches P06 through the route (cooperative name and hectares), nothing is cached. 429 is mapped inside the repository (it reaches it as `AppError.Unknown` with an `HttpException`), so the shared `ApiErrorMapper` is not touched. The backend accepts the call without a token through its demo producer fallback.
- L4 (2026-10-20) T1-T4 built inline. RED observed (missing repository and view model), GREEN: subscription tests 14/0, full suite 569/0, `assembleDebug` OK. P05 has no separate "validating" look: while redeeming the field is disabled and the button ignores taps (PrimaryPillButton shows its spinner only with a trailing icon, which the design does not have). Next: T5 with the user (`viora.debugStartScreen=cooperative-code`, backend `feature/cooperative-code-redemption` running locally).
- L5 (2026-10-21) "vamos a continuar haz pull para ver los cambios recientes en la app y ver si hay algo nuevo que debamos considerar" — merged develop 1.5.0 (IAM, session refresh, splash start). Conflicts: `AppNavHost` now starts on `debugStartDestination() ?: SplashRoute`, and the flow leaves through develop's `openHome()` (our `leaveForHome()` was dropped); both strings files keep both sides. The redemption call now carries the producer token when signed in (default Retrofit has `AuthInterceptor`); the debug start skips the splash, so T5 still runs on the backend demo producer fallback. Suite 588/0, `assembleDebug` OK.
