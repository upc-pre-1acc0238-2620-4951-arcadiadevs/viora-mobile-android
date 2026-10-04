# Big Picture Event Storming (As-Is): Dominio del Manejo de Vecería en Olivicultura

Este documento compila la transcripción estructurada, la interpretación semántica y la trazabilidad técnica del taller de **Big Picture Event Storming (As-Is)** para el proyecto **Viora**, basado en el análisis de las 6 fases metodológicas ejecutadas sobre el ecosistema productivo de la macro-región sur de Perú.

---

## 1. Convención Cromática y Notación

* **Naranja (Rectángulo):** *Domain Events* (Hechos del dominio en tiempo pasado e inglés).
* **Naranja (Rombo a 45°):** *Generic Events / High Complexity* (Eventos genéricos que enmascaran respuestas fisiológicas o complejidad no desagregada).
* **Amarillo (Cuadrado chico superior):** *Personas / Actors* (Roles humanos decisores o ejecutores).
* **Amarillo (Cuadrado grande):** *Temporal Milestones* (Hitos temporales agronómicos que anclan la línea de tiempo).
* **Azul (Cuadrado chico inferior):** *External Systems* (Sistemas externos, canales informales o soportes físicos).
* **Morado:** *Hotspots* (Fricciones críticas, riesgos y puntos de dolor actuales).
* **Verde:** *Opportunities* (Oportunidades de intervención y diseño de solución para Viora).
* **Lila:** *Empirical Policies* (Reglas empíricas o heurísticas no científicas de los agricultores).
* **Flechas Azules (Fase 6):** *Directional Votes* (Votos de riesgo de hipótesis para conformación del MVP).

---

## 2. Inventario Consolidado de Elementos del Dominio

### 2.1. Actores (Personas)
1. **Olive Farmer:** Productor agrícola independiente o agremiado en cooperativa.
2. **Technical Advisor:** Ingeniero agrónomo o gestor técnico de campo de la organización/cooperativa.
3. **Neighbor:** Agricultor par que asume un rol informal de soporte ante emergencias.

### 2.2. Sistemas Externos (External Systems)
1. **Phone:** Llamadas telefónicas de emergencia.
2. **WhatsApp:** Mensajería instantánea para consultas informales y fotos de plagas.
3. **SENAMHI:** Servicio Nacional de Meteorología e Hidrología del Perú (alertas climáticas macro).
4. **Field Notebook:** Cuaderno de campo físico y registros en papel.
5. **Cooperativa:** Organización gremial que proyecta acopio y consolida volúmenes.
6. **Acopiador:** Comerciante intermediario que fija el precio local de compra.

### 2.3. Hitos Temporales (Temporal Milestones)
1. **Letargo invernal y acumulación de frío**
2. **Floración y cuajado**
3. **Crecimiento del fruto y competencia fuente-sumidero**
4. **Búsqueda reactiva de asistencia técnica** (carril transversal concurrente)
5. **Cosecha del Año ON**
6. **Campaña siguiente: Año OFF** (con cierre de ciclo hacia el hito 1)

---

## 3. Transcripción Fase por Fase

---

### Fase 1: Exploración Caótica y Generación de Domain Events

En esta fase exploratoria se levantaron 34 eventos iniciales más 4 eventos en rombo (complejidad oculta), dispuestos en grilla de lluvia de ideas libre ("Guess First"):

#### Fila 1:
* `ClimaticAnomalyPerceived`
* `ChillHoursMissed`
* `AgriculturalAlertReceived`
* `HeavyBloomObserved`
* `ExcessiveFruitSetPerceived`
* `BranchOverloadNoticed`
* `OffYearForecastedFromMemory`
* `ThinningDecidedByIntuition`
* `ThinningWindowMissed`
* `TippingPruningPostponed`

#### Fila 2:
* `ShootGrowthStalled`
* `IrrigationKeptUniformAcrossPlot`
* `TechnicalAdvisorContactedByPhone`
* `TechnicalAdvisorAvailabilityChecked`
* `VisitScheduled`
* `VisitDelayed`
* `NeighborConsultedViaWhatsApp`
* `PlotManuallyInspected`
* `RecommendationVerballyIssued`
* `ProducerCallsAccumulated`

#### Fila 3:
* `PlotsPrioritizedByAdvisorMemory`
* `LowYieldRegistered`
* `HarvestEstimatedByEye`
* `HarvestDataWrittenInNotebook`
* `OffYearYieldCollapsed` *(Evento semilla facilitador)*
* `HarvestReduced`
* `CooperativeVolumeCommitmentBroken`
* `PriceDroppedByRegionalOversupply`
* `CropSoldAtLowerMargin`
* `CooperativeForecastEstimatedRoughly`

