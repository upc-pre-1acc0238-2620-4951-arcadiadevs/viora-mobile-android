package pe.edu.upc.viora.features.subscription.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import java.math.BigDecimal
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.application.usecase.*
import pe.edu.upc.viora.features.subscription.domain.entity.*
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerPlanRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.presentation.state.ProducerPlanUiState
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.ProducerPlanViewModel

private class FakeCatalogRepository : ProducerPlanRepository {
    val catalog = MutableStateFlow(ProducerPlanCatalog(emptyList(), null))
    var refreshCount = 0
    var response: suspend () -> AppResult<Unit> = { AppResult.Failure(AppError.Offline) }
    override fun observeCatalog() = catalog
    override suspend fun refresh(): AppResult<Unit> { refreshCount++; return response() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProducerPlanViewModelTest {
    private val repository = FakeCatalogRepository()
    private val stores = mutableListOf<ViewModelStore>()
    private val fetchedAt = Instant.parse("2026-10-21T12:00:00Z")
    private fun offer(step: String = "0.5", code: String = "PRODUCER_ANNUAL") = ProducerPlanOffer(
        ProducerPlanCode(code), "PEN", BigDecimal("55"), BigDecimal("0.18"),
        PlanCapacity.of(BigDecimal(step)), PlanCapacity.of(BigDecimal(step)), 12, "fixture-1")

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { stores.forEach { it.clear() }; Dispatchers.resetMain() }

    private fun model(saved: SavedStateHandle = SavedStateHandle()): ProducerPlanViewModel {
        val model = ProducerPlanViewModel(ObserveProducerPlanCatalogUseCase(repository),
            RefreshProducerPlanCatalogUseCase(repository), saved)
        ViewModelStore().also { it.put("plan", model); stores += it }
        return model
    }

    private fun cache(vararg offers: ProducerPlanOffer = arrayOf(offer())) {
        repository.catalog.value = ProducerPlanCatalog(offers.toList(), fetchedAt)
    }

    @Test fun `offline without cache offers retry instead of a fabricated price`() {
        assertEquals(ProducerPlanUiState.Failed(AppError.Offline), model().uiState.value)
    }

    @Test fun `saved policies remain visible with their timestamp when refresh fails`() {
        cache()
        val state = model().uiState.value as ProducerPlanUiState.Content
        assertTrue(state.isSavedCopy)
        assertEquals(fetchedAt, state.fetchedAt)
        assertEquals(AppError.Offline, state.error)
        assertEquals(BigDecimal("389.40"), state.estimate.total)
    }

    @Test fun `selection starts at the server minimum and never decreases below it`() {
        cache(); val model = model()
        model.decrease()
        assertFalse((model.uiState.value as ProducerPlanUiState.Content).canDecrease)
        model.increase(); model.increase(); model.decrease()
        assertEquals(BigDecimal.ONE, (model.uiState.value as ProducerPlanUiState.Content).capacity.hectares)
    }

    @Test fun `selection is restored after view model recreation`() {
        cache(); val saved = SavedStateHandle(); val first = model(saved)
        first.increase(); first.increase()
        val restored = model(SavedStateHandle(mapOf("producer_plan_code" to saved.get<String>("producer_plan_code"),
            "producer_plan_quota_ha" to saved.get<String>("producer_plan_quota_ha"))))
        assertEquals(BigDecimal("1.5"), (restored.uiState.value as ProducerPlanUiState.Content).capacity.hectares)
    }

    @Test fun `an updated catalog revalidates a saved capacity against the new server step`() {
        cache(); val model = model(); model.increase(); model.increase()
        cache(offer("1"))
        assertEquals(BigDecimal.ONE, (model.uiState.value as ProducerPlanUiState.Content).capacity.hectares)
    }

    @Test fun `switching offers starts from that offers minimum`() {
        cache(offer(), offer("2", "OTHER")); val model = model()
        model.selectOffer("OTHER")
        assertEquals(BigDecimal("2"), (model.uiState.value as ProducerPlanUiState.Content).capacity.hectares)
        model.selectOffer("UNKNOWN")
        assertEquals("OTHER", (model.uiState.value as ProducerPlanUiState.Content).selectedOffer.code.value)
    }

    @Test fun `empty successful catalog has an explicit empty state`() {
        repository.catalog.value = ProducerPlanCatalog(emptyList(), fetchedAt)
        repository.response = { AppResult.Success(Unit) }
        assertTrue(model().uiState.value is ProducerPlanUiState.Empty)
    }

    @Test fun `refresh taps are coalesced and a successful response replaces the cached label`() = runTest {
        val response = CompletableDeferred<AppResult<Unit>>()
        repository.response = { response.await() }
        val model = model()
        assertEquals(ProducerPlanUiState.Loading, model.uiState.value)
        model.refresh(); model.refresh()
        assertEquals(1, repository.refreshCount)
        cache(); response.complete(AppResult.Success(Unit))
        val state = model.uiState.value as ProducerPlanUiState.Content
        assertFalse(state.isRefreshing)
        assertFalse(state.isSavedCopy)
        assertNull(state.error)
    }
}
