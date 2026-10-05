package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Payload sent to `POST /api/v1/plots/{plotId}/samplings` to register an in-field sampling batch.
 */
@Serializable
data class SamplingBatchRequestDto(
    val campaignYear: Int,
    val samplingBatchId: String,
    val samples: List<TreeSampleRequestDto>,
)
