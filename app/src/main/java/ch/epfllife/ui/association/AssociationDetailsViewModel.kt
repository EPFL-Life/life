package ch.epfllife.ui.association

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.association.Association
import ch.epfllife.model.association.AssociationRepository
import ch.epfllife.model.association.AssociationRepositoryFirestore
import ch.epfllife.model.event.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AssociationDetailsUIState {
  object Loading : AssociationDetailsUIState()

  data class Success(val association: Association, val events: List<Event>?) :
      AssociationDetailsUIState()

  data class Error(val message: String) : AssociationDetailsUIState()
}

class AssociationDetailsViewModel(
    private val repo: AssociationRepository =
        AssociationRepositoryFirestore(
            com.google.firebase.firestore.FirebaseFirestore.getInstance())
) : ViewModel() {

  private val _uiState =
      MutableStateFlow<AssociationDetailsUIState>(AssociationDetailsUIState.Loading)
  val uiState: StateFlow<AssociationDetailsUIState> = _uiState.asStateFlow()

  /** Loads association details by ID using AssociationRepository.getAssociation. */
  fun loadAssociation(associationId: String) {
    viewModelScope.launch {
      try {
        val association = repo.getAssociation(associationId)
        if (association != null) {
          val events = repo.getEventsForAssociation(associationId)
          _uiState.value = AssociationDetailsUIState.Success(association, events.getOrNull())
        } else {
          _uiState.value = AssociationDetailsUIState.Error("Association not found")
        }
      } catch (e: Exception) {
        _uiState.value = AssociationDetailsUIState.Error("Failed to load association: ${e.message}")
      }
    }
  }
}
