package ch.epfllife.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.ui.composables.CompactEventCard
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarGridScreen(
    db: Db,
    viewModel: CalendarGridViewModel = viewModel { CalendarGridViewModel(db) },
    onEventClick: (String) -> Unit
) {
  val currentMonth by viewModel.currentMonth.collectAsState()
  val selectedDate by viewModel.selectedDate.collectAsState()
  val enrolledEventIds by viewModel.enrolledEventIds.collectAsState()

  // Trigger event loading when the screen is shown
  LaunchedEffect(Unit) { viewModel.loadEvents() }

  Column(modifier = Modifier.fillMaxSize()) {
    CalendarMonthHeader(
        currentMonth = currentMonth,
        onPreviousMonth = { viewModel.previousMonth() },
        onNextMonth = { viewModel.nextMonth() })

    CalendarWeekdayHeader()

    Spacer(modifier = Modifier.height(8.dp))

    CalendarGrid(
        currentMonth = currentMonth,
        selectedDate = selectedDate,
        viewModel = viewModel // Passing viewModel for cleaner logic interaction in grid generation
        )

    Spacer(modifier = Modifier.height(16.dp))

    CalendarEventsList(
        selectedDate = selectedDate,
        viewModel = viewModel,
        enrolledEventIds = enrolledEventIds,
        onEventClick = onEventClick)
  }
}

@Composable
private fun CalendarMonthHeader(
    currentMonth: java.time.YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPreviousMonth) {
          Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.calendar_previous_month))
        }
        Text(
            text =
                "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)
        IconButton(onClick = onNextMonth) {
          Icon(
              Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = stringResource(id = R.string.calendar_next_month))
        }
      }
}

@Composable
private fun CalendarWeekdayHeader() {
  Row(modifier = Modifier.fillMaxWidth()) {
    val daysOfWeek =
        listOf(
            stringResource(id = R.string.calendar_day_mon),
            stringResource(id = R.string.calendar_day_tue),
            stringResource(id = R.string.calendar_day_wed),
            stringResource(id = R.string.calendar_day_thu),
            stringResource(id = R.string.calendar_day_fri),
            stringResource(id = R.string.calendar_day_sat),
            stringResource(id = R.string.calendar_day_sun))
    daysOfWeek.forEach { day ->
      Text(
          text = day,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.bodyMedium,
          color = Color.Gray)
    }
  }
}

@Composable
private fun CalendarGrid(
    currentMonth: java.time.YearMonth,
    selectedDate: java.time.LocalDate,
    viewModel: CalendarGridViewModel
) {
  val allDays = viewModel.calculateCalendarDays(currentMonth)
  val totalRows =
      (allDays.size + 6) / 7 // Ensure we cover all days, though typically it's exact multiples of 7

  Column {
    for (row in 0 until totalRows) {
      Row(modifier = Modifier.fillMaxWidth()) {
        for (col in 0 until 7) {
          val index = row * 7 + col
          if (index < allDays.size) {
            val date = allDays[index]
            val isSelected = date == selectedDate
            val isCurrentMonth = date.month == currentMonth.month
            val hasEnrolledEvents = viewModel.hasEnrolledEventsOnDate(date)

            CalendarDayCell(
                date = date,
                isSelected = isSelected,
                isCurrentMonth = isCurrentMonth,
                hasEnrolledEvents = hasEnrolledEvents,
                onDateClick = { viewModel.selectDate(it) },
                modifier = Modifier.weight(1f))
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun CalendarDayCell(
    date: java.time.LocalDate,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    hasEnrolledEvents: Boolean,
    onDateClick: (java.time.LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
  Box(
      modifier =
          modifier
              .aspectRatio(1f)
              .padding(4.dp)
              .background(
                  color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                  shape = CircleShape)
              .testTag("calendar_day_${date.dayOfMonth}")
              .clickable { onDateClick(date) },
      contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
              text = date.dayOfMonth.toString(),
              color =
                  if (isSelected) Color.White
                  else if (isCurrentMonth) MaterialTheme.colorScheme.onSurface else Color.Gray,
              style = MaterialTheme.typography.bodyMedium)
          if (hasEnrolledEvents) {
            Box(
                modifier =
                    Modifier.size(4.dp)
                        .background(
                            color =
                                if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                            shape = CircleShape))
          }
        }
      }
}

@Composable
private fun CalendarEventsList(
    selectedDate: java.time.LocalDate,
    viewModel: CalendarGridViewModel,
    enrolledEventIds: Set<String>,
    onEventClick: (String) -> Unit
) {
  Text(
      text =
          stringResource(
              id = R.string.calendar_events_for_date,
              selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))),
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(horizontal = 16.dp))

  Spacer(modifier = Modifier.height(8.dp))

  // Events List for Selected Date
  val eventsForDate =
      viewModel.getEventsForDate(selectedDate).sortedByDescending { it.id in enrolledEventIds }

  LazyColumn(
      contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (eventsForDate.isEmpty()) {
          item {
            Text(
                text = stringResource(id = R.string.calendar_no_events_on_day),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center)
          }
        } else {
          items(eventsForDate) { event ->
            CompactEventCard(
                event = event,
                isEnrolled = enrolledEventIds.contains(event.id),
                onClick = { onEventClick(event.id) })
          }
        }
      }
}
