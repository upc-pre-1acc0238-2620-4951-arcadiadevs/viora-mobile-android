package pe.edu.upc.viora.core.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlin.reflect.KClass
import kotlinx.serialization.Serializable
import pe.edu.upc.viora.R

// Each tab owns a nested graph (…Graph) whose start destination is the tab root (…Route).
// A feature builds its graph with `navigation<XGraph>(startDestination = XRoute) { … }`
// and adds its detail routes inside it.

@Serializable
data object HomeGraph

@Serializable
data object HomeRoute

@Serializable
data object PlotsGraph

@Serializable
data object PlotsRoute

@Serializable
data object PlanGraph

@Serializable
data object PlanRoute

@Serializable
data object LogbookGraph

@Serializable
data object LogbookRoute

/** The four destinations of the producer's floating tab bar, in display order. */
enum class TopLevelDestination(
    val graph: Any,
    val startRoute: KClass<*>,
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
) {
    Home(HomeGraph, HomeRoute::class, R.drawable.ic_home, R.string.nav_home),
    Plots(PlotsGraph, PlotsRoute::class, R.drawable.ic_map, R.string.nav_plots),
    Plan(PlanGraph, PlanRoute::class, R.drawable.ic_calendar_month, R.string.nav_plan),
    Logbook(LogbookGraph, LogbookRoute::class, R.drawable.ic_menu_book, R.string.nav_logbook),
}
