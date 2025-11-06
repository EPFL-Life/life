package ch.epfllife.ui.association

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.model.association.Association
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.model.event.EventCategory
import ch.epfllife.ui.composables.AssociationCard
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.SearchBar
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun AssociationBrowser(
    modifier: Modifier = Modifier,
) {
  var selected by remember { mutableStateOf(SubscriptionFilter.Subscribed) }
  val subscribedAssociations = remember {
    emptyList<Association>()
  } // No Associations to show empty state

  val allAssociations = remember {
    listOf(
        Association(
            id = "1",
            name = "ESN Lausanne",
            description = "Erasmus Student Network at EPFL.",
            pictureUrl = null,
            eventCategory = EventCategory.CULTURE),
        Association(
            id = "2",
            name = "EPFL Sports Club",
            description = "Join for weekly sports activities and tournaments.",
            pictureUrl = null,
            eventCategory = EventCategory.SPORTS))
  }

  val shownAssociations =
      if (selected == SubscriptionFilter.Subscribed) subscribedAssociations else allAssociations

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .testTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally) {
        // Empty space where the logo would normally be
        Spacer(Modifier.height(40.dp))

        SearchBar()

        Spacer(Modifier.height(12.dp))

        DisplayedSubscriptionFilter(
            selected = selected,
            onSelected = { selected = it },
            subscribedLabel = stringResource(id = R.string.subscribed_filter),
            allLabel = stringResource(id = R.string.all_associations_filter))

        Spacer(Modifier.height(12.dp))

        // If statement to display certain messages for empty screens
        if (shownAssociations.isEmpty()) {
          val (title, description) =
              if (selected == SubscriptionFilter.Subscribed) {
                Pair(R.string.associations_empty_title, R.string.associations_empty_description)
              } else {
                Pair(R.string.associations_no_all_title, R.string.associations_no_all_description)
              }
          EmptyAssociationsMessage(
              title = stringResource(id = title),
              description = stringResource(id = description),
              modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp))
        } else {
          LazyColumn(
              verticalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxSize()) {
                items(shownAssociations, key = { it.id }) { assoc ->
                  AssociationCard(
                      association = assoc, onClick = { /* TODO: Navigate to association details */})
                }
              }
        }
      }
}

@Composable
private fun EmptyAssociationsMessage(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(
            text = description,
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
