package pe.edu.upc.viora.features.iam.domain.valueobject

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType

/**
 * Mobile number rules of the profile, with libphonenumber like the backend: it must be a valid
 * *mobile* number of the chosen country (a landline or a number of another country is refused).
 */
object MobileNumbers {

    private val util: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }

    fun isValidMobile(national: String, countryIso: String): Boolean {
        val number = parse(national, countryIso) ?: return false
        if (!util.isValidNumberForRegion(number, countryIso)) return false
        val type = util.getNumberType(number)
        return type == PhoneNumberType.MOBILE || type == PhoneNumberType.FIXED_LINE_OR_MOBILE
    }

    /** E.164 ("+51987654321"), the only format the backend accepts. */
    fun e164(national: String, countryIso: String): String =
        parse(national, countryIso)?.let { util.format(it, PhoneNumberFormat.E164) }
            ?: "+${util.getCountryCodeForRegion(countryIso)}$national"

    /** For reading ("+51 987 654 321"). */
    fun international(national: String, countryIso: String): String =
        parse(national, countryIso)?.let { util.format(it, PhoneNumberFormat.INTERNATIONAL) }
            ?: "+${util.getCountryCodeForRegion(countryIso)} $national"

    private fun parse(national: String, countryIso: String) = try {
        util.parse(national, countryIso)
    } catch (e: NumberParseException) {
        null
    }
}
