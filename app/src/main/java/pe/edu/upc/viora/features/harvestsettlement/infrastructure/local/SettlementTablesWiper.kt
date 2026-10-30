package pe.edu.upc.viora.features.harvestsettlement.infrastructure.local

import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper

/** The tables of this context, its settlements, the queued ones and the dossiers: emptied when the account leaves the phone. */
class SettlementTablesWiper @Inject constructor(
    private val harvestSettlementDao: HarvestSettlementDao,
    private val pendingSettlementDao: PendingSettlementDao,
    private val agronomicDossierDao: AgronomicDossierDao,
) : AccountDataWiper {
    override suspend fun wipe() {
        harvestSettlementDao.wipeAll()
        pendingSettlementDao.wipeAll()
        agronomicDossierDao.wipeAll()
    }
}
