package ch.epfllife.ui.home

import android.util.Log
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

  init {
    db.eventRepo.listenAll { events -> viewModelScope.launch { updateState(events) } }
  }

  fun refresh(signalFinished: () -> Unit = {}) {
    viewModelScope.launch {
      updateState(db.eventRepo.getAllEvents())
      signalFinished()
    }
  }

  suspend fun updateState(events: List<Event>) {
    try {
      _allEvents.value = events
      db.userRepo.getCurrentUser()?.let { currentUser ->
        val enrolledEventIds = currentUser.enrolledEvents
        _myEvents.value = events.filter { event -> enrolledEventIds.contains(event.id) }
      }
    } catch (e: Exception) {
      Log.e("HomeViewModel", "Failed to refresh events", e)
      // Refresh failed, so we keep the previous state
    }
  }
}
