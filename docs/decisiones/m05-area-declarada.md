# Decisión M-05: área declarada del lote

**Fecha:** 2026-11-19
**Mejora relacionada:** M-05 – Validación del área delimitada (`docs/mejoras_app_android_viora.md`)
**Estado:** aprobada

## Contexto

M-05 pide comparar el área del polígono trazado con el área que el productor declara administrar y advertir antes de guardar cuando la diferencia sea considerable (caso observado: un lote de 0,03 ha guardado cuando la productora administra 3 ha).

Al revisar la aplicación se comprobó que **no existe ningún dato de área declarada**: el asistente de registro solo calcula el área del polígono y la compara con la cuota del Plan Productor. No había contra qué comparar.

## Decisión

Se agrega al asistente de registro de lote un campo **opcional** en el que el productor indica, de forma aproximada, cuántas hectáreas tiene el lote.

- El dato se usa **solo en la aplicación Android**, para advertir antes de guardar cuando el área trazada difiere considerablemente del área declarada.
- **No se envía al backend** ni cambia el contrato de la API. El lote se sigue guardando con el área calculada a partir del polígono.
- Si el productor deja el campo vacío, no se hace la comparación; se mantienen el área destacada y la escala del mapa.

## Alternativas descartadas

| Alternativa | Motivo |
|---|---|
| Guardar el área declarada en el backend | Requiere cambios en `ma-viora-platform` y en el contrato de la API; se puede retomar más adelante si el dato aporta valor fuera del registro. |
| Sin campo nuevo, solo advertir áreas anormalmente pequeñas | No cubre el criterio de aceptación de M-05 (comparar con el área declarada). |

## Impacto para el equipo

- **Backend:** ninguno.
- **Informe (`ma-viora-report`):** actualizar el escenario de US09 (registro de lote) para incluir el área aproximada opcional y la advertencia de diferencia de área.
- **Figma:** agregar el campo y el estado de advertencia en el flujo «07 · Registrar un lote».
