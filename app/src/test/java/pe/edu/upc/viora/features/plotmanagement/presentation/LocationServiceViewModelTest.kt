package pe.edu.upc.viora.features.plotmanagement.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveGpsFixesUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationService
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationTracker
import pe.edu.upc.viora.features.plotmanagement.presentation.state.GpsReading
import pe.edu.upc.viora.features.plotmanagement.presentation.state.MarkingMethod
import pe.edu.upc.viora.features.plotmanagement.presentation.state.availableMethod
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.GpsTrackingViewModel
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.LocationServiceViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class LocationServiceViewModelTest {
    private class FakeService(initial: Boolean) : LocationService {
        val updates = MutableStateFlow(initial)
        var current = initial
        override fun isEnabled() = current
        override fun observeEnabled() = updates
    }

    @Before fun setup() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `service changes and resume refresh update the state`() {
        val service = FakeService(true)
        val vm = LocationServiceViewModel(service)
        assertEquals(true, vm.enabled.value)
        service.updates.value = false
        assertEquals(false, vm.enabled.value)
        service.current = true
        vm.refresh()
        assertEquals(true, vm.enabled.value)
    }

    @Test fun `tracking starts afresh when location returns`() = runTest {
        val service = FakeService(false)
        var starts = 0
        val tracker = object : LocationTracker {
            override fun observeFixes() = kotlinx.coroutines.flow.flow<pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix> { starts++; kotlinx.coroutines.awaitCancellation() }
        }
        val vm = GpsTrackingViewModel(ObserveGpsFixesUseCase(tracker), service)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.reading.collect {} }
        assertEquals(0, starts)
        service.updates.value = true
        assertEquals(1, starts)
        assertEquals(GpsReading.Searching, vm.reading.value)
        service.updates.value = false
        service.updates.value = true
        assertEquals(2, starts)
        assertEquals(GpsReading.Searching, vm.reading.value)
    }

    @Test fun `gps requires both permission and service and map stays available`() {
        for (permission in listOf(false, true)) for (enabled in listOf(false, true)) {
            val expected = if (permission && enabled) MarkingMethod.GPS_WALK else MarkingMethod.MAP_TRACE
            assertEquals(expected, availableMethod(MarkingMethod.GPS_WALK, permission, enabled))
            assertEquals(MarkingMethod.MAP_TRACE, availableMethod(MarkingMethod.MAP_TRACE, permission, enabled))
            assertEquals(expected, availableMethod(null, permission, enabled))
        }
    }
}
