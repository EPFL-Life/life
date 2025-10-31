package ch.epfllife.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.EventCard
import ch.epfllife.ui.composables.Price
import ch.epfllife.ui.composables.SearchBar

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
  var selected by remember { mutableStateOf(SubscriptionFilter.Subscribed) }

  val myEvents = remember { emptyList<Event>() } // No events to show empty state

  val allEvents = remember {
    listOf(
        Event(
            id = "1",
            title = "Via Ferrata",
            description = "Excursion to the Alps",
            location = Location(0.0, 0.0, "Lausanne Train Station"),
            time = "Oct 4th, 6:50am",
            associationId = "ESN Lausanne",
            tags = setOf("Sport", "Outdoor"),
            price = Price(30u)),
        Event(
            id = "2",
            title = "Music Festival",
            description = "Outdoor concert organized by the Cultural Club",
            location = Location(0.0, 0.0, "Esplanade"),
            time = "Nov 3rd, 5:00PM",
            associationId = "Cultural Club",
            tags = setOf("Music", "Festival"),
            price = Price(10u)))
  }

  val shownEvents = if (selected == SubscriptionFilter.Subscribed) myEvents else allEvents

  Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      Image(
          painter = painterResource(id = R.drawable.epfl_life_logo),
          contentDescription = "EPFL Life Logo",
          modifier = modifier.height(40.dp).testTag(HomeScreenTestTags.EPFLLOGO),
          contentScale = ContentScale.Fit)
    }

    Spacer(Modifier.height(12.dp))
    SearchBar()

    Spacer(Modifier.height(12.dp))

    DisplayedSubscriptionFilter(
        selected = selected,
        onSelected = { selected = it },
        subscribedLabel = stringResource(id = R.string.subscribed_filter),
        allLabel = stringResource(id = R.string.all_events_filter))

    Spacer(Modifier.height(12.dp))

    // If statement to display certain messages for empty screens
    if (shownEvents.isEmpty()) {
      val (title, description) =
          if (selected == SubscriptionFilter.Subscribed) {
            Pair(R.string.home_empty_title, R.string.home_empty_description)
          } else {
            Pair(R.string.home_no_events_title, R.string.home_no_events_description)
          }
      EmptyEventsMessage(
          title = stringResource(id = title),
          description = stringResource(id = description),
          modifier = modifier.fillMaxSize().padding(horizontal = 24.dp))
    } else {
      LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier.fillMaxSize()) {
            items(shownEvents, key = { it.id }) { ev ->
              EventCard(event = ev, onClick = { /* TODO: Navigate to event card */})
            }
          }
    }
  }
}

@Composable
private fun EmptyEventsMessage(title: String, description: String, modifier: Modifier = Modifier) {
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

object HomeScreenTestTags {
  const val EPFLLOGO = "EPFL_LOGO"
  const val BOTTON_SUBSCRIBED = "BUTTON_SUBSCRIBED"
  const val BUTTON_ALL = "BUTTON_ALL"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
  MaterialTheme { HomeScreen() }
}
