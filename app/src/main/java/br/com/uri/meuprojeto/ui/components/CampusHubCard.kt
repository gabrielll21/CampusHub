package br.com.uri.meuprojeto.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CampusHubCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardContent: @Composable ColumnScope.() -> Unit = {
        Column(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }

    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            content = cardContent
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.medium,
            content = cardContent
        )
    }
}
