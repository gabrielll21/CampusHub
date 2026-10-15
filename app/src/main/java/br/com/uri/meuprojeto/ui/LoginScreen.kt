package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.ui.components.CampusHubButton
import br.com.uri.meuprojeto.ui.components.CampusHubFeedback
import br.com.uri.meuprojeto.ui.components.CampusHubOutlinedButton
import br.com.uri.meuprojeto.ui.components.CampusHubTextField

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: (String) -> Unit,
    isLoading: Boolean = false,
    feedbackMessage: String? = null,
    isSuccess: Boolean = false,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CampusHub",
                modifier = Modifier.semantics { heading() },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Entre para acompanhar a vida no campus.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Entrar",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(20.dp))

            CampusHubTextField(
                value = email,
                onValueChange = { email = it },
                label = "E-mail",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            CampusHubTextField(
                value = password,
                onValueChange = { password = it },
                label = "Senha",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(
                onClick = { onForgotPassword(email) },
                modifier = Modifier.align(Alignment.End),
                enabled = !isLoading
            ) {
                Text("Esqueci minha senha")
            }
            Spacer(modifier = Modifier.height(8.dp))
            CampusHubButton(
                text = "Entrar",
                onClick = { onLogin(email, password) },
                enabled = !isLoading,
                isLoading = isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            feedbackMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                CampusHubFeedback(
                    message = message,
                    isError = !isSuccess,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            CampusHubOutlinedButton(
                text = "Criar conta",
                onClick = onCreateAccount,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
