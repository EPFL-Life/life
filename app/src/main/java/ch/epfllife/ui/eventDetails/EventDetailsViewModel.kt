package com.android.sample.ui.eventDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.entities.Event
import ch.epfllife.model.map.Location
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
 * Handles loading and exposing a single Event object.
 */
class EventDetailsViewModel : ViewModel() {

  private val _uiState = MutableStateFlow<EventDetailsUIState>(EventDetailsUIState.Loading)
  val uiState: StateFlow<EventDetailsUIState> = _uiState.asStateFlow()

  /** Loads event details by ID. In the future this would call a repository. */
  fun loadEvent(eventId: String) {
    viewModelScope.launch {
      _uiState.value = EventDetailsUIState.Loading
      try {
        val fakeEvent =
            Event(
                id = "1",
                title = "Drone Workshop",
                description =
                    "The Drone Workshop is a multi-evening workshop organized by AéroPoly...",
                location = Location(46.5191, 6.5668, "Centre Sport et Santé"),
                time = "2025-10-12 18:00",
                associationId = "AeroPoly",
                tags = setOf("workshop"),
                price = 10u,
                imageUrl =
                    "https://www.shutterstock.com/image-photo/engineer-working-on-racing-fpv-600nw-2278353271.jpg")
        _uiState.value = EventDetailsUIState.Success(fakeEvent, isEnrolled = false)
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
    return false
  }
}
