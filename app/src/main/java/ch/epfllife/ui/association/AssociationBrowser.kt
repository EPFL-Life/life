package ch.epfllife.ui.association

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.model.entities.Association
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.model.enums.Category
import ch.epfllife.ui.composables.AssociationCard
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.SearchBar

@Composable
fun AssociationBrowser(modifier: Modifier = Modifier) {
  var selected by remember { mutableStateOf(SubscriptionFilter.Subscribed) }
  val subscribedAssociations = remember { emptyList<Association>() } // No Associations to show empty state

  // hardcoded data
  // val subscribedAssociations = remember { listOf(Association(id = "1",name = "ESN
  // Lausanne",description = "Erasmus Student Network at EPFL.",pictureUrl = null,category =
  // Category.CULTURE))}

  val allAssociations = remember {
    listOf(
        Association(
            id = "1",
            name = "ESN Lausanne",
            description = "Erasmus Student Network at EPFL.",
            pictureUrl = null,
            category = Category.CULTURE),
        Association(
            id = "2",
            name = "EPFL Sports Club",
            description = "Join for weekly sports activities and tournaments.",
            pictureUrl = null,
            category = Category.SPORTS))
  }

  val shownAssociations =
      if (selected == SubscriptionFilter.Subscribed) subscribedAssociations else allAssociations

  Column(
      modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Empty space where the logo would normally be
        Spacer(Modifier.height(40.dp))

        SearchBar()

        Spacer(Modifier.height(12.dp))

      DisplayedSubscriptionFilter(
          selected = selected,
          onSelected = { selected = it },
          subscribedLabel = stringResource(id = R.string.subscribed_filter),
          allLabel = stringResource(id = R.string.all_associations_filter)
      )

      Spacer(Modifier.height(12.dp))

        if (shownAssociations.isEmpty() && selected == SubscriptionFilter.Subscribed) {
          EmptyAssociationsMessage(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp))
        } else {
          LazyColumn(
              verticalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxSize()) {
                items(shownAssociations, key = { it.id }) { assoc ->
                  AssociationCard(association = assoc)
                }
              }
        }
      }
}

@Composable
private fun EmptyAssociationsMessage(modifier: Modifier = Modifier) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(
            text = stringResource(id = R.string.associations_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(
            stringResource(id = R.string.associations_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
      }
}

@Preview(showBackground = true)
@Composable
private fun AssociationBrowserPreview() {
  MaterialTheme { AssociationBrowser() }
}
