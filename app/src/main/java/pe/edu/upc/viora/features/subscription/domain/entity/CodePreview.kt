package pe.edu.upc.viora.features.subscription.domain.entity

import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError

/**
 * What a cooperative code offers before it is redeemed (P05b): who pays, who would read the producer's data, how
 * many hectares it covers and until when. Asking for it does not spend the code.
 */
data class CodePreview(
    val organizationName: String,
    /** The manager who would read the data; null while the license has none. */
    val managerName: String?,
    val quotaHectares: Double,
    val planEndsAt: Instant?,
    val registeredHectares: Double,
    /** The producer already has more hectares than the code covers (P05b · Cupo menor). */
    val exceedsQuota: Boolean,
    /** The version of the consent text shown; it goes back with the redemption to accept sharing. */
    val consentTextVersion: String,
)

/** What happened when the producer asked what a code offers. */
sealed interface CodePreviewOutcome {

    data class Previewed(val preview: CodePreview) : CodePreviewOutcome

    /** The code does not exist, expired, was already redeemed or was revoked. */
    data object NotRedeemable : CodePreviewOutcome

    /** The producer already has an active plan: plans do not combine. */
    data object AlreadySubscribed : CodePreviewOutcome

    /** Too many codes were tried; the backend refuses more for a while. */
    data object TooManyAttempts : CodePreviewOutcome

    data class Failed(val error: AppError) : CodePreviewOutcome
}
