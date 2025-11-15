package ch.epfllife.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.EventCard
import ch.epfllife.ui.composables.ListView
import ch.epfllife.ui.composables.SearchBar
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
    onEventClick: (eventId: String) -> Unit,
) {
  var selected by remember { mutableStateOf(SubscriptionFilter.Subscribed) }

  val myEvents by viewModel.myEvents.collectAsState()
  val allEvents by viewModel.allEvents.collectAsState()

  val shownEvents = if (selected == SubscriptionFilter.Subscribed) myEvents else allEvents

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .testTag(NavigationTestTags.HOMESCREEN_SCREEN)) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Image(
              painter = painterResource(id = R.drawable.epfl_life_logo),
              contentDescription = "EPFL Life Logo",
              modifier = modifier.height(40.dp).testTag(HomeScreenTestTags.EPFLLOGO),
              contentScale = ContentScale.Fit,
          )
        }

        Spacer(Modifier.height(12.dp))
        SearchBar()

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
        ListView(
            list = shownEvents,
            emptyTitle = stringResource(title),
            emptyDescription = stringResource(description),
            onRefresh = { viewModel.refresh() },
            key = { it.id },
            item = { ev -> EventCard(event = ev, onClick = { onEventClick(ev.id) }) },
        )
      }
}

object HomeScreenTestTags {
  const val EPFLLOGO = "EPFL_LOGO"
  const val BOTTON_SUBSCRIBED = "BUTTON_SUBSCRIBED"
  const val BUTTON_ALL = "BUTTON_ALL"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
  MaterialTheme { HomeScreen(onEventClick = {}) }
}
