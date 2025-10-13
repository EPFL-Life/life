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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.model.entities.Event
import ch.epfllife.model.enums.EventsFilter
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.EventCard
import ch.epfllife.ui.composables.EventsFilterButtons
import ch.epfllife.ui.composables.SearchBar

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
  var selected by remember { mutableStateOf(EventsFilter.Subscribed) }

  val myEvents = remember { emptyList<Event>() } // No events to show empty state

  // val myEvents = remember { listOf( Event( id = "1", title = "Via Ferrata", description =
  // "Excursion to the Alps", location = Location(0.0, 0.0, "Lausanne Train Station"), time = "Oct
  // 4th, 6:50am", associationId = "ESN Lausanne", tags = setOf("Sport", "Outdoor"), price = 30)) }

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
                price = 30u
            ),
            Event(
                id = "2",
                title = "Music Festival",
                description = "Outdoor concert organized by the Cultural Club",
                location = Location(0.0, 0.0, "Esplanade"),
                time = "Nov 3rd, 5:00PM",
                associationId = "Cultural Club",
                tags = setOf("Music", "Festival"),
                price = 10u
            )
        )
    }

  val shownEvents = if (selected == EventsFilter.Subscribed) myEvents else allEvents

  Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
    // App Logo
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      Image(
          painter = painterResource(id = R.drawable.epfl_life_logo),
          contentDescription = "EPFL Life Logo",
          modifier = Modifier.height(40.dp),
          contentScale = ContentScale.Fit)
    }

    Spacer(Modifier.height(12.dp))
    SearchBar()

    Spacer(Modifier.height(12.dp))
    EventsFilterButtons(selected = selected, onSelected = { selected = it })

    Spacer(Modifier.height(12.dp))

    if (shownEvents.isEmpty() && selected == EventsFilter.Subscribed) {
      EmptyEventsMessage(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp))
    } else {
      LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(shownEvents, key = { it.id }) { ev -> EventCard(event = ev) }
          }
    }
  }
}

@Composable
private fun EmptyEventsMessage(modifier: Modifier = Modifier) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(
            text = "There’s nothing here!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(2.dp))
        Text(
            text =
                "Subscribe to clubs to fill your feed.\n\n" +
                    "Start by pressing the “Clubs” icon on the navigation bar below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
      }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
  MaterialTheme { HomeScreen() }
}