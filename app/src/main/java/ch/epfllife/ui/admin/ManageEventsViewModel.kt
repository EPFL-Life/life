package ch.epfllife.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ManageEventsUIState {
  object Loading : ManageEventsUIState

  data class Error(val messageRes: Int) : ManageEventsUIState

  data class Success(val events: List<Event>, val enrolledEvents: List<String>) :
      ManageEventsUIState
}

class ManageEventsViewModel(private val db: Db, val associationId: String) : ViewModel() {

  private val _uiState = MutableStateFlow<ManageEventsUIState>(ManageEventsUIState.Loading)
  val uiState: StateFlow<ManageEventsUIState> = _uiState

  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  init {
    viewModelScope.launch { loadEvents() }
  }

  fun reload(onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      loadEvents()
      onComplete?.invoke()
    }
  }

  private suspend fun loadEvents() {
    _uiState.value = ManageEventsUIState.Loading

    val result = db.assocRepo.getEventsForAssociation(associationId)
    val currentUser = db.userRepo.getCurrentUser()
    val enrolledEventIds = currentUser?.enrolledEvents ?: emptyList()

    result.fold(
        onSuccess = { events ->
          val today = LocalDate.now()
          val filtered =
              events
                  .filter { event ->
                    val eventDate = event.startDateOrNull()
                    eventDate == null || !eventDate.isBefore(today)
                  }
                  .sortedBy { it.startDateOrNull() ?: LocalDate.MAX }

          _uiState.value = ManageEventsUIState.Success(filtered, enrolledEvents = enrolledEventIds)
        },
        onFailure = { e ->
          Log.e("ManageEventsVM", "Failed to load events for association $associationId", e)
          _uiState.value = ManageEventsUIState.Error(R.string.error_loading_events_generic)
        })
  }

  private fun Event.startDateOrNull(): LocalDate? {
    if (time.length < 10) return null
    return try {
      LocalDate.parse(time.substring(0, 10), dateFormatter)
    } catch (_: Exception) {
      null
    }
  }
}
