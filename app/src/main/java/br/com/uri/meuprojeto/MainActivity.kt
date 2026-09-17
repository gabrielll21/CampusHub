package br.com.uri.meuprojeto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import br.com.uri.meuprojeto.auth.AuthRepository
import br.com.uri.meuprojeto.navigation.AppNavigation
import br.com.uri.meuprojeto.ui.theme.MeuProjetoTheme
import br.com.uri.meuprojeto.user.UserRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MeuProjetoTheme {
                val authRepository = remember { AuthRepository() }
                val userRepository = remember { UserRepository() }

                AppNavigation(
                    authRepository = authRepository,
                    userRepository = userRepository
                )
            }
        }
    }
}
