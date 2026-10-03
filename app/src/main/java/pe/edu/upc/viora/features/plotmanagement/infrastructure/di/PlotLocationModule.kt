package pe.edu.upc.viora.features.plotmanagement.infrastructure.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationTracker
import pe.edu.upc.viora.features.plotmanagement.infrastructure.location.FusedLocationTracker

@Module
@InstallIn(SingletonComponent::class)
abstract class PlotLocationModule {

    @Binds
    abstract fun bindLocationTracker(impl: FusedLocationTracker): LocationTracker

    companion object {
        @Provides
        @Singleton
        fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
    }
}
