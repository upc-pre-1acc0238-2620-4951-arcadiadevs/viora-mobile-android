package pe.edu.upc.viora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pe.edu.upc.viora.core.navigation.AppNavHost
import pe.edu.upc.viora.core.theme.VioraTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VioraTheme {
                VioraApp()
            }
        }
    }
}

@Composable
fun VioraApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        AppNavHost(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@Preview(showBackground = true)
@Composable
fun VioraAppPreview() {
    VioraTheme {
        VioraApp()
    }
}
