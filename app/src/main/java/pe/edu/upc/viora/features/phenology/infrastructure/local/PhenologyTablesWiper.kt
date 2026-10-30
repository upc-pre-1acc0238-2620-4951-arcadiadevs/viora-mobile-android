package pe.edu.upc.viora.features.phenology.infrastructure.local

import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper

/** The tables of this context, its harvest records, bearing indexes and chill trackers: emptied when the account leaves the phone. */
class PhenologyTablesWiper @Inject constructor(
    private val harvestRecordDao: HarvestRecordDao,
    private val bearingIndexDao: BearingIndexDao,
    private val chillTrackerDao: ChillTrackerDao,
) : AccountDataWiper {
    override suspend fun wipe() {
        harvestRecordDao.wipeAll()
        bearingIndexDao.wipeAll()
        chillTrackerDao.wipeAll()
    }
}
