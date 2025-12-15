package ch.epfllife.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.EPFLLogo
import ch.epfllife.ui.composables.EventCard
import ch.epfllife.ui.composables.ListView
import ch.epfllife.ui.composables.SearchBar
import ch.epfllife.ui.composables.subscriptionSwipe
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    db: Db,
    viewModel: HomeViewModel = viewModel { HomeViewModel(db) },
    onEventClick: (eventId: String) -> Unit,
) {
  LaunchedEffect(Unit) { viewModel.refresh() }
  var selected by rememberSaveable { mutableStateOf(SubscriptionFilter.Subscribed) }

  val allEvents by viewModel.allEvents.collectAsState()
  val subscribedEventsCombined by viewModel.allEventsSubscribedAssociations.collectAsState()
  val enrolledEvents by viewModel.myEvents.collectAsState()

  val shownEvents =
      if (selected == SubscriptionFilter.Subscribed) subscribedEventsCombined else allEvents

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .subscriptionSwipe(selected) { selected = it }
              .testTag(NavigationTestTags.HOMESCREEN_SCREEN)) {
        EPFLLogo(modifier = modifier)

        Spacer(Modifier.height(12.dp))
        var query by remember { mutableStateOf("") }
        SearchBar(query = query, onQueryChange = { query = it })

        Spacer(Modifier.height(12.dp))

        DisplayedSubscriptionFilter(
            selected = selected,
            onSelected = { selected = it },
            subscribedLabel = stringResource(id = R.string.subscribed_filter),
            allLabel = stringResource(id = R.string.all_events_filter),
        )

        Spacer(Modifier.height(12.dp))

        val (title, description) =
            if (selected == SubscriptionFilter.Subscribed) {
              Pair(R.string.home_empty_title, R.string.home_empty_description)
            } else {
              Pair(R.string.home_no_events_title, R.string.home_no_events_description)
            }

        val filteredEvents =
            shownEvents.filter {
              it.title.contains(query, ignoreCase = true) ||
                  it.association.name.contains(query, ignoreCase = true)
            }

        ListView(
            list = filteredEvents,
            emptyTitle = stringResource(title),
            emptyDescription = stringResource(description),
            onRefresh = viewModel::refresh,
        ) { list ->
          items(list, key = { ev -> ev.id }) { ev ->
            EventCard(
                event = ev,
                isEnrolled = enrolledEvents.contains(ev),
                onClick = { onEventClick(ev.id) })
          }
        }
      }
}

object HomeScreenTestTags {

  const val BUTTON_SUBSCRIBED = "BUTTON_SUBSCRIBED"
  const val BUTTON_ALL = "BUTTON_ALL"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
  MaterialTheme { HomeScreen(onEventClick = {}, db = Db.freshLocal()) }
}
