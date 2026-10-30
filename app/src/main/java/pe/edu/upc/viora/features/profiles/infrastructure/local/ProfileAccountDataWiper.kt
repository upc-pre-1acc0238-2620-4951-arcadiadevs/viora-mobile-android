package pe.edu.upc.viora.features.profiles.infrastructure.local

import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper

/** The profile kept for Mi cuenta and the Home greeting belongs to the account that downloaded it. */
class ProfileAccountDataWiper @Inject constructor(private val profileStore: ProfileStore) : AccountDataWiper {
    override suspend fun wipe() = profileStore.clear()
}
