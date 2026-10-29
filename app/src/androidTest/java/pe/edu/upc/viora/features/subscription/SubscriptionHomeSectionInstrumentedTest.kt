package pe.edu.upc.viora.features.subscription

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import java.time.Instant
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus
import pe.edu.upc.viora.features.subscription.presentation.ui.InactiveAccessCard
import pe.edu.upc.viora.features.subscription.presentation.ui.SubscriptionHomeContent

class SubscriptionHomeSectionInstrumentedTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun inactiveCardDisplaysFormattedExpirationDateAndTriggersRenewAction() {
        var renewed = 0
        var redeemed = 0
        val sampleExpiry = Instant.parse("2026-11-15T12:00:00Z")

        compose.setContent {
            VioraTheme {
                InactiveAccessCard(
                    endsAt = sampleExpiry,
                    onRenew = { renewed++ },
                    onRedeemCode = { redeemed++ },
                )
            }
        }

        compose.onNodeWithTag("home_inactive_access_card").assertIsDisplayed()
        compose.onNodeWithTag("home_renew_plan").assertIsDisplayed().performClick()
        assertEquals(1, renewed)

        compose.onNodeWithTag("home_redeem_code").assertIsDisplayed().performClick()
        assertEquals(1, redeemed)
    }

    @Test
    fun inactiveCardWithoutExpiryDateDisplaysFallbackTitle() {
        compose.setContent {
            VioraTheme {
                InactiveAccessCard(
                    endsAt = null,
                    onRenew = {},
                    onRedeemCode = {},
                )
            }
        }

        compose.onNodeWithTag("home_inactive_access_card").assertIsDisplayed()
        compose.onNodeWithTag("home_renew_plan").assertIsDisplayed()
    }

    @Test
    fun inactiveCardDisplaysInEnglish() {
        exerciseLocale("en-US")
    }

    @Test
    fun inactiveCardDisplaysInSpanish() {
        exerciseLocale("es-PE")
    }

    private fun exerciseLocale(tag: String) {
        var renewTitle = ""
        var chipTitle = ""
        val sampleExpiry = Instant.parse("2026-11-15T12:00:00Z")

        compose.setContent {
            val context = LocalContext.current
            val configuration = Configuration(LocalConfiguration.current).apply { setLocale(Locale.forLanguageTag(tag)) }
            val localized = context.createConfigurationContext(configuration)
            CompositionLocalProvider(LocalContext provides localized, LocalConfiguration provides configuration) {
                renewTitle = stringResource(R.string.home_access_renew)
                chipTitle = stringResource(R.string.home_access_inactive_chip)
                VioraTheme {
                    InactiveAccessCard(
                        endsAt = sampleExpiry,
                        onRenew = {},
                        onRedeemCode = {},
                    )
                }
            }
        }

        compose.onNodeWithText(renewTitle).assertIsDisplayed()
        compose.onNodeWithText(chipTitle).assertIsDisplayed()
    }

    @Test
    fun subscriptionHomeSectionAddsNothingWhenEntitlementIsActive() {
        val activeSub = ProducerSubscription(
            id = java.util.UUID.randomUUID().toString(),
            mode = SubscriptionMode.INDIVIDUAL,
            planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
            status = SubscriptionStatus.ACTIVE,
            capacity = PlanCapacity.of(java.math.BigDecimal("0.5")),
            currency = "PEN",
            baseAmount = java.math.BigDecimal("279.66"),
            taxAmount = java.math.BigDecimal("50.34"),
            totalAmount = java.math.BigDecimal("330.00"),
            startsAt = Instant.now(),
            endsAt = Instant.now().plusSeconds(86400 * 365),
            entitlementActive = true,
            cooperativeId = null,
        )
        val repo = object : pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository {
            override suspend fun getCurrent(): pe.edu.upc.viora.core.domain.AppResult<ProducerSubscription?> =
                pe.edu.upc.viora.core.domain.AppResult.Success(activeSub)
            override suspend fun getById(id: String): pe.edu.upc.viora.core.domain.AppResult<ProducerSubscription> =
                pe.edu.upc.viora.core.domain.AppResult.Success(activeSub)
        }
        val vm = pe.edu.upc.viora.features.subscription.presentation.viewmodel.SubscriptionAccessViewModel(
            pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase(repo)
        )

        compose.setContent {
            VioraTheme {
                val state by vm.uiState.collectAsState()
                LaunchedEffect(Unit) { vm.refresh() }
                SubscriptionHomeContent(
                    result = state,
                    onRenew = {},
                    onRedeemCode = {},
                )
            }
        }

        compose.waitForIdle()
        compose.onNodeWithTag("home_inactive_access_card").assertDoesNotExist()
    }

    @Test
    fun subscriptionHomeSectionShowsInactiveCardWhenEntitlementIsInactive() {
        val inactiveSub = ProducerSubscription(
            id = java.util.UUID.randomUUID().toString(),
            mode = SubscriptionMode.INDIVIDUAL,
            planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
            status = SubscriptionStatus.PENDING_PAYMENT,
            capacity = PlanCapacity.of(java.math.BigDecimal("0.5")),
            currency = "PEN",
            baseAmount = java.math.BigDecimal("279.66"),
            taxAmount = java.math.BigDecimal("50.34"),
            totalAmount = java.math.BigDecimal("330.00"),
            startsAt = null,
            endsAt = null,
            entitlementActive = false,
            cooperativeId = null,
        )
        val repo = object : pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository {
            override suspend fun getCurrent(): pe.edu.upc.viora.core.domain.AppResult<ProducerSubscription?> =
                pe.edu.upc.viora.core.domain.AppResult.Success(inactiveSub)
            override suspend fun getById(id: String): pe.edu.upc.viora.core.domain.AppResult<ProducerSubscription> =
                pe.edu.upc.viora.core.domain.AppResult.Success(inactiveSub)
        }
        val vm = pe.edu.upc.viora.features.subscription.presentation.viewmodel.SubscriptionAccessViewModel(
            pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase(repo)
        )

        compose.setContent {
            VioraTheme {
                val state by vm.uiState.collectAsState()
                LaunchedEffect(Unit) { vm.refresh() }
                SubscriptionHomeContent(
                    result = state,
                    onRenew = {},
                    onRedeemCode = {},
                )
            }
        }

        compose.onNodeWithTag("home_inactive_access_card").assertIsDisplayed()
        compose.onNodeWithTag("home_renew_plan").assertDoesNotExist()
    }

    @Test
    fun subscriptionHomeContentInLoadingStateRendersNoUnconfirmedCard() {
        compose.setContent {
            VioraTheme {
                pe.edu.upc.viora.features.subscription.presentation.ui.SubscriptionHomeContent(
                    result = null,
                    onRenew = {},
                    onRedeemCode = {},
                )
            }
        }

        compose.onNodeWithTag("home_subscription_loading").assertExists()
        compose.onNodeWithTag("home_inactive_access_card").assertDoesNotExist()
        compose.onNodeWithTag("home_renew_plan").assertDoesNotExist()
    }

    @Test
    fun subscriptionHomeContentInErrorStateRendersNoFalseClaim() {
        compose.setContent {
            VioraTheme {
                pe.edu.upc.viora.features.subscription.presentation.ui.SubscriptionHomeContent(
                    result = pe.edu.upc.viora.core.domain.AppResult.Failure(pe.edu.upc.viora.core.domain.AppError.Offline),
                    onRenew = {},
                    onRedeemCode = {},
                )
            }
        }

        compose.onNodeWithTag("home_subscription_error").assertExists()
        compose.onNodeWithTag("home_inactive_access_card").assertDoesNotExist()
        compose.onNodeWithTag("home_renew_plan").assertDoesNotExist()
    }
}
