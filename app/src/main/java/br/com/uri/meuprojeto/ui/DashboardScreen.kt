package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.ui.components.CampusHubCard
import br.com.uri.meuprojeto.ui.components.CampusHubErrorState
import br.com.uri.meuprojeto.ui.components.CampusHubLoadingState
import br.com.uri.meuprojeto.ui.components.CampusHubOutlinedButton
import br.com.uri.meuprojeto.ui.components.CampusHubSectionHeader

@Composable
fun DashboardScreen(
    name: String?,
    email: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onLogout: () -> Unit,
    onEventsClick: () -> Unit,
    onMyEventsClick: () -> Unit,
    onMyFavoritesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            CampusHubLoadingState(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                ) {
                    DashboardHeader(
                        name = name,
                        errorMessage = errorMessage
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    if (errorMessage != null) {
                        CampusHubErrorState(
                            message = errorMessage,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (name != null && email != null) {
                        DashboardContent(
                            email = email,
                            onEventsClick = onEventsClick,
                            onMyEventsClick = onMyEventsClick,
                            onMyFavoritesClick = onMyFavoritesClick,
                            onProfileClick = onProfileClick
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    CampusHubOutlinedButton(
                        text = "Logout",
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    name: String?,
    errorMessage: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CampusHub",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (errorMessage == null && name != null) {
                "Olá, $name!"
            } else {
                "Home"
            },
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Encontre eventos e acompanhe sua vida no campus.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun DashboardContent(
    email: String,
    onEventsClick: () -> Unit,
    onMyEventsClick: () -> Unit,
    onMyFavoritesClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    CampusHubCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEventsClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(
            text = "Eventos",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Explore atividades e experiências do campus.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Abrir eventos",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
    }

    Spacer(modifier = Modifier.height(28.dp))

    CampusHubSectionHeader(
        title = "Sua atividade",
        subtitle = "Acesse rapidamente seus eventos e favoritos."
    )

    Spacer(modifier = Modifier.height(14.dp))

    DashboardShortcut(
        title = "Meus Eventos",
        description = "Consulte os eventos em que você está inscrito.",
        onClick = onMyEventsClick
    )

    Spacer(modifier = Modifier.height(12.dp))

    DashboardShortcut(
        title = "Meus Favoritos",
        description = "Retome os eventos que você guardou.",
        onClick = onMyFavoritesClick
    )

    Spacer(modifier = Modifier.height(28.dp))

    CampusHubSectionHeader(
        title = "Sua conta",
        subtitle = "Gerencie seu perfil e sua sessão."
    )

    Spacer(modifier = Modifier.height(14.dp))

    CampusHubCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onProfileClick
    ) {
        Text(
            text = "Meu Perfil",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = email,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Abrir perfil",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
    }

}

@Composable
private fun DashboardShortcut(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    CampusHubCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Abrir",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
