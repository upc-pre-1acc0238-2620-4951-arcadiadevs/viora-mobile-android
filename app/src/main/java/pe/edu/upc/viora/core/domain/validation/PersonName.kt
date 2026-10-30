package pe.edu.upc.viora.core.domain.validation

/** The producer's full name as it is asked in the onboarding and in Mi cuenta. */
object PersonName {
    const val MIN_LETTERS = 2

    fun normalize(text: String): String = text.trim()

    /** At least [MIN_LETTERS] letters once trimmed, so digits or a lone initial are not a name. */
    fun isValid(text: String): Boolean = normalize(text).count(Char::isLetter) >= MIN_LETTERS

    /** Text up to the first space, for greetings ("Hola, Teodoro"). */
    fun firstName(text: String): String = normalize(text).substringBefore(' ')
}
