package pe.edu.upc.viora.features.subscription.domain.repository

import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode

interface CooperativeCodeRepository {

    /** Redeems [code] for the signed-in producer. Needs the connection; nothing is cached. */
    suspend fun redeem(code: InvitationCode): RedemptionOutcome
}
