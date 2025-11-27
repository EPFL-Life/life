package ch.epfllife.ui.association

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.R
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AssociationDetailsUIState {
  object Loading : AssociationDetailsUIState()

  data class Success(val association: Association, val events: List<Event>) :
      AssociationDetailsUIState()

  data class Error(val message: String) : AssociationDetailsUIState()
}

class AssociationDetailsViewModel(private val db: Db) : ViewModel() {

  private val _uiState =
      MutableStateFlow<AssociationDetailsUIState>(AssociationDetailsUIState.Loading)
  val uiState: StateFlow<AssociationDetailsUIState> = _uiState.asStateFlow()

  /** Loads association details by ID using AssociationRepository.getAssociation. */
  fun loadAssociation(associationId: String, context: Context) {
    viewModelScope.launch {
      try {
        val association = db.assocRepo.getAssociation(associationId)
        if (association != null) {
          val events = db.assocRepo.getEventsForAssociation(associationId)
          _uiState.value =
              AssociationDetailsUIState.Success(association, events.getOrNull() ?: emptyList())
        } else {
          _uiState.value =
              AssociationDetailsUIState.Error(
                  context.getString(R.string.error_association_not_found))
        }
      } catch (_: Exception) {
        _uiState.value =
            AssociationDetailsUIState.Error(context.getString(R.string.error_loading_association))
      }
    }
  }
}
