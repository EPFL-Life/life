package ch.epfllife.ui.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CalendarGridViewModel(private val db: Db) : ViewModel() {

  private val _currentMonth = MutableStateFlow(YearMonth.now())
  val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

  private val _selectedDate = MutableStateFlow(LocalDate.now())
  val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

  private val _events = MutableStateFlow<List<Event>>(emptyList())
  val events: StateFlow<List<Event>> = _events.asStateFlow()

  private val _enrolledEventIds = MutableStateFlow<Set<String>>(emptySet())
  val enrolledEventIds: StateFlow<Set<String>> = _enrolledEventIds.asStateFlow()

  init {
    loadEvents()
  }

  fun loadEvents() {
    viewModelScope.launch {
      try {
        val allEvents = db.eventRepo.getAllEvents()
        _events.value = allEvents

        val user = db.userRepo.getCurrentUser()
        if (user != null) {
          _enrolledEventIds.value = user.enrolledEvents.toSet()
        } else {
          _enrolledEventIds.value = emptySet()
        }
      } catch (e: Exception) {
        Log.e("CalendarGridViewModel", "Failed to load events", e)
      }
    }
  }

  fun nextMonth() {
    // https://docs.oracle.com/javase/8/docs/api/java/time/YearMonth.html
    _currentMonth.value = _currentMonth.value.plusMonths(1)
  }

  fun previousMonth() {
    // https://docs.oracle.com/javase/8/docs/api/java/time/YearMonth.html
    _currentMonth.value = _currentMonth.value.minusMonths(1)
  }

  fun selectDate(date: LocalDate) {
    _selectedDate.value = date
  }

  fun getEventsForDate(date: LocalDate): List<Event> {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return _events.value.filter { event ->
      try {
        val eventDate = LocalDate.parse(event.time.substring(0, 10), formatter)
        eventDate == date
      } catch (e: Exception) {
        false
      }
    }
  }

  /**
   * Simple check if there are any enrolled events on the given date we then use this to display a
   * little dot on the date to indicate there is event for which the user already enrolled.
   *
   * @param date The date to check
   * @return True if there are any enrolled events on the given date, false otherwise
   */
  fun hasEnrolledEventsOnDate(date: LocalDate): Boolean {
    val eventsOnDate = getEventsForDate(date)
    return eventsOnDate.any { _enrolledEventIds.value.contains(it.id) }
  }
}
