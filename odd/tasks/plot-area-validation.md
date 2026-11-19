# Feature: Validate the traced plot area (M-05)

Objective: show the traced area prominently, compare it with an optional approximate area the producer declares, and ask for confirmation before saving when they differ considerably.
Branch: `feature/plot-area-validation` from `develop` · Delivery: single PR · Tests: `./gradlew testDebugUnitTest` (JUnit4, coroutines-test).

## Specs
- S1 (M-05, verbatim): "Mostrar el área calculada de forma destacada antes de guardar. Compararla con el área declarada y solicitar confirmación cuando la diferencia sea considerable. Añadir una referencia de escala al mapa."
- S2 (verbatim): "Mostrar el área calculada en hectáreas durante y después del trazado." / "Comparar el área del polígono con el área declarada por el productor." / "Mostrar una advertencia y solicitar confirmación ante diferencias importantes." / "Incorporar una escala visual en el mapa para facilitar el trazado."
- S3 acceptance (verbatim): "Cuando el área calculada difiera considerablemente del área declarada, el sistema advertirá la diferencia antes de guardar." / "El productor podrá corregir el polígono o confirmar que desea continuar."
- S4 (user decision 2026-11-19, see `docs/decisiones/m05-area-declarada.md`): add an OPTIONAL "approximate hectares" field in step 1 of the registration wizard (METHOD step). Client-only: it is NOT sent to the backend and the API contract does not change. Empty field → no comparison.
- S5 (assumption): "considerable" = the traced area differs from the declared one by more than 30 % of the declared area. One named constant in the domain/state, unit-tested at the boundary.
- S6: the warning offers two ways out: go back and correct the outline (keeps the corners) or confirm and save. Confirming once is enough for that outline; changing the outline again re-evaluates it.
- S7: the scale reference is shown on both marking maps (trace on map and GPS walk) and stays correct when zooming.
- S8: all copy in `values/strings.xml` (en) and `values-es/strings.xml` (es, neutral Spanish, "tú" like existing strings).

## Tasks
- T1 — S1-S8 · route: delegated (codex agent `area`) · status: done (supervisor review x2, testDebugUnitTest + assembleDebug green)

## Log
- L1 (user, 2026-11-19): "en docs te estoy poniendo un md con mejoras para la app. de esas mejoras la 1 y la 4 las va a implementar una amiga, encargate del resto de mejoras de nivel alto y medio."
- L2 (user, 2026-11-19, on how to get the declared area): "vamos con la 1  dejalo documentado para informar al team" → option 1: optional field in step 1, client-only.
- L3 (2026-11-19): implemented by codex agent, two supervisor review rounds (design system reuse, copy, imports); testDebugUnitTest and assembleDebug pass; no device check yet.
