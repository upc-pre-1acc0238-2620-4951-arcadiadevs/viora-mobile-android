package pe.edu.upc.viora.core.locale

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/** The two languages of the app (US42): Spanish of Peru and English, the default resources. */
enum class AppLanguage(val tag: String) {
    SPANISH("es-PE"),
    ENGLISH("en-US"),
    ;

    companion object {

        /** The language the app shows: the one chosen in Viora or in the phone's settings, else the phone's. */
        fun current(context: Context): AppLanguage {
            val chosen = chosenLocale(context) ?: Resources.getSystem().configuration.locales[0]
            return if (chosen.language == "es") SPANISH else ENGLISH
        }

        /**
         * Switches the language without signing out. From Android 13 the system keeps the choice and
         * recreates the screen; before, the choice is kept here and the activity is recreated by hand.
         */
        fun apply(activity: Activity, language: AppLanguage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.getSystemService(LocaleManager::class.java).applicationLocales =
                    LocaleList.forLanguageTags(language.tag)
            } else {
                preferences(activity).edit().putString(KEY, language.tag).apply()
                activity.recreate()
            }
        }

        /** Before Android 13, gives the activity the chosen language; call it from `attachBaseContext`. */
        fun wrap(base: Context): Context {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) return base
            val locale = chosenLocale(base) ?: return base
            Locale.setDefault(locale)
            val configuration = Configuration(base.resources.configuration).apply { setLocale(locale) }
            return base.createConfigurationContext(configuration)
        }

        private fun chosenLocale(context: Context): Locale? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getSystemService(LocaleManager::class.java).applicationLocales
                    .takeUnless { it.isEmpty }?.get(0)
            } else {
                preferences(context).getString(KEY, null)?.let(Locale::forLanguageTag)
            }

        private fun preferences(context: Context) =
            context.applicationContext.getSharedPreferences("app_language", Context.MODE_PRIVATE)

        private const val KEY = "language_tag"
    }
}
