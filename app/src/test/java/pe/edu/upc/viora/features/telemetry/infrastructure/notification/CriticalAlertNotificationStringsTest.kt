package pe.edu.upc.viora.features.telemetry.infrastructure.notification

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** The notifier formats both titles with the plot name, so every locale must keep the placeholder. */
class CriticalAlertNotificationStringsTest {

    private val titles = listOf("notification_critical_title", "notification_frost_title")
    private val locales = listOf("values", "values-es")

    @Test
    fun alertTitlesNameThePlotInEveryLocale() {
        for (locale in locales) {
            val strings = File("src/main/res/$locale/strings.xml").readText()
            for (name in titles) {
                val value = Regex("""<string name="$name">(.*?)</string>""").find(strings)?.groupValues?.get(1)
                assertTrue("$locale/$name must contain %1\$s, was: $value", value?.contains("%1\$s") == true)
            }
        }
    }
}
