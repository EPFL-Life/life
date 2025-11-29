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

  fun refresh(signalFinished: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        _allEvents.value = db.eventRepo.getAllEvents()
        db.userRepo.getCurrentUser()?.let { currentUser ->
          val enrolledEventIds = currentUser.enrolledEvents
          _myEvents.value = _allEvents.value.filter { event -> enrolledEventIds.contains(event.id) }
        }
      } catch (e: Exception) {
        Log.e("HomeViewModel", "Failed to refresh events", e)
        // Refresh failed, so we keep the previous state
      }
      signalFinished()
    }
  }
}
