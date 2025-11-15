package ch.epfllife.ui.association

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.ui.composables.AssociationCard
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.EmptyScreen
import ch.epfllife.ui.composables.SearchBar
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun AssociationBrowser(
    modifier: Modifier = Modifier,
    viewModel: AssociationBrowserViewModel = viewModel(),
    onAssociationClick: (associationId: String) -> Unit,
) {
  var selected by remember { mutableStateOf(SubscriptionFilter.Subscribed) }

  // Replaced hardcoded lists with data from ViewModel
  val subscribedAssociations by viewModel.subscribedAssociations.collectAsState()
  val allAssociations by viewModel.allAssociations.collectAsState()

  val shownAssociations =
      if (selected == SubscriptionFilter.Subscribed) subscribedAssociations else allAssociations

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .testTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Empty space where the logo would normally be
    Spacer(Modifier.height(40.dp))

    SearchBar()

    Spacer(Modifier.height(12.dp))

    DisplayedSubscriptionFilter(
        selected = selected,
        onSelected = { selected = it },
        subscribedLabel = stringResource(id = R.string.subscribed_filter),
        allLabel = stringResource(id = R.string.all_associations_filter),
    )

    Spacer(Modifier.height(12.dp))

    // If statement to display certain messages for empty screens
    if (shownAssociations.isEmpty()) {
      val (title, description) =
          if (selected == SubscriptionFilter.Subscribed) {
            Pair(R.string.associations_empty_title, R.string.associations_empty_description)
          } else {
            Pair(R.string.associations_no_all_title, R.string.associations_no_all_description)
          }
      EmptyScreen(
          title = stringResource(id = title),
          description = stringResource(id = description),
      )
    } else {
      LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize(),
      ) {
        items(shownAssociations, key = { it.id }) { assoc ->
          AssociationCard(association = assoc, onClick = { onAssociationClick(assoc.id) })
        }
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun AssociationBrowserPreview() {
  MaterialTheme { AssociationBrowser(onAssociationClick = {}) }
}