#### Fila 4 (Rombos - Complejidad Oculta):
* `AlternateBearingTriggered`
* `CropWeakened`
* `ProductionAffected`
* `AdverseProgressNoted`

---

### Fase 2: Imponer la Línea de Tiempo

Los eventos se estructuraron bajo los 6 Hitos Temporales en islas lógicas y cadenas de causa-efecto estrictas:

* **Hito 1: Letargo invernal y acumulación de frío**
  * *Isla Climática:* `ClimaticAnomalyPerceived` ➔ `ChillHoursMissed`
  * *Isla de Contexto:* `AgriculturalAlertReceived` | `OffYearForecastedFromMemory` | `TippingPruningPostponed`
* **Hito 2: Floración y cuajado**
  * *Cadena 1 (Sobrecarga):* `HeavyBloomObserved` ➔ `ExcessiveFruitSetPerceived` ➔ `BranchOverloadNoticed`
  * *Cadena 2 (Pérdida de ventana):* `ThinningDecidedByIntuition` ➔ `ThinningWindowMissed` ➔ `AlternateBearingTriggered` (Rombo)
* **Hito 3: Crecimiento del fruto y competencia fuente-sumidero**
  * *Cadena Fisiológica:* `ShootGrowthStalled` ➔ `CropWeakened` (Rombo)
  * *Cadena Inspección:* `PlotManuallyInspected` ➔ `AdverseProgressNoted` (Rombo)
  * *Isla Manejo:* `IrrigationKeptUniformAcrossPlot`
* **Hito 4: Búsqueda reactiva de asistencia técnica (Carril inferior)**
  * *Bifurcación de contacto:* `TechnicalAdvisorContactedByPhone` ➔ `TechnicalAdvisorAvailabilityChecked`
    * *Rama A (Éxito):* `VisitScheduled` ➔ `RecommendationVerballyIssued`
    * *Rama B (Demora):* `VisitDelayed` ➔ `NeighborConsultedViaWhatsApp`
  * *Sobrecarga del asesor:* `ProducerCallsAccumulated` ➔ `PlotsPrioritizedByAdvisorMemory`
* **Hito 5: Cosecha del Año ON**
  * *Cadena Registro:* `HarvestEstimatedByEye` ➔ `HarvestDataWrittenInNotebook`
  * *Cadena Comercial:* `PriceDroppedByRegionalOversupply` ➔ `CropSoldAtLowerMargin`
  * *Isla Gremial:* `CooperativeForecastEstimatedRoughly`
* **Hito 6: Campaña siguiente: Año OFF**
  * *Cadena Colapso:* `OffYearYieldCollapsed` ➔ `LowYieldRegistered`
  * *Cadena Cooperativa:* `ProductionAffected` (Rombo) ➔ `CooperativeVolumeCommitmentBroken`
* **Cierre del ciclo bianual:** Flecha de retorno desde `CooperativeVolumeCommitmentBroken` hacia el inicio del Hito 1 (`Letargo invernal y acumulación de frío`).

---

### Fase 3: Integración de Personas y Sistemas Externos

Se mapearon los responsables humanos y canales/herramientas sobre los eventos clave:

| Hito Temporal | Evento(s) | Actor Vinculado | Sistema Externo Vinculado |
| :--- | :--- | :--- | :--- |
| **Letargo Invernal** | `ClimaticAnomalyPerceived` | Olive Farmer | — |
| | `AgriculturalAlertReceived` | — | SENAMHI |
| | `OffYearForecastedFromMemory` | Olive Farmer | — |
| | `TippingPruningPostponed` | Olive Farmer | — |
| **Floración y Cuajado** | `HeavyBloomObserved` | Olive Farmer | — |
| | `ThinningDecidedByIntuition` | Olive Farmer | — |
| **Crecimiento Fruto** | `PlotManuallyInspected` | Olive Farmer | — |
| | `IrrigationKeptUniformAcrossPlot`| Olive Farmer | — |
| **Búsqueda Reactiva** | `TechnicalAdvisorContactedByPhone`| Olive Farmer | Phone |
| | `VisitScheduled` | Technical Advisor | — |
| | `NeighborConsultedViaWhatsApp` | Neighbor | WhatsApp |
| | `ProducerCallsAccumulated` | Technical Advisor | — |
| **Cosecha Año ON** | `HarvestEstimatedByEye` | Olive Farmer | — |
| | `HarvestDataWrittenInNotebook` | — | Field Notebook |
| | `PriceDroppedByRegionalOversupply`| — | Acopiador |
| | `CropSoldAtLowerMargin` | — | Acopiador |
| | `CooperativeForecastEstimatedRoughly`| Technical Advisor | Cooperativa |
| **Año OFF** | `LowYieldRegistered` | Olive Farmer | Field Notebook |
| | `CooperativeVolumeCommitmentBroken`| — | Cooperativa |

