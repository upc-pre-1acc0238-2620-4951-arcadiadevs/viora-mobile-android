package pe.edu.upc.viora.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upc.viora.core.theme.VioraTheme

/** Minimal placeholder start destination so the app launches before any feature exists. */
@Composable
fun PlaceholderScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Viora")
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    VioraTheme {
        PlaceholderScreen()
    }
}
