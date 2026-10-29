package pe.edu.upc.viora.core.presentation

import androidx.annotation.StringRes
import pe.edu.upc.viora.R

/**
 * Countries of the operation the onboarding offers (T02b2 "PE +51"): how long their mobile numbers
 * are and how they start, for the input limit and the error hint. Validity itself is checked with
 * [pe.edu.upc.viora.core.domain.MobileNumbers].
 */
enum class PhoneCountry(
    val iso: String,
    val dialCode: String,
    @StringRes val nameRes: Int,
    val nationalDigits: Int,
    @StringRes val invalidHintRes: Int,
) {
    PE("PE", "+51", R.string.iam_country_pe, 9, R.string.onboarding_phone_invalid_pe),
    ES("ES", "+34", R.string.iam_country_es, 9, R.string.onboarding_phone_invalid_es),
    CL("CL", "+56", R.string.iam_country_cl, 9, R.string.onboarding_phone_invalid_cl),
    ;

    companion object {
        fun fromIso(iso: String): PhoneCountry = entries.firstOrNull { it.iso == iso } ?: PE
    }
}
