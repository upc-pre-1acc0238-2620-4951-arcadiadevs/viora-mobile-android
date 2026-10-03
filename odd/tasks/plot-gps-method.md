# Feature: Plot marking method and GPS walk (P21, P22)

## Objective
Let the producer choose how to mark the outline of a plot (walk it with the phone's GPS, or trace it on the satellite map) and mark the corners with the GPS while walking.

## Problem / Why
The wizard only had the map. Walking the plot and marking each corner with the GPS is the faster and more precise way for a producer standing in the field, and the Figma flow (Paso 1 de 3: method, Paso 2: outline, Paso 3: details and review) already assumes it.

## Sources
- Figma `Viora202602_Mobile_App` → Mobile Prototipo → "07 · Registrar un lote": P21 Método, P22 Delimitar con GPS, "Sin señal GPS", "Contorno abierto", "Bordes que se cruzan"; new states designed in this feature: P21 "Sin permiso de ubicación" and P22 "Señal débil" (Mockups only, pending approval for Wireframes and Prototipo).
- Report US09 scenarios 1-3 (area, fewer than 3 corners, GPS lost).

## Decisions
- The method chooser is always shown. The GPS card needs the **precise** location permission; without it the card is locked (padlock) and **Trazar en el mapa** becomes the recommended and selected one. Tapping the locked card asks for the permission again; when Android will not ask any more (denied twice, or "don't ask again": detected with `shouldShowRequestPermissionRationale`) a dialog sends the producer to the app settings, and the permission is checked again when the app comes back to the foreground. The location permission screen itself belongs to the onboarding of the Home (not built yet); the wizard does not depend on it.
- Signal rules (`GpsSignal`): up to 5 m good, up to 15 m fair (a corner can be marked), up to 30 m weak (the button turns grey and says why), beyond 30 m, or no fix for 10 s, is a lost signal. Agreed with Victor: above 15 m the corner button is blocked.
- Lost signal: the sheet offers "Seguir en el mapa" (corners are kept, the map step opens on them) or "Esperar la señal" (hides the sheet until the GPS is back).
- Steps are numbered by `RegisterPlotStep.number`: METHOD 1, TRACE 2, DETAILS 3, REVIEW 3 (as in Figma).
- Both methods share the same outline logic of the view model (`addCorner` slots the corner so the edges never cross), so a producer can switch method without losing corners.
- The walking map is a Mapbox map without gestures; the camera frames the corners marked so far together with the producer (never further out than zoom 16, then it follows the producer). Numbered corners, the position dot and its accuracy circle are drawn with Compose over the map, as `CornerHandles` does.
- GPS comes from Google's fused provider (`play-services-location`); `android.hardware.location.gps` is declared not required, so devices without GPS can still trace on the map.
- `GpsTrackingViewModel` only keeps the GPS on while its state is collected (5 s grace period).

## Scope
- In: method step, permission handling, GPS step with its states, lost-signal sheet, shared sheet pieces, strings (en/es), tests.
- Out: the onboarding permission screen, GPS-averaging per corner, the numbered corners on the map-trace method, Wireframes and Prototipo versions of the two new Figma states.

## Tasks
- [x] T1 — Domain: `GpsFix`, `GpsSignal`, `LocationTracker` port, `ObserveGpsFixesUseCase`.
- [x] T2 — Infrastructure: `FusedLocationTracker` and its DI module, dependency, manifest permissions.
- [x] T3 — Wizard: new METHOD step, `MarkingMethod`, step numbers, switch to map keeping corners.
- [x] T4 — `LocationPermission` helper and `MethodStep` (locked card, settings dialog).
- [x] T5 — `GpsTrackingViewModel`, `GpsStep`, `PlotGpsMap`, `LostSignalSheet`.
- [x] T6 — Unit tests (signal rules, reading states, camera framing, accuracy circle, wizard steps).
- [x] T7 — Checked on the emulator with mock locations: permission flow (deny, deny again, settings, grant), walk and mark corners, lost signal, continue on the map. The emulator draws no Mapbox annotations (the outline lines).
- [ ] T8 — On-device check by Victor (walking a real plot).

## Acceptance criteria
- `./gradlew assembleDebug testDebugUnitTest` succeeds.
- Without permission the GPS card is locked and the map is recommended; with it, GPS is recommended.
- A corner cannot be marked with more than 15 m of error; the button explains why.
- Losing the GPS keeps the corners and offers to continue on the map.
