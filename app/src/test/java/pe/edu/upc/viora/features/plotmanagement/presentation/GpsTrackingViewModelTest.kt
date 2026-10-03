package pe.edu.upc.viora.features.plotmanagement.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveGpsFixesUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationTracker
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix
import pe.edu.upc.viora.features.plotmanagement.presentation.state.GpsReading
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.GpsTrackingViewModel

private class ScriptedTracker(private val fixes: Flow<GpsFix>) : LocationTracker {
    override fun observeFixes(): Flow<GpsFix> = fixes
}

@OptIn(ExperimentalCoroutinesApi::class)
class GpsTrackingViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fix(accuracy: Double) = GpsFix(GeoPoint(-18.0958, -70.3167), accuracy)

    private fun viewModel(fixes: Flow<GpsFix>) = GpsTrackingViewModel(ObserveGpsFixesUseCase(ScriptedTracker(fixes)))

    @Test
    fun `it searches until the first fix arrives`() = runTest {
        val channel = Channel<GpsFix>(Channel.UNLIMITED)
        val vm = viewModel(channel.consumeAsFlow())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.reading.collect {} }

        assertEquals(GpsReading.Searching, vm.reading.value)

        channel.send(fix(4.0))

        assertEquals(GpsReading.Located(fix(4.0)), vm.reading.value)
    }

    @Test
    fun `a fix more imprecise than thirty metres is a lost signal that remembers where it was`() = runTest {
        val channel = Channel<GpsFix>(Channel.UNLIMITED)
        val vm = viewModel(channel.consumeAsFlow())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.reading.collect {} }

        channel.send(fix(40.0))

        assertEquals(GpsReading.Lost(fix(40.0)), vm.reading.value)
    }

    @Test
    fun `a weak fix is still a position, only too imprecise to mark a corner`() = runTest {
        val channel = Channel<GpsFix>(Channel.UNLIMITED)
        val vm = viewModel(channel.consumeAsFlow())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.reading.collect {} }

        channel.send(fix(20.0))

        val reading = vm.reading.value as GpsReading.Located
        assertTrue(!reading.fix.signal.allowsMarking)
    }

    @Test
    fun `the signal is lost when no fix comes for ten seconds and comes back with the next one`() = runTest {
        val channel = Channel<GpsFix>(Channel.UNLIMITED)
        val vm = viewModel(channel.consumeAsFlow())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.reading.collect {} }
        channel.send(fix(4.0))

        advanceTimeBy(GpsTrackingViewModel.SIGNAL_TIMEOUT_MS + 1)

        assertEquals(GpsReading.Lost(fix(4.0)), vm.reading.value)

        channel.send(fix(6.0))

        assertEquals(GpsReading.Located(fix(6.0)), vm.reading.value)
    }

    @Test
    fun `the first fix never arriving is also a lost signal`() = runTest {
        val vm = viewModel(flow { })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.reading.collect {} }

        advanceTimeBy(GpsTrackingViewModel.SIGNAL_TIMEOUT_MS + 1)

        assertEquals(GpsReading.Lost(null), vm.reading.value)
    }

    @Test
    fun `a GPS that fails, for example without the permission, is a lost signal`() = runTest {
        val vm = viewModel(flow { throw SecurityException("no permission") })
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.reading.collect {} }

        assertEquals(GpsReading.Lost(null), vm.reading.value)
    }
}
