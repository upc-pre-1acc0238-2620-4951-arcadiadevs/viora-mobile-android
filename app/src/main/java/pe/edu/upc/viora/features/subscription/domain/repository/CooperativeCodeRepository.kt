package pe.edu.upc.viora.features.subscription.domain.repository

import pe.edu.upc.viora.features.subscription.domain.entity.CodePreviewOutcome
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode

/** Cooperative codes of the signed-in producer. Both calls need the connection; nothing is cached. */
interface CooperativeCodeRepository {

    /** What [code] offers, without spending it (P05b). */
    suspend fun preview(code: InvitationCode): CodePreviewOutcome

    /** Redeems [code], accepting to share the data under the consent text [consentTextVersion]. */
    suspend fun redeem(code: InvitationCode, consentTextVersion: String): RedemptionOutcome
}
