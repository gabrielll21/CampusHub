package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.auth.PasswordResetError

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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CampusHub",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Recuperar senha",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Informe seu e-mail e enviaremos um link para redefinir sua senha.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    validationError = null
                    feedbackMessage = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("E-mail") },
                placeholder = { Text("seuemail@universidade.edu.br") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = validationError != null,
                enabled = !isSending,
                supportingText = validationError?.let { message ->
                    {
                        Text(
                            text = message,
                            modifier = Modifier.semantics {
                                liveRegion = LiveRegionMode.Assertive
                            }
                        )
                    }
                },
                singleLine = true
            )

            feedbackMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    modifier = Modifier.semantics {
                        liveRegion = if (feedbackIsError) {
                            LiveRegionMode.Assertive
                        } else {
                            LiveRegionMode.Polite
                        }
                    },
                    color = if (feedbackIsError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
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
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSending
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Enviar link")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSending
            ) {
                Text("Voltar")
            }
        }
    }
}

private fun PasswordResetError.toUserMessage(): String = when (this) {
    PasswordResetError.INVALID_EMAIL -> "Digite um e-mail válido."
    PasswordResetError.USER_NOT_FOUND -> "Não encontramos uma conta com esse e-mail."
    PasswordResetError.UNKNOWN -> "Não foi possível enviar o link. Tente novamente."
}
