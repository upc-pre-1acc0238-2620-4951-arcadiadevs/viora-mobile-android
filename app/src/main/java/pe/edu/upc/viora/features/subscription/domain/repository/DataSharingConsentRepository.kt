package pe.edu.upc.viora.features.subscription.domain.repository

import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.domain.entity.DataSharingConsent

/** The producer's consents to share their data with a cooperative. Read from the server; nothing is cached. */
interface DataSharingConsentRepository {

    /** The newest consent, or null when the producer never gave one. */
    suspend fun latest(): AppResult<DataSharingConsent?>

    /** Stops sharing for good; the plan goes on. Answers the consent as it stands now. */
    suspend fun withdraw(consentId: String): AppResult<DataSharingConsent>
}
