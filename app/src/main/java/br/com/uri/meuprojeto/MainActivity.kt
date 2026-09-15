package br.com.uri.meuprojeto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import br.com.uri.meuprojeto.ui.RegistrationScreen
import br.com.uri.meuprojeto.ui.theme.MeuProjetoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeuProjetoTheme {
                RegistrationScreen(
                    onRegister = { _, _, _ -> },
                    onBack = {}
                )
            }
        }
    }
}