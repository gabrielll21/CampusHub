package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.ui.components.CampusHubButton
import br.com.uri.meuprojeto.ui.components.CampusHubCard
import br.com.uri.meuprojeto.ui.components.CampusHubFeedback
import br.com.uri.meuprojeto.ui.components.CampusHubOutlinedButton
import br.com.uri.meuprojeto.ui.components.CampusHubSectionHeader
import br.com.uri.meuprojeto.ui.components.CampusHubTextField

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Voltar"
                        }
                    ) {
                        Text("Voltar")
                    }
                }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(contentPadding)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .align(Alignment.TopCenter)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Minha conta",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confira e atualize suas informações pessoais.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))

                CampusHubSectionHeader(
                    title = if (isEditing) "Editar perfil" else "Informações da conta"
                )
                Spacer(modifier = Modifier.height(12.dp))

                CampusHubCard(modifier = Modifier.fillMaxWidth()) {
                    if (isEditing) {
                        CampusHubTextField(
                            value = editedName,
                            onValueChange = {
                                editedName = it
                                feedbackMessage = null
                            },
                            label = "Nome",
                            enabled = !isSaving,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        ProfileInformation(label = "Nome", value = displayedName)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    ProfileInformation(label = "E-mail", value = email)
                }

                feedbackMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    CampusHubFeedback(
                        message = message,
                        isError = isError,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                if (isEditing) {
                    CampusHubButton(
                        text = "Salvar",
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
                        enabled = !isSaving,
                        isLoading = isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CampusHubOutlinedButton(
                        text = "Cancelar",
                        onClick = {
                            editedName = displayedName
                            isEditing = false
                            feedbackMessage = null
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    CampusHubButton(
                        text = "Editar perfil",
                        onClick = {
                            editedName = displayedName
                            isEditing = true
                            feedbackMessage = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                CampusHubSectionHeader(title = "Conta")
                Spacer(modifier = Modifier.height(12.dp))
                CampusHubOutlinedButton(
                    text = "Sair",
                    onClick = onLogout,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
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
