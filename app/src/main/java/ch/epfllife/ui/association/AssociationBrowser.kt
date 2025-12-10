package ch.epfllife.ui.association

import androidx.compose.foundation.layout.*
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
import ch.epfllife.model.db.Db
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.ui.composables.AssociationCard
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.EPFLLogo
import ch.epfllife.ui.composables.ListView
import ch.epfllife.ui.composables.SearchBar
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun AssociationBrowser(
    modifier: Modifier = Modifier,
    db: Db,
    viewModel: AssociationBrowserViewModel = viewModel { AssociationBrowserViewModel(db) },
    onAssociationClick: (associationId: String) -> Unit,
) {
  LaunchedEffect(Unit) { viewModel.refresh() }
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
    EPFLLogo(modifier = modifier)

    Spacer(Modifier.height(12.dp))

    var query by remember { mutableStateOf("") }
    SearchBar(query = query, onQueryChange = { query = it })

    Spacer(Modifier.height(12.dp))

    DisplayedSubscriptionFilter(
        selected = selected,
        onSelected = { selected = it },
        subscribedLabel = stringResource(id = R.string.subscribed_filter),
        allLabel = stringResource(id = R.string.all_associations_filter),
    )

    Spacer(Modifier.height(12.dp))

    val (title, description) =
        if (selected == SubscriptionFilter.Subscribed) {
          Pair(R.string.associations_empty_title, R.string.associations_empty_description)
        } else {
          Pair(R.string.associations_no_all_title, R.string.associations_no_all_description)
        }
    val filteredAssociations =
        shownAssociations.filter { it.name.contains(query, ignoreCase = true) }
    ListView(
        list = filteredAssociations,
        emptyTitle = stringResource(id = title),
        emptyDescription = stringResource(id = description),
        onRefresh = viewModel::refresh,
    ) { list ->
      items(list, key = { assoc -> assoc.id }) { assoc ->
        AssociationCard(association = assoc, onClick = { onAssociationClick(assoc.id) })
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun AssociationBrowserPreview() {
  MaterialTheme { AssociationBrowser(onAssociationClick = {}, db = Db.freshLocal()) }
}
