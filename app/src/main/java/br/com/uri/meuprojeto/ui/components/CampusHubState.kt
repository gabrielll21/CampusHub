package br.com.uri.meuprojeto.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun CampusHubLoadingState(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier.semantics {
            contentDescription = "Carregando"
        }
    )
}

@Composable
fun CampusHubEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun CampusHubErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Assertive
            },
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )

        onRetry?.let { retry ->
            Spacer(modifier = Modifier.height(12.dp))
            CampusHubOutlinedButton(
                text = "Tentar novamente",
                onClick = retry
            )
        }
    }
}


@Composable
fun CampusHubFeedback(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        modifier = modifier.semantics {
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
