package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import kotlinx.serialization.Serializable

/**
 * Payload sent to `POST /api/v1/plots/{plotId}/samplings` to register an in-field sampling batch.
 */
@Serializable
data class SamplingBatchRequestDto(
    val clientBatchId: String,
    val campaignYear: Int,
    val samples: List<TreeSampleRequestDto>,
)
