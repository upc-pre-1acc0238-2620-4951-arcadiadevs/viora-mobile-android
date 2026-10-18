package pe.edu.upc.viora.features.croploadregulation.domain.valueobject

/** Lifecycle of a thinning prescription. [UNKNOWN] keeps an older app working when the backend adds a status. */
enum class PrescriptionStatus {
    SAMPLING_IN_PROGRESS,
    PRESCRIBED,
    CONFIRMED,
    EXECUTED,
    EXPIRED,
    VOIDED_BY_PLOT_REMOVAL,
    UNKNOWN,
    ;

    companion object {
        fun from(value: String?): PrescriptionStatus = entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}

/** An input still missing to issue the prescription. */
enum class PrescriptionBlocker {
    SAMPLING_NOT_REPRESENTATIVE,
    TARGET_NOT_CONFIGURED,
    FULL_BLOOM_MISSING,
    ;

    companion object {
        fun from(value: String?): PrescriptionBlocker? = entries.firstOrNull { it.name == value }
    }
}

/** Load against the target: up to 1.0 balanced, up to 1.3 moderate, above that severe (US26). */
enum class LoadState {
    BALANCED,
    MODERATE_OVERLOAD,
    SEVERE_OVERLOAD,
    ;

    companion object {
        fun from(value: String?): LoadState? = entries.firstOrNull { it.name == value }
    }
}

/** Who approved the technical profile that gives the target and the window. */
enum class ThinningProfileStatus {
    AGRONOMIST_APPROVED,
    PROVISIONAL,
    SYNTHETIC_DEMO,
    ;

    companion object {
        fun from(value: String?): ThinningProfileStatus? = entries.firstOrNull { it.name == value }
    }
}

enum class YieldEstimateStatus {
    AVAILABLE,
    LOAD_UNKNOWN,
    INSUFFICIENT_HISTORY,
    AREA_UNKNOWN,
    ;

    companion object {
        fun from(value: String?): YieldEstimateStatus = entries.firstOrNull { it.name == value } ?: LOAD_UNKNOWN
    }
}

enum class YieldEstimateBasis {
    CALIBER_ADJUSTED,
    FRUIT_COUNT_ONLY,
    ;

    companion object {
        fun from(value: String?): YieldEstimateBasis? = entries.firstOrNull { it.name == value }
    }
}
