package pe.edu.upc.viora.features.subscription.infrastructure

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.subscription.infrastructure.local.PlanExtensionAccountDataWiper

class PlanExtensionAccountDataWiperTest {

    @Test
    fun `an extension left to pay does not follow the producer to another account`() = runTest {
        val preferences = InMemoryPreferences()
        val pendingSubscription = stringPreferencesKey("plan_extension_pending_subscription")
        val pendingExtension = stringPreferencesKey("plan_extension_pending_id")
        preferences.edit {
            it[pendingSubscription] = "sub-1"
            it[pendingExtension] = "ext-1"
        }

        PlanExtensionAccountDataWiper(preferences).wipe()

        assertNull(preferences.data.first()[pendingSubscription])
        assertNull(preferences.data.first()[pendingExtension])
    }
}
