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
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.auth.PasswordResetError
import br.com.uri.meuprojeto.ui.components.CampusHubButton
import br.com.uri.meuprojeto.ui.components.CampusHubFeedback
import br.com.uri.meuprojeto.ui.components.CampusHubOutlinedButton
import br.com.uri.meuprojeto.ui.components.CampusHubTextField

@Composable
fun ForgotPasswordScreen(
    initialEmail: String = "",
    onSendResetEmail: (
        email: String,
        onSuccess: () -> Unit,
        onError: (PasswordResetError) -> Unit
    ) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable(initialEmail) { mutableStateOf(initialEmail) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var feedbackMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var feedbackIsError by rememberSaveable { mutableStateOf(false) }
    var isSending by rememberSaveable { mutableStateOf(false) }

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
                text = "Recupere o acesso à sua conta.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Recuperar senha",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Informe seu e-mail e enviaremos um link para redefinir sua senha.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(24.dp))

            CampusHubTextField(
                value = email,
                onValueChange = {
                    email = it
                    validationError = null
                    feedbackMessage = null
                },
                label = "E-mail",
                placeholder = "seuemail@universidade.edu.br",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                isError = validationError != null,
                supportingText = validationError,
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth()
            )

            feedbackMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                CampusHubFeedback(
                    message = message,
                    isError = feedbackIsError,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            CampusHubButton(
                text = "Enviar link",
                onClick = {
                    val normalizedEmail = email.trim()
                    if (normalizedEmail.isEmpty()) {
                        validationError = "Digite seu e-mail."
                    } else {
                        isSending = true
                        feedbackMessage = null
                        onSendResetEmail(
                            normalizedEmail,
                            {
                                isSending = false
                                feedbackIsError = false
                                feedbackMessage =
                                    "Link de recuperação enviado. Verifique seu e-mail."
                            },
                            { error ->
                                isSending = false
                                feedbackIsError = true
                                feedbackMessage = error.toUserMessage()
                            }
                        )
                    }
                },
                enabled = !isSending,
                isLoading = isSending,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            CampusHubOutlinedButton(
                text = "Voltar",
                onClick = onBack,
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun PasswordResetError.toUserMessage(): String = when (this) {
    PasswordResetError.INVALID_EMAIL -> "Digite um e-mail válido."
    PasswordResetError.USER_NOT_FOUND -> "Não encontramos uma conta com esse e-mail."
    PasswordResetError.UNKNOWN -> "Não foi possível enviar o link. Tente novamente."
}
