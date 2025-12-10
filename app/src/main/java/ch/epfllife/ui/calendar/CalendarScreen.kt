package ch.epfllife.ui.calendar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.model.event.Event
import ch.epfllife.ui.composables.CalendarCard
import ch.epfllife.ui.composables.DisplayedSubscriptionFilter
import ch.epfllife.ui.composables.ListView
import ch.epfllife.ui.composables.SearchBar
import ch.epfllife.ui.home.HomeScreenTestTags
import ch.epfllife.ui.home.HomeViewModel
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
    db: Db,
    viewModel: HomeViewModel = viewModel { HomeViewModel(db) },
    onEventClick: (String) -> Unit,
) {
  LaunchedEffect(Unit) { viewModel.refresh() }
  var selected by remember { mutableStateOf(SubscriptionFilter.Subscribed) }
  var query by remember { mutableStateOf("") }

  val enrolledEvents by viewModel.myEvents.collectAsState()
  val allEvents by viewModel.allEvents.collectAsState()

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
          .sortedBy { it.startDateOrNull() }
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
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
      Image(
          painter = painterResource(id = R.drawable.epfl_life_logo),
          contentDescription = "EPFL Life Logo",
          modifier = modifier.height(40.dp).testTag(HomeScreenTestTags.EPFLLOGO),
          contentScale = ContentScale.Fit,
      )
    }

    Spacer(Modifier.height(12.dp))

    SearchBar(query = query, onQueryChange = { query = it })

    Spacer(Modifier.height(12.dp))

    DisplayedSubscriptionFilter(
        selected = selected,
        onSelected = { selected = it },
        subscribedLabel = stringResource(id = R.string.calendar_filter_enrolled),
        allLabel = stringResource(id = R.string.calendar_filter_all_events),
    )

    Spacer(Modifier.height(12.dp))

    ListView(
        list = grouped.toList(),
        emptyTitle = stringResource(id = R.string.calendar_no_events_placeholder),
        onRefresh = { signalFinished -> viewModel.refresh(signalFinished) },
    ) { list ->
      list.forEach { (month, events) ->
        item {
          Text(
              text = month,
              style = MaterialTheme.typography.titleMedium,
              modifier =
                  Modifier.padding(vertical = 8.dp)
                      .align(Alignment.Start)
                      .testTag(CalendarTestTags.MONTH_HEADER),
          )
        }

        items(events, key = { it.id }) { event ->
          CalendarCard(event = event, onClick = { onEventClick(event.id) })
        }
      }
    }
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
