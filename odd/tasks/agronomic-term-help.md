# Feature: "¿Qué significa?" help for agronomic terms (M-03)

Objective: let the producer open a plain-language explanation of "aclareo", "índice de alternancia" and the phenological stages without leaving the screen.
Branch: `feature/agronomic-term-help` from `develop` · Delivery: single PR · Tests: `./gradlew testDebugUnitTest` (JUnit4, coroutines-test).

## Specs
- S1 (M-03, verbatim): "Añadir ayudas contextuales «¿Qué significa?» con definiciones breves y lenguaje de campo. Evaluar el uso conjunto de «raleo (aclareo)» y explicar el índice como la variación entre campañas de alta y baja producción."
- S2 (verbatim): "Incorporar un icono o enlace «¿Qué significa?» junto a «aclareo», «índice de alternancia» y las etapas fenológicas." / "Usar explicaciones breves, ejemplos relacionados con la campaña y vocabulario comprensible." / "Mantener las explicaciones disponibles desde las pantallas donde aparece cada término."
- S3 acceptance (verbatim): "Cuando el productor consulte una métrica o término técnico, podrá abrir su explicación sin abandonar la pantalla actual." / "Las definiciones describirán el concepto en lenguaje sencillo y no dependerán de conocimientos agronómicos especializados."
- S4 (scope): the help sits on Inicio (phase carousel / phase card, alternation card, today-in-field section) and in the plot detail (thinning plan section, alternation) — the screens named by M-03: "Inicio y detalle del lote". One reusable glossary (term → title, short definition, campaign example) and one reusable trigger + bottom sheet; no copy duplicated per screen.
- S5 (assumption): "raleo" is shown only as a synonym inside the aclareo definition ("También se le dice raleo."); existing labels keep saying "aclareo" because the mapping says "Evaluar" (not decided yet).
- S6: all copy in `values/strings.xml` (en) and `values-es/strings.xml` (es, neutral Spanish, "tú" like existing strings). Trigger has a content description and a touch target of at least 48 dp.

## Tasks
- T1 — S1-S6 · route: delegated (codex agent `terms`) · status: done (supervisor review x2, testDebugUnitTest + assembleDebug green)

## Log
- L1 (user, 2026-11-19): "en docs te estoy poniendo un md con mejoras para la app. de esas mejoras la 1 y la 4 las va a implementar una amiga, encargate del resto de mejoras de nivel alto y medio."
- L2 (2026-11-19): implemented by codex agent, two supervisor review rounds (design system reuse, copy, imports); testDebugUnitTest and assembleDebug pass; no device check yet.
