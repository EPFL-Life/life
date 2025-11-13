package ch.epfllife.ui.calendar

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
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.model.event.Event
import ch.epfllife.ui.composables.CalendarCard
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.SearchBar
import ch.epfllife.ui.navigation.NavigationTestTags
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

object CalendarTestTags {
  const val MONTH_HEADER = "month_header"
  const val EVENT_CARD = "event_card"
  const val EVENT_DATE_BOX = "event_date_box"
  const val EVENT_TITLE = "event_title"
  const val EVENT_ASSOCIATION = "event_association"
  const val EVENT_ARROW = "event_arrow"
}

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    allEvents: List<Event>,
    enrolledEvents: List<Event>,
    onEventClick: (String) -> Unit
) {
  var selected by remember { mutableStateOf(SubscriptionFilter.Subscribed) }
  var query by remember { mutableStateOf("") }

  val shownEvents =
      remember(selected, query, allEvents, enrolledEvents) {
        val base = if (selected == SubscriptionFilter.Subscribed) enrolledEvents else allEvents
        if (query.isBlank()) base
        else
            base.filter {
              it.title.contains(query, ignoreCase = true) ||
                  it.association.name.contains(query, ignoreCase = true)
            }
      }
  // This excludes any events without a set date!!
  val grouped =
      shownEvents
          .filter { it.startDateOrNull() != null }
          .groupBy { event ->
            val date = event.startDateOrNull()!!
            "${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.year}"
          }

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp, vertical = 12.dp)
              .testTag(NavigationTestTags.CALENDAR_SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(40.dp))

        SearchBar()

        Spacer(Modifier.height(12.dp))

        DisplayedSubscriptionFilter(
            selected = selected,
            onSelected = { selected = it },
            subscribedLabel = stringResource(id = R.string.calendar_filter_enrolled),
            allLabel = stringResource(id = R.string.calendar_filter_all_events))

        Spacer(Modifier.height(12.dp))

        if (shownEvents.isEmpty()) {
          EmptyCalendarMessage(
              title = stringResource(id = R.string.calendar_no_events_placeholder),
              modifier = Modifier.fillMaxSize())
        } else {
          LazyColumn(
              verticalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxSize()) {
                grouped.forEach { (month, events) ->
                  item {
                    Text(
                        text = month,
                        style = MaterialTheme.typography.titleMedium,
                        modifier =
                            Modifier.padding(vertical = 8.dp)
                                .align(Alignment.Start)
                                .testTag(CalendarTestTags.MONTH_HEADER))
                  }

                  items(events, key = { it.id }) { event ->
                    CalendarCard(event = event, onClick = { onEventClick(event.id) })
                  }
                }
              }
        }
      }
}

@Composable
private fun EmptyCalendarMessage(title: String, modifier: Modifier = Modifier) {
  Column(
      modifier = modifier,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center)
      }
}

private fun Event.startDateOrNull(): LocalDate? {
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  return try {
    LocalDate.parse(this.time.substring(0, 10), formatter)
  } catch (_: Exception) {
    null
  }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
  // Mock associations
  val esn =
      ch.epfllife.model.association.Association(
          id = "a1",
          name = "ESN Lausanne",
          description = "Erasmus Student Network at EPFL.",
          pictureUrl = null,
          eventCategory = ch.epfllife.model.event.EventCategory.CULTURE)

  val codingClub =
      ch.epfllife.model.association.Association(
          id = "a2",
          name = "Coding Club",
          description = "Student coding community.",
          pictureUrl = null,
          eventCategory = ch.epfllife.model.event.EventCategory.TECH)

  // Mock location
  val location =
      ch.epfllife.model.map.Location(
          name = "Rolex Learning Center", latitude = 46.5191, longitude = 6.5668)

  // Mock events (include one spanning multiple days)
  val sampleEvents =
      listOf(
          ch.epfllife.model.event.Event(
              id = "1",
              title = "Innovation Week",
              description = "A week-long celebration of creativity and technology.",
              location = location,
              time = "2025-11-15T09:00:00/2025-11-20T18:00:00", // timespan example
              association = codingClub,
              tags = listOf("tech", "workshop")),
          ch.epfllife.model.event.Event(
              id = "2",
              title = "Cultural Night",
              description = "Join us for an evening of performances and food!",
              location = location,
              time = "2025-12-05T19:00:00",
              association = esn,
              tags = listOf("culture", "food")))

  MaterialTheme {
    CalendarScreen(
        allEvents = sampleEvents,
        enrolledEvents = emptyList(), // Enrolled tab should be empty → shows placeholder message
        onEventClick = {})
  }
}
