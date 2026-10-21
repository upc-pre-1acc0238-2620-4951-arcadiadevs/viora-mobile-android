package pe.edu.upc.viora.features.subscription.domain.entity

import pe.edu.upc.viora.core.domain.AppError

/** What happened when the producer redeemed a code. */
sealed interface RedemptionOutcome {

    data class Redeemed(val membership: CooperativeMembership) : RedemptionOutcome

    /** The code does not exist, expired, was already redeemed or was revoked: the backend does not say which. */
    data object NotRedeemable : RedemptionOutcome

    /** The producer already has an active subscription. */
    data object AlreadySubscribed : RedemptionOutcome

    /** Too many codes were tried; the backend refuses more for a while. */
    data object TooManyAttempts : RedemptionOutcome

    data class Failed(val error: AppError) : RedemptionOutcome
}
