package pe.edu.upc.viora.features.croploadregulation

import java.time.LocalDate
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CaliberProjection
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CurrentLoad
import pe.edu.upc.viora.features.croploadregulation.domain.entity.LoadBalance
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningPrescription
import pe.edu.upc.viora.features.croploadregulation.domain.entity.YieldEstimate
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.CaliberProjectionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningProfileStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateBasis
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateStatus

/** The plan of La Yarada as production returned it on 2026-10-16, demo profile on, bloom not recorded yet. */
const val REAL_PLAN_WITHOUT_BLOOM_JSON = """
{"id":"446f4803-f42d-4419-967d-470d88942449","plotId":"2975dae0-0df6-402e-9809-1579a88bb900","campaignYear":2026,
"targetFruitsPerShoot":null,"percentageToRemove":null,"loadUnit":"FRUITS_PER_SHOOT","status":"SAMPLING_IN_PROGRESS",
"fullBloomOn":null,"windowOpensOn":null,"windowClosesOn":null,"windowBasis":null,"profileVersion":null,"profileStatus":null,
"isWindowOpen":false,"blockers":["FULL_BLOOM_MISSING"],"issuedAt":null,"currentLoad":null,"sampledTrees":5,"treesNeeded":0,
"lastSampledOn":"2026-10-09","windowOpensDaysAfterBloom":14,"windowClosesDaysAfterBloom":49,
"yieldEstimate":{"status":"LOAD_UNKNOWN","sustainableKgPerHa":751.3,"estimatedKgPerHa":null,"basis":null,
"harvestCampaigns":4,"requiredHarvestCampaigns":3},"someFieldAddedLater":true}
"""

fun issuedPlan(
    percentage: Double = 30.0,
    loadState: LoadState = LoadState.SEVERE_OVERLOAD,
    opens: LocalDate = LocalDate.of(2026, 11, 3),
    closes: LocalDate = LocalDate.of(2026, 11, 30),
) = ThinningPrescription(
    id = "p-1",
    plotId = "plot-1",
    campaignYear = 2026,
    status = PrescriptionStatus.PRESCRIBED,
    percentageToRemove = percentage,
    targetFruitsPerShoot = 0.40,
    fullBloomOn = opens.minusDays(14),
    windowOpensOn = opens,
    windowClosesOn = closes,
    profileStatus = ThinningProfileStatus.SYNTHETIC_DEMO,
    blockers = emptySet(),
    currentLoad = CurrentLoad(0.57, 0.40, 1.43, loadState),
    sampledTrees = 5,
    treesNeeded = 0,
    lastSampledOn = LocalDate.of(2026, 10, 29),
    windowOpensDaysAfterBloom = 14,
    windowClosesDaysAfterBloom = 41,
    yieldEstimate = YieldEstimate(YieldEstimateStatus.AVAILABLE, 8200.0, 9800.0, YieldEstimateBasis.CALIBER_ADJUSTED, 4, 3),
)

fun blockedPlan(vararg blockers: PrescriptionBlocker, sampled: Int = 5) = issuedPlan().copy(
    status = PrescriptionStatus.SAMPLING_IN_PROGRESS,
    percentageToRemove = null,
    windowOpensOn = null,
    windowClosesOn = null,
    fullBloomOn = null,
    blockers = blockers.toSet(),
    currentLoad = null,
    sampledTrees = sampled,
    treesNeeded = (5 - sampled).coerceAtLeast(0),
)

/** A POST execution-confirmations answer shaped like the backend's (0.40.0), calibrated Sevillana. */
const val REAL_EXECUTION_JSON = """
{"prescriptionId":"p-1","confirmationId":"c-1","confirmationStatus":"OPTIMAL","executedDate":"2026-11-18",
"actualRemovalPercentage":30.0,"removedKg":null,"laborCrewSize":null,"notes":"  ","isOpportune":true,
"recordedAt":"2026-11-18T13:40:00Z","warning":null,
"loadBalance":{"preThinningFruitsPerShoot":0.57,"residualFruitsPerShoot":0.399,"targetFruitsPerShoot":0.40,
"deltaFruitsPerShoot":-0.001,"loadRatio":0.9975,"loadState":"BALANCED"},
"caliberProjection":{"status":"ESTIMATED","mostLikelyFruitsPerKg":104.3,"fruitsPerKgLow":93.8,"fruitsPerKgHigh":126.0,
"mostLikelySizeGrade":"101/110","sizeGradeLow":"91/100","sizeGradeHigh":"121/140","confidenceLevel":0.8,
"calibrationObservations":14,"requiredObservations":8,"model":"LOAD_RESPONSE_V1","calibratedLoadMin":0.25,"calibratedLoadMax":0.8},
"yieldEstimate":{"status":"AVAILABLE","sustainableKgPerHa":8200.0,"estimatedKgPerHa":8200.0,"basis":"CALIBER_ADJUSTED",
"harvestCampaigns":4,"requiredHarvestCampaigns":3}}
"""

fun recordedThinning(
    status: CaliberProjectionStatus = CaliberProjectionStatus.ESTIMATED,
    isOpportune: Boolean = true,
    loadState: LoadState = LoadState.BALANCED,
) = ThinningExecution(
    prescriptionId = "p-1",
    executedOn = LocalDate.of(2026, 11, 18),
    removalPercentage = 30.0,
    notes = null,
    isOpportune = isOpportune,
    loadBalance = LoadBalance(0.57, 0.40, 0.40, 1.0, loadState),
    caliberProjection = CaliberProjection(status, "101/110", "121/140", "91/100", 0.8, 14, 8, 0.25, 0.80),
    yieldEstimate = YieldEstimate(YieldEstimateStatus.AVAILABLE, 8200.0, 8200.0, YieldEstimateBasis.CALIBER_ADJUSTED, 4, 3),
)
