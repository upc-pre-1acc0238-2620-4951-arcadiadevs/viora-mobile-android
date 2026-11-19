# M-07 · Auditoría de uso sin conexión

Estado observado en el cliente Android. «En caché» significa que los datos se descargaron previamente en este dispositivo y no se borraron al cerrar sesión; no garantiza que estén actualizados. Las operaciones remotas requieren conexión. Esta auditoría no implementa las brechas.

## Consulta y registros

| Pantalla o registro | Consulta sin conexión | Registro sin conexión y sincronización | Estado en la interfaz |
| --- | --- | --- | --- |
| Lotes y detalle de lote | Lotes activos y archivados previamente descargados desde Room. | El registro y la edición del lote requieren servicio; no se identificó una cola de alta o edición. | No hay estado pendiente para estos cambios. |
| Clima, sensores, pronóstico y centro de alertas | Lecturas, sensores, pronóstico e incidentes previamente descargados desde Room. El detalle completo de una alerta se obtiene del servicio. | Posponer una alerta y completar una medida requieren servicio; no quedan en cola. | Las consultas disponen de marcas de última actualización donde están implementadas; no hay estado pendiente para estas acciones. |
| Frío invernal e historial de cosechas | Métricas de frío, historial e índice previamente sincronizados desde Room. | No se identificó una cola general de cambios de fenología. | Son datos en caché, no registros locales pendientes. |
| Muestras de aclareo | Borradores de muestras guardados en Room; también eventos y planes previamente descargados. | Las muestras quedan como borradores locales y `SamplingSyncWorker` intenta enviarlas con conectividad; la pantalla también reintenta al recuperar conexión. | La ronda distingue muestras sincronizadas y locales y muestra conteos. |
| Liquidación de cosecha | Liquidaciones descargadas y liquidaciones pendientes en Room. | Ante un error de conectividad, la liquidación se guarda con clave de idempotencia y `SettlementSyncWorker` intenta enviarla; puede requerir corrección ante errores o conflictos. | La pantalla de cierre distingue pendiente, error, conflicto y liquidación confirmada. |
| Notas de campo y fotos | Las notas guardadas localmente se observan desde Room. | La nota se guarda primero como pendiente y `FieldNoteSyncWorker` envía el texto y luego la foto pendiente. Los errores permanentes se marcan como fallidos. | El detalle muestra sincronizada, en el teléfono o fallida. |
| Dossier agronómico | Se puede consultar el dossier previamente guardado y un PDF ya descargado, si sigue disponible en el dispositivo. | Sellar, descargar un PDF no guardado y verificar requieren servicio; no hay cola de sellado. | La pantalla comunica la condición sin conexión; no hay estado pendiente de sellado. |
| Asistente y resumen | Respuestas recientes y algunas respuestas o resúmenes previamente guardados pueden recuperarse de la caché. | Las preguntas nuevas requieren el servicio; no se identificó una cola de preguntas. | No hay estado pendiente de envío de preguntas. |
| Cuenta y preferencias | El perfil guardado y las preferencias locales se pueden consultar en el dispositivo. | Las preferencias locales se guardan en el teléfono; las operaciones de cuenta requieren servicio. | Las preferencias locales no representan una cola de sincronización. |

## Brechas frente a S3

| Criterio | Brecha observada |
| --- | --- |
| Consultar datos sincronizados | El detalle completo de alertas y las funciones que solo consultan el servicio no están garantizados sin conexión; la caché depende de una descarga previa. |
| Guardar registros y enviarlos al recuperar conexión | La cola no cubre todas las acciones: alta/edición de lotes, medidas y aplazamiento de alertas, sellado de dossier y preguntas al asistente requieren conexión. |
| Distinguir pendiente de sincronizado | Hay estados explícitos en notas, muestras y liquidaciones; no existe un indicador universal para los demás tipos de registro ni para todas las consultas en caché. |
| Conservar registros pendientes | El cierre de sesión limpia los datos locales; la pantalla de cuenta advierte sobre registros sin enviar. No se debe asumir que sobreviven a esa acción. |

## Evidencia de código

- `features/plotmanagement/infrastructure/repository/PlotRepositoryImpl.kt` y `FieldNoteRepositoryImpl.kt`.
- `features/telemetry/infrastructure/repository/IncidentRepositoryImpl.kt`, `ForecastRepositoryImpl.kt`, `SensorRepositoryImpl.kt` y `TelemetryRepositoryImpl.kt`.
- `features/phenology/infrastructure/repository/ChillRepositoryImpl.kt` y `HarvestRecordRepositoryImpl.kt`.
- `features/croploadregulation/infrastructure/repository/ThinningRepositoryImpl.kt` y `infrastructure/sync/SamplingSyncWorker.kt`.
- `features/harvestsettlement/infrastructure/repository/HarvestSettlementRepositoryImpl.kt`, `AgronomicDossierRepositoryImpl.kt` e `infrastructure/sync/SettlementSyncWorker.kt`.
- `features/plotmanagement/infrastructure/sync/FieldNoteSyncWorker.kt`, `presentation/ui/FieldNoteDetailSheet.kt` y `features/croploadregulation/presentation/ui/SamplingRoundScreen.kt`.
- `features/assistant/infrastructure/repository/AssistantRepositoryImpl.kt` y `features/profiles/presentation/viewmodel/AccountViewModel.kt`.

Las rutas anteriores son relativas a `app/src/main/java/pe/edu/upc/viora/`.
