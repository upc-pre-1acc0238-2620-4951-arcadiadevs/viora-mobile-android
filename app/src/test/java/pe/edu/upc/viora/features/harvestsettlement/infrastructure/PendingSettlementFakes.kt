package pe.edu.upc.viora.features.harvestsettlement.infrastructure

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.harvestsettlement.domain.service.SettlementSyncScheduler
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.PendingSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.PendingSettlementEntity

internal class InMemoryPendingSettlementDao : PendingSettlementDao {
    val rows = MutableStateFlow<List<PendingSettlementEntity>>(emptyList())

    override fun observeAll(): Flow<List<PendingSettlementEntity>> = rows.map { it.sortedBy { row -> row.createdAt } }

    /** Runs right after [findByStatus] returns, to simulate an edit made while a sync pass is in flight. */
    var afterFindByStatus: (suspend () -> Unit)? = null

    override suspend fun findByStatus(status: String): List<PendingSettlementEntity> {
        val found = rows.value.filter { it.status == status }.sortedBy { it.createdAt }
        afterFindByStatus?.invoke()
        return found
    }

    override suspend fun find(plotId: String, campaignYear: Int) =
        rows.value.firstOrNull { it.plotId == plotId && it.campaignYear == campaignYear }

    override suspend fun upsert(entity: PendingSettlementEntity) {
        rows.value = rows.value.filterNot { it.plotId == entity.plotId && it.campaignYear == entity.campaignYear } + entity
    }

    override suspend fun delete(plotId: String, campaignYear: Int) {
        rows.value = rows.value.filterNot { it.plotId == plotId && it.campaignYear == campaignYear }
    }
}

internal class RecordingSyncScheduler : SettlementSyncScheduler {
    var scheduled = 0

    override fun schedule() {
        scheduled++
    }
}
