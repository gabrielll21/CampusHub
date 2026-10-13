package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.event.Event
import br.com.uri.meuprojeto.event.EventStatusFilter
import br.com.uri.meuprojeto.event.availableCategories
import br.com.uri.meuprojeto.event.filterEvents

@Composable
fun EventsScreen(
    events: List<Event>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onEventClick: (Event) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp)
        ) {
            Text(
                text = "CampusHub",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Eventos",
                modifier = Modifier.semantics { heading() },
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            var searchQuery by remember { mutableStateOf("") }
            var selectedCategory by remember { mutableStateOf<String?>(null) }
            var selectedStatus by remember {
                mutableStateOf(EventStatusFilter.ALL)
            }
            var isCategoryMenuExpanded by remember { mutableStateOf(false) }

            val categories = availableCategories(events)
            val visibleEvents = filterEvents(
                events = events,
                searchQuery = searchQuery,
                category = selectedCategory,
                statusFilter = selectedStatus
            )
            val hasActiveFilters = searchQuery.trim().isNotEmpty() ||
                selectedCategory != null ||
                selectedStatus != EventStatusFilter.ALL

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar eventos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box {
                OutlinedButton(
                    onClick = { isCategoryMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedCategory ?: "Todas as categorias")
                }
                DropdownMenu(
                    expanded = isCategoryMenuExpanded,
                    onDismissRequest = { isCategoryMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas as categorias") },
                        onClick = {
                            selectedCategory = null
                            isCategoryMenuExpanded = false
                        }
                    )
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                isCategoryMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Situação",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatusFilterButton(
                    label = "Todos",
                    selected = selectedStatus == EventStatusFilter.ALL,
                    onClick = { selectedStatus = EventStatusFilter.ALL }
                )
                StatusFilterButton(
                    label = "Próximos",
                    selected = selectedStatus == EventStatusFilter.UPCOMING,
                    onClick = { selectedStatus = EventStatusFilter.UPCOMING }
                )
                StatusFilterButton(
                    label = "Encerrados",
                    selected = selectedStatus == EventStatusFilter.ENDED,
                    onClick = { selectedStatus = EventStatusFilter.ENDED }
                )
            }

            if (hasActiveFilters) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        searchQuery = ""
                        selectedCategory = null
                        selectedStatus = EventStatusFilter.ALL
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Limpar filtros")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    errorMessage != null -> ErrorState(
                        message = errorMessage,
                        onRetry = onRetry
                    )
                    events.isEmpty() -> Text(
                        text = "Não há eventos disponíveis.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    visibleEvents.isEmpty() -> Text(
                        text = "Nenhum evento encontrado com os filtros atuais.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(visibleEvents, key = { event -> event.id }) { event ->
                            EventCard(
                                event = event,
                                onClick = onEventClick
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar")
            }
        }
    }
}

@Composable
private fun StatusFilterButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    if (selected) {
        Button(onClick = onClick) {
            Text(label)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(label)
        }
    }
}

@Composable
private fun EventCard(
    event: Event,
    onClick: (Event) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(event) }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(event.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = event.date,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = event.location,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = event.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Assertive
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Tentar novamente")
            }
        }
    }
}
