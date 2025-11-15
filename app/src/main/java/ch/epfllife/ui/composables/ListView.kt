package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun <T> ListView(
    list: List<T>,
    emptyTitle: String,
    emptyDescription: String? = null,
    onRefresh: () -> Unit,
    key: (T) -> Any,
    item: @Composable (T) -> Unit,
) {
  Refreshable(onRefresh = onRefresh) {
    if (list.isEmpty()) {
      EmptyListView(
          title = emptyTitle,
          description = emptyDescription,
      )
    } else {
      LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize(),
      ) {
        items(list, key = { key(it) }) { item(it) }
      }
    }
  }
}

@Composable
private fun EmptyListView(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
) {
  Column(
      modifier =
          modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
  ) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )
    if (description != null) {
      Spacer(Modifier.height(2.dp))
      Text(
          text = description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
      )
    }
  }
}
