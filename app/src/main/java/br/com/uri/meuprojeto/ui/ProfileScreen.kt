package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(
    uid: String,
    name: String,
    email: String,
    onSaveName: (
        uid: String,
        name: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedName by rememberSaveable(name) { mutableStateOf(name) }
    var editedName by rememberSaveable(name) { mutableStateOf(name) }
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var feedbackMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) }

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
                .widthIn(max = 600.dp)
        ) {
            Text(
                text = "CampusHub",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Meu Perfil",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Confira as informações da sua conta.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(28.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedName,
                            onValueChange = {
                                editedName = it
                                feedbackMessage = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Nome") },
                            enabled = !isSaving,
                            singleLine = true
                        )
                    } else {
                        ProfileInformation(
                            label = "Nome",
                            value = displayedName
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    ProfileInformation(label = "E-mail", value = email)
                }
            }

            feedbackMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    modifier = Modifier.semantics {
                        liveRegion = if (isError) {
                            LiveRegionMode.Assertive
                        } else {
                            LiveRegionMode.Polite
                        }
                    },
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (isEditing) {
                Button(
                    onClick = {
                        val normalizedName = editedName.trim()
                        if (normalizedName.isEmpty()) {
                            isError = true
                            feedbackMessage = "O nome não pode ficar vazio."
                        } else {
                            isSaving = true
                            feedbackMessage = null

                            onSaveName(
                                uid,
                                normalizedName,
                                {
                                    displayedName = normalizedName
                                    editedName = normalizedName
                                    isSaving = false
                                    isEditing = false
                                    isError = false
                                    feedbackMessage = "Nome atualizado com sucesso!"
                                },
                                {
                                    isSaving = false
                                    isError = true
                                    feedbackMessage =
                                        "Não foi possível atualizar o nome. Tente novamente."
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Salvar")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        editedName = displayedName
                        isEditing = false
                        feedbackMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) {
                    Text("Cancelar")
                }
            } else {
                Button(
                    onClick = {
                        editedName = displayedName
                        isEditing = true
                        feedbackMessage = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Editar perfil")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text("Voltar")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text("Sair")
            }
        }
    }
}

@Composable
private fun ProfileInformation(
    label: String,
    value: String
) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelLarge
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge
    )
}
