package pe.edu.upc.viora.features.plotmanagement.domain.repository

import kotlinx.coroutines.flow.Flow

/** Remembers that the member already answered P06b · Sector de tus lotes, which is asked only once. */
interface SectorPromptRepository {

    /** True once the member saved or put off P06b on this account. */
    val answered: Flow<Boolean>

    suspend fun markAnswered()
}
