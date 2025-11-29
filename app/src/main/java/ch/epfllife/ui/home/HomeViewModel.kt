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

  private val _allEventsSubscribedAssociations = MutableStateFlow<List<Event>>(emptyList())
  val allEventsSubscribedAssociations: StateFlow<List<Event>> =
      _allEventsSubscribedAssociations.asStateFlow()

  init {
    db.eventRepo.listenAll { events -> viewModelScope.launch { updateState(events) } }
  }

  fun refresh(signalFinished: () -> Unit = {}) {
    viewModelScope.launch {
      updateState(db.eventRepo.getAllEvents())
      signalFinished()
    }
  }

  private suspend fun updateState(events: List<Event>) {
    try {
      _allEvents.value = events
      val user = db.userRepo.getCurrentUser()
      if (user != null) {
        val enrolledEventIds = user.enrolledEvents
        val subscribedAssociationIds = user.subscriptions
        _myEvents.value = _allEvents.value.filter { event -> enrolledEventIds.contains(event.id) }
        val subscribedAssocOnly =
            allEvents.value.filter { event ->
              subscribedAssociationIds.contains(event.association.id) &&
                  !enrolledEventIds.contains(event.id)
            }
        _allEventsSubscribedAssociations.value = _myEvents.value + subscribedAssocOnly
      } else {
        _myEvents.value = emptyList()
        _allEventsSubscribedAssociations.value = emptyList()
      }
    } catch (e: Exception) {
      Log.e("HomeViewModel", "Failed to refresh events", e)
      // Refresh failed, so we keep the previous state
    }
  }
}
