package ch.epfllife.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ManageEventsUIState {
  object Loading : ManageEventsUIState

  data class Error(val message: String) : ManageEventsUIState

  data class Success(val events: List<Event>) : ManageEventsUIState
}

class ManageEventsViewModel(private val db: Db, private val associationId: String) : ViewModel() {

  private val _uiState = MutableStateFlow<ManageEventsUIState>(ManageEventsUIState.Loading)
  val uiState: StateFlow<ManageEventsUIState> = _uiState

  init {
    loadEvents()
  }

  private fun loadEvents() {
    viewModelScope.launch {
      try {
        val list = db.eventRepo.getAllEvents().filter { it.association.id == associationId }

        val sorted = list.sortedBy { it.time }
        _uiState.value = ManageEventsUIState.Success(sorted)
      } catch (e: Exception) {
        _uiState.value = ManageEventsUIState.Error(e.message ?: "Unknown error")
      }
    }
  }

  fun reload(onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      loadEvents()
      onComplete?.invoke()
    }
  }
}
