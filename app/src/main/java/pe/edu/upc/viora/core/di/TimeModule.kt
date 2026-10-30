package pe.edu.upc.viora.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.ZoneId

/** Injectable clock and zone so time-dependent code can be tested with a fixed instant. */
@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()

    /** The zone dates are shown in; screens read it through LocalAppZone. */
    @Provides
    fun provideZoneId(): ZoneId = ZoneId.systemDefault()
}
