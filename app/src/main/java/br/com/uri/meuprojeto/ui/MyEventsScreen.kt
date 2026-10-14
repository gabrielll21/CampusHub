package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.event.Event
import br.com.uri.meuprojeto.ui.components.CampusHubEmptyState
import br.com.uri.meuprojeto.ui.components.CampusHubErrorState
import br.com.uri.meuprojeto.ui.components.CampusHubEventCard
import br.com.uri.meuprojeto.ui.components.CampusHubLoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEventsScreen(
    events: List<Event>,
    isLoading: Boolean,
    errorMessage: String?,
    onEventClick: (Event) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Meus Eventos") },
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isLoading -> CampusHubLoadingState()
                        errorMessage != null -> CampusHubErrorState(
                            message = errorMessage,
                            onRetry = onRetry,
                            modifier = Modifier.fillMaxWidth()
                        )
                        events.isEmpty() -> CampusHubEmptyState(
                            message = "Você ainda não está inscrito em nenhum evento.",
                            modifier = Modifier.fillMaxWidth()
                        )
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(
                                items = events,
                                key = { event -> event.id }
                            ) { event ->
                                CampusHubEventCard(
                                    title = event.title,
                                    description = event.description,
                                    date = event.date,
                                    location = event.location,
                                    category = event.category,
                                    onClick = { onEventClick(event) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
