package pe.edu.upc.viora.features.telemetry.presentation.state

import java.time.Instant
import java.time.ZoneId
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.telemetry.domain.entity.MetricPoint
import pe.edu.upc.viora.features.telemetry.domain.entity.MetricStats
import pe.edu.upc.viora.features.telemetry.domain.entity.TelemetrySeries
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SoilMoistureStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryRange

/** What the metric detail screen shows (Figma P91 "Humedad del suelo"; US17, scenario 1). */
sealed interface TelemetryDetailUiState {

    /** The metric and range the producer picked, available in every state. */
    val metric: TelemetryMetric
    val range: TelemetryRange

    data class Loading(
        override val metric: TelemetryMetric,
        override val range: TelemetryRange,
    ) : TelemetryDetailUiState

    data class Error(
        override val metric: TelemetryMetric,
        override val range: TelemetryRange,
        val error: AppError,
    ) : TelemetryDetailUiState

    /** Cached readings of the window, possibly stale ([lastRefresh] is when they were downloaded). */
    data class Content(
        override val metric: TelemetryMetric,
        override val range: TelemetryRange,
        val plotName: String,
        val series: TelemetrySeries,
        val now: Instant,
        val zone: ZoneId,
        val lastRefresh: Instant?,
        val isRefreshing: Boolean,
        val refreshError: AppError?,
    ) : TelemetryDetailUiState {

        val points: List<MetricPoint> get() = series.points(metric)

        /** The latest measured value with its timestamp (US17: "highlights the last value"). */
        val latest: MetricPoint? get() = points.lastOrNull()

        val stats: MetricStats? get() = series.stats(metric)

        /** Only the soil moisture has a recharge point, irrigation events and an irrigation advice. */
        val soilStatus: SoilMoistureStatus?
            get() = if (metric == TelemetryMetric.SOIL_MOISTURE) series.soilStatus() else null

        val irrigationEvents: List<Instant>
            get() = if (metric == TelemetryMetric.SOIL_MOISTURE) series.irrigationEvents() else emptyList()

        val projectedWatchLevelAt: Instant?
            get() = if (metric == TelemetryMetric.SOIL_MOISTURE) series.projectedWatchLevelAt() else null

        /** The readings on screen are the stored ones because the last download failed. */
        val isFromCache: Boolean get() = points.isNotEmpty() && refreshError != null
    }
}
