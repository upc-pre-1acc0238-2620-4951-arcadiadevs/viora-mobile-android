# Feature: Plot detail (P26)

## Objective
Show one plot's detail: its outline on a satellite map, name and status, and its figures (area, density, trees). It opens right after registering a plot (with a "plot saved" notice) and from any card of the plots list.

## Problem / Why
After saving, the registration wizard just closed and the producer saw the list again, with no confirmation and no way to open the plot. The list cards looked tappable (Figma has a chevron) but did nothing.

## Sources
- Figma `Viora202602_Mobile_App` → Mobile Prototipo → "07 · Registrar un lote" → P26 (and the "⋮" options sheet of P26).
- Backend `viora-platform` PlotResource: `{id, name, variety, areaHa, treeDensity, rowSpacingM, treeSpacingM, polygonGeoJson, status, revision}` (no creation date, no registration method).

## Decisions
- The cache (Room) is the source of truth: the detail observes the plot by id (`observePlot`), including inactive plots. No `GET /plots/{id}` yet: the list refresh already keeps the cache current, and the plot is cached on registration. It becomes useful with the edit feature (fresh `revision`).
- The hero reuses `OutlinePreview` (read-only satellite map framed on the outline), made flexible: optional caption, shape and framing padding.
- The subtitle shows the planting frame ("Sevillana · marco 7 × 5 m") instead of Figma's "registrado hoy con GPS": the backend does not return the date or the method.
- The tab bar stays visible on this screen, as in Figma (`VioraApp` lists `PlotDetailRoute` next to the tab roots).
- The wizard now reports the saved plot's id (`savedPlotId`) and the nav graph replaces the wizard with the detail, so "back" goes to the list.
- The view model reads `plotId` / `justSaved` from the `SavedStateHandle` by name instead of `toRoute`, which needs a real `Bundle` and cannot run in JVM unit tests. The notice flag is kept in the saved state so it does not reappear after process death.
- **Left out on purpose** (their destinations belong to other features and do not exist yet): the "Siguientes pasos" cards (past harvests → Jahat's US20/US21, link a node → Diana's US13/US14) and the "⋮" options sheet (edit data and adjust outline → US10 edit plot, sensors → Diana).

### Explore mode (added after the first phone test)
- The sheet has two positions (a plain sheet whose height changes with `animateContentSize`, the same soft spring as the registration wizard's sheet, not Material's `BottomSheetScaffold`): open (its top at 340 dp, the map framed above it) and lowered (grabber, name, status and frame, above the tab bar). The sheet moves only from its grabber (drag it down or up, or tap it), as in the registration wizard, so a stray swipe on the content does nothing; double-tapping the map also lowers it. Lowering it unlocks the map: pan and pinch-zoom (never rotate or tilt, zoom between 11 and 20). Raising it, with a drag, the grabber or the system back button, frames the whole outline again.
- Why no extra buttons: the map corner already hosts the back button and will host the "⋮" menu; the gesture and the grabber add nothing visible. The grabber is also the accessible alternative to dragging (it is a button).
- The map (`PlotDetailMap`) fills the screen and the camera is padded by the part the sheet covers, like `PlotTraceMap`; the Mapbox logo and attribution are moved above the sheet. `OutlinePreview` is back to its original form (the detail no longer uses it).
- While locked, double-tap zoom is not available (it is the unlock gesture); once exploring the map's own gestures are back, including double-tap zoom.
- Figma: P26 gets a grabber and the variant "P26 · Detalle del lote / Explorando el mapa" in Mockups, Wireframes and Prototipo (where dragging the sheet navigates between the two frames); the previous P26 is archived.
- Pending: onboarding should teach the double tap (not built yet); an optional one-time hint.

## Scope
- In: `observePlot` (DAO, repository, use case), `PlotDetailViewModel`/`PlotDetailUiState`/`PlotDetailScreen`, `PlotDetailRoute`, tappable plot cards with chevron, wizard hand-off, en/es strings, tests.
- Out: next-step cards, options sheet, edit/archive, GPS-walk method.

## Tasks
- [x] T1 — `observeById` in `PlotDao`, `observePlot` in the repository and `ObservePlotUseCase`.
- [x] T2 — Wizard exposes `savedPlotId`; `RegisterPlotScreen` has `onSaved(PlotId)` and `onLeave`.
- [x] T3 — `PlotDetailViewModel` (cache + auto-hiding saved notice) and `PlotDetailUiState`.
- [x] T4 — `PlotDetailScreen`: hero map, back button, saved notice, sheet with title, status chip, subtitle and three figures; not-found state.
- [x] T5 — `PlotDetailRoute` in `PlotsNavGraph`; cards open it; tab bar shown on it.
- [x] T6 — Strings (en/es), `ic_chevron_right`, unit tests.
- [ ] T7 — On-device check of the hero map and the saved notice (the emulator's map does not render satellite tiles).

## Acceptance criteria
- `./gradlew assembleDebug testDebugUnitTest` succeeds (102 tests).
- Registering a plot lands on its detail with the "Lote guardado" notice that fades after 3 s; back returns to the list.
- Tapping a card of the list opens that plot's detail; the figures match the card.
- The detail works offline (reads the cache) and shows a "not available" message for an unknown id.
