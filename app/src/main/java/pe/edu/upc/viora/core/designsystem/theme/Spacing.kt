package pe.edu.upc.viora.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Closed spacing scale (multiples of 4 dp). If a value is not here, do not use it.
 * Related items: [xs]-[sm]; separate groups: [md]-[lg]; sections: [lg]-[xl].
 */
object Spacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp

    /** Minimum interactive area: field use with gloves or under direct sun. */
    val minTouchTarget = 48.dp
}
