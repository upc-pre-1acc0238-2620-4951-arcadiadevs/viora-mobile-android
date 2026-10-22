package pe.edu.upc.viora.features.subscription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.math.BigDecimal
import java.time.Instant
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

    @Test fun selectionUpdatesEstimateAndCooperativeActionWorks() {
        val offer = ProducerPlanOffer(ProducerPlanCode("PRODUCER_ANNUAL"), "PEN", BigDecimal("55"),
            BigDecimal("0.18"), PlanCapacity.of(BigDecimal("0.5")), PlanCapacity.of(BigDecimal("0.5")), 12, "test")
        var capacity by mutableStateOf(offer.minimumQuota)
        var cooperativeClicks = 0
        var increaseLabel = ""
        var decreaseLabel = ""
        var cooperativeLabel = ""
        compose.setContent {
            val step = java.text.NumberFormat.getNumberInstance(LocalConfiguration.current.locales[0]).format(0.5)
            increaseLabel = stringResource(R.string.producer_plan_increase, step)
            decreaseLabel = stringResource(R.string.producer_plan_decrease, step)
            cooperativeLabel = stringResource(R.string.producer_plan_cooperative)
            VioraTheme {
                ProducerPlanContent(ProducerPlanUiState.Content(listOf(offer), offer, capacity, Instant.EPOCH,
                    false, false, null), {}, { cooperativeClicks++ },
                    { capacity = PlanCapacity.of(capacity.hectares.add(offer.quotaIncrement.hectares)) },
                    { capacity = PlanCapacity.of(capacity.hectares.subtract(offer.quotaIncrement.hectares)) }, {}, {})
            }
        }
        compose.onNodeWithContentDescription(decreaseLabel).assertIsNotEnabled()
        compose.onNodeWithTag("producer-plan-total").assertTextContains("389", substring = true)
        val screenshot = compose.onRoot().captureToImage().asAndroidBitmap()
        val destination = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
            .getExternalFilesDir(null)!!.resolve("producer-plan.png")
        destination.outputStream().use { screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        compose.onNodeWithContentDescription(increaseLabel).performClick()
        compose.onNodeWithContentDescription(decreaseLabel).assertIsEnabled()
        compose.onNodeWithTag("producer-plan-total").assertTextContains("778", substring = true)
        compose.onNodeWithText(cooperativeLabel).performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, cooperativeClicks) }
    }
}
