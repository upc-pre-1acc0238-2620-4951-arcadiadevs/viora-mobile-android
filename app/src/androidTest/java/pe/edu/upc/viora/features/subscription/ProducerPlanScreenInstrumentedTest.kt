package pe.edu.upc.viora.features.subscription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.math.BigDecimal
import java.time.Instant
import java.util.Locale
import android.content.res.Configuration
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanOffer
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.presentation.state.ProducerPlanUiState
import pe.edu.upc.viora.features.subscription.presentation.ui.ProducerPlanContent

@RunWith(AndroidJUnit4::class)
class ProducerPlanScreenInstrumentedTest {
    @get:Rule val compose = createComposeRule()

    @Test fun selectionUpdatesEstimateAndCooperativeActionWorksInEnglish() = exerciseScreen("en-US")
    @Test fun selectionUpdatesEstimateAndCooperativeActionWorksInSpanish() = exerciseScreen("es-PE")

    private fun exerciseScreen(localeTag: String) {
        val offer = ProducerPlanOffer(ProducerPlanCode("PRODUCER_ANNUAL"), "PEN", BigDecimal("55"),
            BigDecimal("0.18"), PlanCapacity.of(BigDecimal("0.5")), PlanCapacity.of(BigDecimal("0.5")), 12, "test", true)
        var capacity by mutableStateOf(offer.minimumQuota)
        var cooperativeClicks = 0
        var increaseLabel = ""
        var decreaseLabel = ""
        var cooperativeLabel = ""
        var includedLabel = ""
        compose.setContent {
            val currentConfiguration = LocalConfiguration.current
            val baseContext = LocalContext.current
            val configuration = remember(currentConfiguration, localeTag) {
                Configuration(currentConfiguration).apply { setLocale(Locale.forLanguageTag(localeTag)) }
            }
            val localizedContext = remember(baseContext, configuration) { baseContext.createConfigurationContext(configuration) }
            CompositionLocalProvider(LocalContext provides localizedContext, LocalConfiguration provides configuration) {
            val step = java.text.NumberFormat.getNumberInstance(LocalConfiguration.current.locales[0]).format(0.5)
            increaseLabel = stringResource(R.string.producer_plan_increase, step)
            decreaseLabel = stringResource(R.string.producer_plan_decrease, step)
            cooperativeLabel = stringResource(R.string.producer_plan_cooperative)
            includedLabel = stringResource(R.string.producer_plan_tax_included)
            VioraTheme {
                ProducerPlanContent(ProducerPlanUiState.Content(listOf(offer), offer, capacity, Instant.EPOCH,
                    false, false, null), {}, { cooperativeClicks++ },
                    { capacity = PlanCapacity.of(capacity.hectares.add(offer.quotaIncrement.hectares)) },
                    { capacity = PlanCapacity.of(capacity.hectares.subtract(offer.quotaIncrement.hectares)) }, {}, {})
            }
            }
        }
        compose.onNodeWithContentDescription(decreaseLabel).assertIsNotEnabled()
        compose.onNodeWithTag("producer-plan-total").assertTextContains("330", substring = true)
        compose.onNodeWithTag("producer-plan-total").assertTextContains("S/", substring = true)
        compose.onNodeWithText("Base for 12 months:", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Base por 12 meses:", substring = true).assertDoesNotExist()
        compose.onNodeWithText("IGV (18%)", substring = true).assertDoesNotExist()
        compose.onNodeWithText("The final price and conditions", substring = true).assertDoesNotExist()
        compose.onNodeWithText("El precio y las condiciones definitivas", substring = true).assertDoesNotExist()
        compose.onNodeWithText(includedLabel).assertExists()
        val screenshot = compose.onRoot().captureToImage().asAndroidBitmap()
        val destination = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir(null)!!.resolve("producer-plan-$localeTag.png")
        destination.outputStream().use { screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        compose.onNodeWithContentDescription(increaseLabel).performClick()
        compose.onNodeWithContentDescription(decreaseLabel).assertIsEnabled()
        compose.onNodeWithTag("producer-plan-total").assertTextContains("660", substring = true)
        compose.onNodeWithText(cooperativeLabel).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, cooperativeClicks) }
    }
}
