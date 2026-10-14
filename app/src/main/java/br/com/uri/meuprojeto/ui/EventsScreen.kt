package br.com.uri.meuprojeto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.uri.meuprojeto.event.Event
import br.com.uri.meuprojeto.event.EventStatusFilter
import br.com.uri.meuprojeto.event.availableCategories
import br.com.uri.meuprojeto.event.filterEvents
import br.com.uri.meuprojeto.ui.components.CampusHubEmptyState
import br.com.uri.meuprojeto.ui.components.CampusHubErrorState
import br.com.uri.meuprojeto.ui.components.CampusHubEventCard
import br.com.uri.meuprojeto.ui.components.CampusHubLoadingState
import br.com.uri.meuprojeto.ui.components.CampusHubOutlinedButton
import br.com.uri.meuprojeto.ui.components.CampusHubTextField

@OptIn(ExperimentalMaterial3Api::class)
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
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf(EventStatusFilter.ALL) }
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Eventos") },
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
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Spacer(modifier = Modifier.height(16.dp))

            CampusHubTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Buscar eventos",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box {
                CampusHubOutlinedButton(
                    text = selectedCategory ?: "Todas as categorias",
                    onClick = { isCategoryMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                )

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

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Situação",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                item {
                    StatusFilterChip(
                        label = "Todos",
                        selected = selectedStatus == EventStatusFilter.ALL,
                        onClick = { selectedStatus = EventStatusFilter.ALL }
                    )
                }
                item {
                    StatusFilterChip(
                        label = "Próximos",
                        selected = selectedStatus == EventStatusFilter.UPCOMING,
                        onClick = { selectedStatus = EventStatusFilter.UPCOMING }
                    )
                }
                item {
                    StatusFilterChip(
                        label = "Encerrados",
                        selected = selectedStatus == EventStatusFilter.ENDED,
                        onClick = { selectedStatus = EventStatusFilter.ENDED }
                    )
                }
            }

            if (hasActiveFilters) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        searchQuery = ""
                        selectedCategory = null
                        selectedStatus = EventStatusFilter.ALL
                    }
                ) {
                    Text("Limpar filtros")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        message = "Não há eventos disponíveis.",
                        modifier = Modifier.fillMaxWidth()
                    )
                    visibleEvents.isEmpty() -> CampusHubEmptyState(
                        message = "Nenhum evento encontrado com os filtros atuais.",
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(
                            items = visibleEvents,
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) }
    )
}
