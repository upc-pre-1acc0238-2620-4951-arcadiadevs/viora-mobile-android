package pe.edu.upc.viora.features.telemetry.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode

interface SensorRepository {
    /** Emits the list of cached sensor nodes for the given plot. */
    fun observeNodes(plotId: String): Flow<List<SensorNode>>

    /** Refreshes sensor nodes from remote API and updates local cache. */
    suspend fun refresh(plotId: String): AppResult<Unit>

    /** Links a new virtual sensor node (US13). */
    suspend fun linkNode(newNode: NewSensorNode): AppResult<SensorNode>

    /** Updates the editable configuration of a virtual sensor node (US15). */
    suspend fun updateNode(node: SensorNode): AppResult<SensorNode>

    /** Removes a virtual sensor from the active inventory while preserving telemetry history (US16). */
    suspend fun unlinkNode(plotId: String, nodeId: String): AppResult<Unit>
}
