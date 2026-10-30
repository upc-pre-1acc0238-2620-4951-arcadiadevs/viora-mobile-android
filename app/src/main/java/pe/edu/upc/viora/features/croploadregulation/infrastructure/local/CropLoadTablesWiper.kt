package pe.edu.upc.viora.features.croploadregulation.infrastructure.local

import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper

/** The tables of this context, its prescriptions, thinning events and the sampling drafts: emptied when the account leaves the phone. */
class CropLoadTablesWiper @Inject constructor(
    private val thinningPrescriptionDao: ThinningPrescriptionDao,
    private val thinningEventDao: ThinningEventDao,
    private val draftTreeSampleDao: DraftTreeSampleDao,
) : AccountDataWiper {
    override suspend fun wipe() {
        thinningPrescriptionDao.wipeAll()
        thinningEventDao.wipeAll()
        draftTreeSampleDao.wipeAll()
    }
}
