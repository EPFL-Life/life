package ch.epfllife.ui.eventDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepository
import ch.epfllife.model.user.UserRepositoryFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the EventDetails screen. */
sealed class EventDetailsUIState {
  object Loading : EventDetailsUIState()

  data class Success(val event: Event, val isEnrolled: Boolean) : EventDetailsUIState()

  data class Error(val message: String) : EventDetailsUIState()
}

/**
 * ViewModel for the EventDetails screen.
 *
 * Uses `EventRepository` to fetch an Event by id.
 *
 * @param repo The repository to fetch events from.
 */
class EventDetailsViewModel(
    private val repo: EventRepository =
        EventRepositoryFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance()),
    private val userRepo: UserRepository =
        UserRepositoryFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())
) : ViewModel() {

  private val _uiState = MutableStateFlow<EventDetailsUIState>(EventDetailsUIState.Loading)
  val uiState: StateFlow<EventDetailsUIState> = _uiState.asStateFlow()
  private var currentUser: User? = null

  /** Loads event details by ID using EventRepository.getEvent. */
  fun loadEvent(eventId: String) {
    viewModelScope.launch {
      try {
        val event = repo.getEvent(eventId)
        currentUser = userRepo.getCurrentUser()
        if (event != null) {
          _uiState.value = EventDetailsUIState.Success(event, isEnrolled = isEnrolled(event))
        } else {
          _uiState.value = EventDetailsUIState.Error("Event not found")
        }
      } catch (e: Exception) {
        _uiState.value = EventDetailsUIState.Error("Failed to load event: ${e.message}")
      }
    }
  }

  /**
   * Logic to enroll in an event. Either a redirection to separate signup page or use Firebase to
   * track enrollment
   */
  fun enrollInEvent(event: Event) {
    viewModelScope.launch {
      try {
        userRepo
            .subscribeToEvent(event.id)
            .fold(
                onSuccess = { loadEvent(event.id) },
                onFailure = { error ->
                  _uiState.value = EventDetailsUIState.Error("Failed to enrol: Please try again.")
                })
      } catch (e: Exception) {
        _uiState.value = EventDetailsUIState.Error("Failed to enrol: Please try again.")
      }
    }
  }

  /**
   * Check if user is enrolled, this will be only possible if enrollments are tracked by us (not
   * implemented for now, as we only do redirection for the MVP)
   */
  fun isEnrolled(event: Event): Boolean {
    return currentUser?.enrolledEvents?.contains(event.id) ?: false
  }
}
