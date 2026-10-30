package pe.edu.upc.viora.core.domain

/** The constant of [E] named [name] (trimmed, any case), or null when there is none. */
inline fun <reified E : Enum<E>> enumOrNull(name: String?): E? {
    val wanted = name?.trim() ?: return null
    return enumValues<E>().firstOrNull { it.name.equals(wanted, ignoreCase = true) }
}

/** Like [enumOrNull], with [default] for a name the app does not know (a value the server added later). */
inline fun <reified E : Enum<E>> enumOrDefault(name: String?, default: E): E = enumOrNull<E>(name) ?: default
