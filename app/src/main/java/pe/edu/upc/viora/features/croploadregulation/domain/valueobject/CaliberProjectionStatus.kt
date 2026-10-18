package pe.edu.upc.viora.features.croploadregulation.domain.valueobject

/**
 * Why the caliber projection has, or has not, a figure (US28). [NOT_APPLICABLE] also covers a status an older app
 * does not know: the screen then shows no caliber at all.
 */
enum class CaliberProjectionStatus {
    /** The variety model is calibrated and covers the residual load. */
    ESTIMATED,

    /** Not enough real harvests of the variety yet. */
    NOT_CALIBRATED,

    /** The thinning was late: no harvest shows the effect of a late thinning. */
    NOT_ESTIMATED_LATE,

    /** The residual load is outside the loads the model was calibrated with. */
    OUTSIDE_CALIBRATION_RANGE,

    NOT_APPLICABLE,
    ;

    companion object {
        fun from(value: String?): CaliberProjectionStatus = entries.firstOrNull { it.name == value } ?: NOT_APPLICABLE
    }
}