*Nota Fisiológica:* `ShootGrowthStalled`, `OffYearYieldCollapsed`, `AlternateBearingTriggered`, `CropWeakened` y `ProductionAffected` no poseen actor porque corresponden a respuestas biológicas automáticas de la planta frente al agotamiento de carbohidratos.

---

### Fase 4: Storytelling y Narrativa Reversa

El análisis hacia atrás desde `CooperativeVolumeCommitmentBroken` evidenció huecos en la cadena fisiológica y de gestión, integrando **4 nuevos eventos** (totalizando 37 eventos):

1. `PreviousSeasonRecordsSearched` (Ubicado en Letargo Invernal): Búsqueda manual e infructuosa de datos históricos.
   * *Actor:* Olive Farmer | *Sistema:* Field Notebook.
2. `HarvestLaborContracted` (Ubicado en Cosecha del Año ON): Contratación a ciegas de jornales de cosecha.
   * *Conexión:* Precede a `HarvestEstimatedByEye`.
3. `CarbohydrateReservesDepleted` (Ubicado en Año OFF): Agotamiento fisiológico de reservas energéticas del árbol.
   * *Conexión:* Nace de la sobrecarga del Año ON y desemboca en `FloweringFailedNextSeason`.
4. `FloweringFailedNextSeason` (Ubicado en Año OFF): Falla masiva de floración como consecuencia directa del agotamiento de reservas.
   * *Conexión:* Desemboca directamente en `OffYearYieldCollapsed`.

---

### Fase 5: Puntos Calientes, Oportunidades y Políticas

Se ubicaron directamente sobre el tablero las fricciones estructurales, las ideas de solución y la regla empírica tradicional:

#### Hotspots (Morado — Fricciones y Puntos Críticos):
* **Hotspot 1 — Unmeasured Crop Load:** (Anclado en `ThinningWindowMissed`). La carga frutal jamás se mide de forma cuantitativa, perdiendo la única ventana de regulación real.
* **Hotspot 2 — Lack of History:** (Anclado en `HarvestDataWrittenInNotebook`). El registro físico en papel no permite generar trazabilidad, cruces climáticos ni predicción del ciclo.
* **Hotspot 3 — Reactive Advisory:** (Anclado en `VisitDelayed`). El asesor llega cuando la flor o el fruto ya sufrieron el daño; la atención depende de llamadas urgentes.
* **Hotspot 4 — Campaign Uncertainty:** (Anclado en `PreviousSeasonRecordsSearched`). Incertidumbre absoluta sobre el rendimiento futuro por falta de métricas propias del fundo.
* **Hotspot 5 — Income Volatility:** (Anclado en `CropSoldAtLowerMargin`). Caída abrupta de rentabilidad por vender a precios fijados por sobreoferta estacional.

#### Oportunidades (Verde — Soluciones Viora):
* **Oportunidad 1:** Alerta de ventana de aclareo según estado fenológico de la parcela (Resuelve *Unmeasured Crop Load*).
* **Oportunidad 2:** Registro digital de carga y peso por parcela (Resuelve *Lack of History*).
* **Oportunidad 3:** Cálculo automático del BBI (*Biennial Bearing Index*) a partir del histórico acumulado (Resuelve *Campaign Uncertainty*).
* **Oportunidad 4:** Priorización de visitas del asesor técnico con datos reales de parcela (Resuelve *Reactive Advisory*).

#### Política Empírica (Lila):
* **Regla Tradicional:** *"Si el año anterior fue de alta carga, se asume que este será bajo y no se invierte en poda ni fertilización"*. (Ubicada en Letargo Invernal).

---

### Fase 6: Definición del MVP (Votación)

Se aplicaron votos de dirección (flechas azules) enfocados en las **hipótesis de negocio de mayor riesgo y dependencia operativa**:

* **Ganador Absoluto (Foco Primario del MVP):** `Unmeasured Crop Load`
  * *Votos:* Concentró la mayor densidad de flechas azules (4 votos directos).
  * *Justificación de Negocio:* Si el olivicultor no adquiere el hábito de cuantificar y registrar la carga frutal, el motor analítico de Viora no puede emitir alertas de aclareo ni calcular índices de vecería.
* **Segundo Foco Prioritario:** `Lack of History`
  * *Votos:* Concentró 2 votos directos.
  * *Justificación de Negocio:* Resuelve la captura y estructuración del dato histórico a lo largo del tiempo, condición indispensable para alimentar la inteligencia predictiva del cultivo.
* **Descartados para el MVP Inicial:**
  * *Reactive Advisory:* Problema de coordinación operativa abordable en versiones posteriores.
  * *Campaign Uncertainty:* Consecuencia de los dos primeros Hotspots.
  * *Income Volatility:* Variable exógena condicionada por el mercado mayorista.