package ch.epfllife.ui.eventDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.Price
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ch.epfllife.model.association.Association

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
        EventRepositoryFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())
) : ViewModel() {

  private val _uiState = MutableStateFlow<EventDetailsUIState>(EventDetailsUIState.Loading)
  val uiState: StateFlow<EventDetailsUIState> = _uiState.asStateFlow()

  /** Loads event details by ID using EventRepository.getEvent. */
  fun loadEvent(eventId: String) {
    viewModelScope.launch {
      try {
        val fakeEvent =
            Event(
                id = eventId,
                title = "Drone Workshop",
                description =
                    "The Drone Workshop is a multi-evening workshop organized by AéroPoly...",
                location = Location(46.5191, 6.5668, "Centre Sport et Santé"),
                time = "2025-10-12 18:00",
                association =
                    Association(
                        id = "AéroPoly",
                        name = "AéroPoly",
                        description = "The association for drone enthusiasts at EPFL.",
                        eventCategory = EventCategory.ACADEMIC),
                tags = listOf("workshop"),
                price = Price(10u),
                pictureUrl =
                    "https://www.shutterstock.com/image-photo/engineer-working-on-racing-fpv-600nw-2278353271.jpg")
        _uiState.value = EventDetailsUIState.Success(fakeEvent, isEnrolled = false)
        val event = repo.getEvent(eventId)
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
        // TODO: implement logic
        _uiState.value = EventDetailsUIState.Success(event, isEnrolled = true)
      } catch (e: Exception) {
        _uiState.value = EventDetailsUIState.Error("Failed to enrol: ${e.message}")
      }
    }
  }

  /**
   * Check if user is enrolled, this will be only possible if enrollments are tracked by us (not
   * implemented for now, as we only do redirection for the MVP)
   */
  fun isEnrolled(event: Event): Boolean {
    // TODO implement actual logic (Firestore / API call)
    // we need this to decide whether the button should be gray or not ("Enroll in event" or
    // "Enrolled")
    if (event.id == event.title) return false

    return false
  }
}
