package ch.epfllife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.model.user.UserRepository
import ch.epfllife.model.user.UserRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repo: EventRepository = EventRepositoryFirestore(FirebaseFirestore.getInstance()),
    private val userRepo: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance()),
) : ViewModel() {

  private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
  val allEvents: StateFlow<List<Event>> = _allEvents.asStateFlow()

  private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
  val myEvents: StateFlow<List<Event>> = _myEvents.asStateFlow()

  init {
    refresh()
  }

  fun refresh(signalFinished: () -> Unit = {}) {
    viewModelScope.launch {
      _allEvents.value =
          try {
            repo.getAllEvents()
          } catch (_: Exception) {
            emptyList()
          }
      userRepo.getCurrentUser()?.let { user ->
        _myEvents.value =
            _allEvents.value.filter { event -> user.enrolledEvents.contains(event.id) }
      }
      signalFinished()
    }
  }

  // For testing purposes - allows setting myEvents directly
  fun setMyEvents(events: List<Event>) {
    _myEvents.value = events
  }
}
