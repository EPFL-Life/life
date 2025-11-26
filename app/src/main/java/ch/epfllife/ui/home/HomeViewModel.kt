package ch.epfllife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.model.user.UserRepository
import ch.epfllife.model.user.UserRepositoryFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: EventRepository =
        EventRepositoryFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance()),
    private val userRepo: UserRepository =
        UserRepositoryFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())
) : ViewModel() {

  private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
  val allEvents: StateFlow<List<Event>> = _allEvents.asStateFlow()

  private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
  val myEvents: StateFlow<List<Event>> = _myEvents.asStateFlow()

  fun refresh(signalFinished: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        _allEvents.value = repo.getAllEvents()
        val currentUser = userRepo.getCurrentUser()

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
