# Feature: Edit, adjust the outline and archive a plot (US10, US11 · P27, P28, P29)

## Objective
Let the producer correct the data of a registered plot (name, variety, planting frame), move the corners of its outline, and archive a plot that is no longer farmed, from the plot detail.

## Problem / Why
A plot can only be registered today. A typo in the name, a wrong variety, a wrong planting frame or a badly marked corner had no way out, and a plot that is no longer farmed stayed in the active list forever.

## Sources
- Figma `Viora202602_Mobile_App` → Mobile Prototipo → "19 · Editar o archivar un lote": P27 "Opciones del lote (hoja)" (opened by the ⋮ of P26), P28 "Editar lote" (+ "Marco fuera de rango"), P28 "Archivar lote con historial (diálogo)", P29 "Ajustar el contorno", P20 "Archivados".
- Backend `PUT /plots/{id}` (`If-Match`, 0.23.0 adds the optional `variety`), `DELETE /plots/{id}` (soft delete, `REMOVED_SOFT_DELETE`), `GET /plots?status=REMOVED_SOFT_DELETE` and `POST /plots/{id}/restore` (added for this feature in `feature/plot-restore`, which also fixes the stale revision the save used to return).

## Decisions
- Three separate screens, as in the design: the options sheet (P27, a white card with the rows Editar datos del lote, Ajustar el contorno, Sensores del lote and, in terracotta, Archivar lote), the edit form (P28, its own route) and the outline adjustment (P29, its own route). The last two hide the tab bar.
- Edit form: cards for name, variety (tap to choose among the chips), planting frame (two typed distances in beige tiles and a pill "72 → 100 árboles/ha · ≈ 250 en el lote") and the area, read only ("desde el contorno"). A frame above 500 trees/ha turns the card red ("no es viable") and blocks saving. The button is enabled only when something changed and everything is valid.
- The variety can be edited. No restriction when the plot has samplings or harvests: the caliber model calibrates itself with the producer's data (agreed with Victor).
- The request carries the revision of the cached plot in `If-Match`. A 412 means the plot changed on another device: the repository refreshes the cache and the screen tells the producer to reopen it. A 409 is a taken name and is shown under the name field.
- Adjust the outline: corners are always draggable (the map can still be panned), "Agregar esquina" adds one where the crosshair is, undo reverts the last change (a whole drag is one step). A move or a corner that would make the edges cross is refused. Back and close leave without saving. Only the outline is sent; name, variety and frame go back as the plot has them.
- Archive dialog: the white card of the design with the hectares in active plots before and after ("4,0 → 1,5 ha en tus lotes activos"), computed from the cached plots. Not built, because the app has no data for them: the plan limit ("de 5,0") and the count of campaigns of the plot. The design's "Tiene 5 campañas, muestreos y expediente" and "puedes restaurarlo" are left out for the same reason.
- Archived list (P20 "Archivados"): two filters, "Activos · N" and "Archivados · N" (dark when selected, white when not; with nothing archived the second does nothing). An archived plot is a card with the badge "Archivado · historial conservado" and, under it, the dark pill "Restaurar lote". The card is not tappable: an archived plot has no detail (the server answers 404 for it). The badge has no date ("Archivado hoy" in the design) because the backend does not return when a plot was archived. Archiving marks the cached plot as archived, so it appears in the list at once; the list is read from the server on every refresh. Restoring needs the connection; with no archived plot left the list goes back to the active ones.
- The "Sensores del lote" row is in the sheet as in the design but only closes it: the sensors feature does not exist yet.

## Scope
- In: update/archive/restore in the data layer, options sheet, edit form, outline adjustment, archive dialog, archived list with restore, strings (en/es), tests.
- Out: sensors, the plan-usage card of the list, the other rows of the ⋮ menu of the older "Más opciones" layers (Vecería, Plan de aclareo, Clima, Expediente), Wireframes and Prototipo are not touched.

## Tasks
- [x] T1 — Data layer: `PlotService` PUT/DELETE, `PlotChanges`, `PlotRepository.update/archive`, use cases.
- [x] T2 — Options sheet (P27) and archive dialog (P28) on the plot detail.
- [x] T3 — Edit form (P28): `EditPlotViewModel`, `EditPlotUiState`, `EditPlotScreen`, route.
- [x] T4 — Outline adjustment (P29): `AdjustOutlineViewModel`, `AdjustOutlineUiState`, `AdjustOutlineScreen`, route; `PlotTraceMap` can pan while corners are dragged.
- [x] T5 — Unit tests (repository update/archive, edit, adjust and archive view models).
- [x] T6 — Edit, adjust outline and archive checked on the phone by Victor.
- [x] T7 — Archived list and restore: `PlotService`, DAO, repository, use cases, `PlotsViewModel`, filters and "Restaurar lote".
- [ ] T8 — On-device check of the archived list and restore by Victor.

## Acceptance criteria
- `./gradlew assembleDebug testDebugUnitTest` succeeds.
- The ⋮ of the detail opens the P27 sheet; each row leads to its screen as in the prototype.
- Editing name, variety or frame updates the detail without reloading; a taken name shows its message; an outdated plot asks to reopen; a non-viable frame blocks saving.
- Dragging a corner updates the area live next to the previous one; saving updates the detail.
- Archiving removes the plot from the active list, closes the detail and shows it under "Archivados".
- "Restaurar lote" brings it back to the active list with its data.
