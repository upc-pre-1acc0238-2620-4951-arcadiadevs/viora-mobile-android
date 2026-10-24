package pe.edu.upc.viora.features.subscription

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import java.io.File
import java.math.BigDecimal
import java.time.Instant
import java.util.Locale
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.presentation.state.IndividualCheckoutUiState
import pe.edu.upc.viora.features.subscription.presentation.ui.PaymentResultScreen

class PaymentResultScreenInstrumentedTest {
    @get:Rule val compose = createComposeRule()
    private var retry = 0
    private var redeem = 0
    private var register = 0
    private var home = 0
    private var checks = 0

    private fun render(state: IndividualCheckoutUiState, locale: String = "es-PE") {
        compose.setContent {
            val config = Configuration(LocalConfiguration.current).apply { setLocale(Locale.forLanguageTag(locale)) }
            val context = LocalContext.current.createConfigurationContext(config)
            CompositionLocalProvider(LocalConfiguration provides config, LocalContext provides context) {
                VioraTheme {
                    PaymentResultScreen(state, "Teodoro", { checks++ }, { retry++ }, { redeem++ }, { register++ }, { home++ })
                }
            }
        }
        compose.onNodeWithTag("payment-result").assertIsDisplayed()
    }

    @Test fun verifyingIsFullScreenWithoutPurchaseActions() {
        render(IndividualCheckoutUiState.Verifying(UUID.randomUUID().toString()))
        compose.onNodeWithText("Confirmando").assertIsDisplayed()
        compose.onNodeWithText("tu pago…").assertIsDisplayed()
        compose.onNodeWithText("Pagar con Mercado Pago").assertDoesNotExist()
        capture("p04-verificando-es.png")
    }

    @Test fun rejectionOffersWorkingRetryAndCooperativeActions() {
        render(IndividualCheckoutUiState.Rejected(UUID.randomUUID().toString()))
        compose.onNodeWithText("El pago no").assertIsDisplayed()
        capture("p04-rechazado-es.png")
        compose.onNodeWithText("Intentar con otro medio").performClick()
        compose.onNodeWithText("Tengo un código de cooperativa").performClick()
        assertEquals(1, retry)
        assertEquals(1, redeem)
    }

    @Test fun approvalShowsServerTermsAndBothDestinations() {
        render(IndividualCheckoutUiState.Confirmed(subscription()))
        compose.onNodeWithText("Teodoro.").assertIsDisplayed()
        compose.onNodeWithText("Productor · 12 ha").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Enviado a tu correo").assertDoesNotExist()
        capture("p04-aprobado-es.png")
        compose.onNodeWithText("Registrar mi primer lote").performClick()
        compose.onNodeWithText("Ir al inicio").performClick()
        assertEquals(1, register)
        assertEquals(1, home)
    }

    @Test fun unconfirmedPaymentDoesNotClaimRejectionAndAllowsRecheck() {
        render(IndividualCheckoutUiState.Unconfirmed(UUID.randomUUID().toString()))
        compose.onNodeWithText("PAGO NO COMPLETADO").assertDoesNotExist()
        compose.onNodeWithText("Consultar estado").performClick()
        assertEquals(1, checks)
    }

    @Test fun approvedScreenIsLocalizedInEnglish() {
        render(IndividualCheckoutUiState.Confirmed(subscription()), "en-US")
        compose.onNodeWithText("Welcome to Viora,").assertIsDisplayed()
        compose.onNodeWithText("Producer · 12 ha").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Go to home").assertIsDisplayed()
        compose.onNodeWithText("Register my first plot").assertIsDisplayed()
    }

    private fun subscription() = ProducerSubscription(UUID.randomUUID().toString(), SubscriptionMode.INDIVIDUAL,
        ProducerPlanCode("PRODUCER_ANNUAL"), SubscriptionStatus.ACTIVE, PlanCapacity.of(BigDecimal("12")),
        "PEN", BigDecimal("6711.86"), BigDecimal("1208.14"), BigDecimal("7920.00"),
        Instant.parse("2026-09-26T12:00:00Z"), Instant.parse("2027-09-26T12:00:00Z"), true, null)

    private fun capture(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val context = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext
        File(context.cacheDir, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
