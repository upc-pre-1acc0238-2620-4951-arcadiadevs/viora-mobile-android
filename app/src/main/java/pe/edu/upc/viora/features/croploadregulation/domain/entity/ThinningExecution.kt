package pe.edu.upc.viora.features.croploadregulation.domain.entity

import java.time.LocalDate
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.CaliberProjectionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState

/**
 * The thinning the producer recorded (P62) and what it means for the campaign (P63): the load it left, the caliber
 * expected at harvest and the yield.
 *
 * @property isOpportune false when it was recorded after the window closed (the backend marks it LATE)
 */
data class ThinningExecution(
    val prescriptionId: String,
    val executedOn: LocalDate,
    val removalPercentage: Double,
    val notes: String?,
    val isOpportune: Boolean,
    val loadBalance: LoadBalance?,
    val caliberProjection: CaliberProjection?,
    val yieldEstimate: YieldEstimate?,
)

/** Fruits per shoot before and after the thinning against the target: residual = before x (1 - removed %). */
data class LoadBalance(
    val beforeFruitsPerShoot: Double,
    val residualFruitsPerShoot: Double,
    val targetFruitsPerShoot: Double,
    val loadRatio: Double,
    val loadState: LoadState,
)

/**
 * Commercial caliber expected at harvest in fruits per kg, graded with the IOC scale (e.g. "101/110"). The grades are
 * only present when [status] is [CaliberProjectionStatus.ESTIMATED]; [calibratedLoadMin]/[calibratedLoadMax] are the
 * residual loads the variety model covers.
 */
data class CaliberProjection(
    val status: CaliberProjectionStatus,
    val mostLikelySizeGrade: String?,
    val smallFruitSizeGrade: String?,
    val bigFruitSizeGrade: String?,
    val confidenceLevel: Double?,
    val calibrationObservations: Int,
    val requiredObservations: Int,
    val calibratedLoadMin: Double?,
    val calibratedLoadMax: Double?,
)
