package br.com.fiap.solidarizeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import br.com.fiap.solidarizeapp.navigation.SolidariNavGraph
import br.com.fiap.solidarizeapp.ui.theme.SolidarizeAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SolidarizeAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    SolidariNavGraph()
                }
            }
        }
    }
}

