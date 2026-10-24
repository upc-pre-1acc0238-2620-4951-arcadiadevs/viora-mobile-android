package pe.edu.upc.viora.features.subscription

import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import java.math.BigDecimal
import java.time.Instant
import java.util.Locale
import java.util.UUID
import org.junit.*
import org.junit.Assert.assertEquals
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.subscription.domain.entity.*
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.presentation.state.*
import pe.edu.upc.viora.features.subscription.presentation.ui.*

class IndividualCheckoutInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    @Test fun confirmedServerTotalRequiresConsentBeforeNavigationInEnglish() = exercise("en-US")
    @Test fun confirmedServerTotalRequiresConsentBeforeNavigationInSpanish() = exercise("es-PE")

    private fun exercise(tag: String) {
        val offer = ProducerPlanOffer(ProducerPlanCode("PRODUCER_ANNUAL"), "PEN", BigDecimal("55"), BigDecimal("0.18"),
            PlanCapacity.of(BigDecimal("0.5")), PlanCapacity.of(BigDecimal("0.5")), 12, "test", true)
        val content = ProducerPlanUiState.Content(listOf(offer), offer, offer.minimumQuota, Instant.EPOCH, false, false, null)
        val purchase = IndividualPurchase(UUID.randomUUID().toString(), offer.code, offer.minimumQuota, "PEN",
            BigDecimal("300.00"), BigDecimal("54.00"), BigDecimal("354.00"), "PENDING_PAYMENT")
        var state by mutableStateOf<IndividualCheckoutUiState>(IndividualCheckoutUiState.Idle)
        var payLabel = ""
        var continueLabel = ""
        var changedLabel = ""
        var opened = 0
        compose.setContent {
            val context = LocalContext.current
            val configuration = Configuration(LocalConfiguration.current).apply { setLocale(Locale.forLanguageTag(tag)) }
            val localized = context.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides configuration) {
                payLabel = stringResource(R.string.purchase_pay)
                continueLabel = stringResource(R.string.purchase_continue)
                changedLabel = stringResource(R.string.purchase_confirm_changed)
                VioraTheme {
                    ProducerPlanContent(content, {}, {}, {}, {}, {}, {}, checkoutAction = {
                        IndividualCheckoutAction(it, state, { state = IndividualCheckoutUiState.Confirm(purchase) },
                            { state = IndividualCheckoutUiState.Ready(IndividualCheckout(UUID.randomUUID().toString(),
                                "https://sandbox.mercadopago.com.pe/checkout/v1/redirect", Instant.MAX), purchase.id) },
                            { state = IndividualCheckoutUiState.Idle },
                            {},
                            { _, _ -> opened++; state = IndividualCheckoutUiState.Idle })
                    })
                }
            }
        }
        compose.onNodeWithText(payLabel).performScrollTo().performClick()
        compose.onNodeWithText(changedLabel).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("354", substring = true).performScrollTo().assertIsDisplayed()
        assertEquals(0, opened)
        compose.onNodeWithText(continueLabel).performClick()
        compose.waitForIdle()
        assertEquals(1, opened)
    }
}
