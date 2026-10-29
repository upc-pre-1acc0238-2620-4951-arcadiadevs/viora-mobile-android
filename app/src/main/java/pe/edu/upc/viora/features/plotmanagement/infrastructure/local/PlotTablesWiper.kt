package pe.edu.upc.viora.features.plotmanagement.infrastructure.local

import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper

/** The tables of this context, its plots and field notes: emptied when the account leaves the phone. */
class PlotTablesWiper @Inject constructor(
    private val plotDao: PlotDao,
    private val fieldNoteDao: FieldNoteDao,
) : AccountDataWiper {
    override suspend fun wipe() {
        plotDao.wipeAll()
        fieldNoteDao.wipeAll()
    }
}
