package ch.epfllife.ui.eventDetails

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.model.user.User
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
 */
class EventDetailsViewModel(
    private val db: Db,
) : ViewModel() {

  private val _uiState = MutableStateFlow<EventDetailsUIState>(EventDetailsUIState.Loading)
  val uiState: StateFlow<EventDetailsUIState> = _uiState.asStateFlow()
  private var currentUser: User? = null

  /** Loads event details by ID using EventRepository.getEvent. */
  fun loadEvent(eventId: String, context: Context) {
    viewModelScope.launch {
      try {
        val event = db.eventRepo.getEvent(eventId)
        currentUser = db.userRepo.getCurrentUser()

        if (event != null) {
          _uiState.value = EventDetailsUIState.Success(event, isEnrolled = isEnrolled(event))
        } else {
          _uiState.value =
              EventDetailsUIState.Error(context.getString(R.string.error_event_not_found))
        }
      } catch (e: Exception) {
        _uiState.value = EventDetailsUIState.Error(context.getString(R.string.error_loading_event))
      }
    }
  }

  /**
   * Logic to enroll in an event. Either a redirection to separate signup page or use Firebase to
   * track enrollment
   */
  fun enrollInEvent(event: Event, context: Context) {
    viewModelScope.launch {
      try {
        if (isEnrolled(event)) {
          _uiState.value = EventDetailsUIState.Success(event, isEnrolled = true)
          return@launch
        }

        db.userRepo
            .subscribeToEvent(event.id)
            .fold(
                onSuccess = { loadEvent(event.id, context) },
                onFailure = { _ ->
                  _uiState.value =
                      EventDetailsUIState.Error(context.getString(R.string.error_enroll_failed))
                })
      } catch (_: Exception) {
        _uiState.value = EventDetailsUIState.Error(context.getString(R.string.error_enroll_failed))
      }
    }
  }

  fun unenrollFromEvent(event: Event, context: Context) {
    viewModelScope.launch {
      try {
        if (!isEnrolled(event)) {
          _uiState.value = EventDetailsUIState.Success(event, isEnrolled = false)
          return@launch
        }

        db.userRepo
            .unsubscribeFromEvent(event.id)
            .fold(
                onSuccess = { loadEvent(event.id, context) },
                onFailure = { _ ->
                  _uiState.value =
                      EventDetailsUIState.Error(context.getString(R.string.error_unenroll_failed))
                })
      } catch (_: Exception) {
        _uiState.value =
            EventDetailsUIState.Error(context.getString(R.string.error_unenroll_failed))
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
