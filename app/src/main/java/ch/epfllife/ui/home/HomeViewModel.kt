package ch.epfllife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val db: Db) : ViewModel() {

  private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
  val allEvents: StateFlow<List<Event>> = _allEvents.asStateFlow()

  private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
  val myEvents: StateFlow<List<Event>> = _myEvents.asStateFlow()

  fun refresh(signalFinished: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        _allEvents.value = db.eventRepo.getAllEvents()
        val currentUser = db.userRepo.getCurrentUser()

        if (currentUser != null) {
          val enrolledEventIds = currentUser.enrolledEvents
          _myEvents.value = _allEvents.value.filter { event -> enrolledEventIds.contains(event.id) }
        } else {
          _myEvents.value = emptyList()
        }
      } catch (_: Exception) {
        _allEvents.value = emptyList()
        _myEvents.value = emptyList()
      }
      signalFinished()
    }
  }

  // For testing purposes - allows setting myEvents directly
  fun setMyEvents(events: List<Event>) {
    _myEvents.value = events
  }
}
