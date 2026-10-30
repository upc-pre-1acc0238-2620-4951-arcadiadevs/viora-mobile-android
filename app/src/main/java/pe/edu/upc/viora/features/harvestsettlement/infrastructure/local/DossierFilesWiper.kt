package pe.edu.upc.viora.features.harvestsettlement.infrastructure.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.AgronomicDossierRepositoryImpl

/** The sealed dossiers an account downloaded. */
class DossierFilesWiper @Inject constructor(@ApplicationContext private val context: Context) : AccountDataWiper {
    override suspend fun wipe() {
        File(context.filesDir, AgronomicDossierRepositoryImpl.DOSSIER_DIR).deleteRecursively()
    }
}
