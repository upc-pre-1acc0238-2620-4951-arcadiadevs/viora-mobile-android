package pe.edu.upc.viora.features.subscription

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.home.presentation.ui.HomeContextChips
import pe.edu.upc.viora.core.designsystem.component.VioraGreetingHeader
import pe.edu.upc.viora.core.designsystem.component.VioraHeadline
import pe.edu.upc.viora.features.subscription.presentation.ui.InactiveAccessCard

class HomeScreenVisualTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun visualHomeScreenWithExpirationDate() {
        val sampleExpiry = Instant.parse("2026-11-15T12:00:00Z")
        compose.setContent {
            val context = androidx.compose.ui.platform.LocalContext.current
            val configuration = android.content.res.Configuration(androidx.compose.ui.platform.LocalConfiguration.current).apply {
                setLocale(java.util.Locale.forLanguageTag("es-PE"))
            }
            val localized = context.createConfigurationContext(configuration)
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalContext provides localized,
                androidx.compose.ui.platform.LocalConfiguration provides configuration,
            ) {
                VioraTheme {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(top = 40.dp, bottom = 40.dp)
                    ) {
                        VioraGreetingHeader(
                            date = LocalDate.of(2026, 11, 18),
                            isOffline = false,
                            lastRefresh = Instant.now(),
                            displayName = "Teodoro",
                            hasUnreadAlerts = true,
                            onOpenAlerts = {},
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        Spacer(Modifier.height(28.dp))
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            VioraHeadline(
                                lead = stringResource(R.string.home_headline_paused_lead),
                                emphasis = stringResource(R.string.home_headline_paused_emphasis),
                            )
                            HomeContextChips(
                                campaignYear = 2026,
                                plotCount = 2,
                                totalHectares = 4.0,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    InactiveAccessCard(
                        endsAt = sampleExpiry,
                        onRenew = {},
                        onRedeemCode = {},
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp),
                    )
                }
            }
        }

        }
        compose.onNodeWithText("Tu acceso").assertIsDisplayed()
        compose.onNodeWithText("está en pausa.").assertIsDisplayed()
        compose.onNodeWithTag("home_inactive_access_card").assertIsDisplayed()
        compose.onNodeWithText("Tu plan venció el\n15 de noviembre").assertIsDisplayed()

        saveScreenshot("p10_with_expiration_date.png")
    }

    @Test
    fun visualHomeScreenWithoutExpirationDate() {
        compose.setContent {
            VioraTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(top = 40.dp, bottom = 40.dp)
                ) {
                    VioraGreetingHeader(
                        date = LocalDate.of(2026, 11, 18),
                        isOffline = false,
                        lastRefresh = Instant.now(),
                        displayName = "Teodoro",
                        hasUnreadAlerts = true,
                        onOpenAlerts = {},
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Spacer(Modifier.height(28.dp))
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        VioraHeadline(
                            lead = stringResource(R.string.home_headline_paused_lead),
                            emphasis = stringResource(R.string.home_headline_paused_emphasis),
                        )
                        HomeContextChips(
                            campaignYear = 2026,
                            plotCount = 2,
                            totalHectares = 4.0,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                    InactiveAccessCard(
                        endsAt = null,
                        onRenew = {},
                        onRedeemCode = {},
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp),
                    )
                }
            }
        }

        compose.onNodeWithTag("home_inactive_access_card").assertIsDisplayed()

        saveScreenshot("p10_without_expiration_date.png")
    }

    private fun saveScreenshot(filename: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val dest = File(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, filename)
        FileOutputStream(dest).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}
