package ch.epfllife.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface SelectAssociationUIState {
  object Loading : SelectAssociationUIState

  data class Success(val associations: List<Association>) : SelectAssociationUIState

  data class Error(val message: String) : SelectAssociationUIState
}

class SelectAssociationViewModel(private val db: Db) : ViewModel() {

  private val _uiState =
      MutableStateFlow<SelectAssociationUIState>(SelectAssociationUIState.Loading)
  val uiState: StateFlow<SelectAssociationUIState> = _uiState

  init {
    loadAssociations()
  }

  private fun loadAssociations() {
    viewModelScope.launch {
      try {
        val list = db.assocRepo.getAllAssociations()
        _uiState.value = SelectAssociationUIState.Success(list)
      } catch (e: Exception) {
        _uiState.value = SelectAssociationUIState.Error(e.message ?: "Unknown error")
      }
    }
  }

  fun reload(onComplete: (() -> Unit)? = null) {
    viewModelScope.launch {
      loadAssociations()
      onComplete?.invoke()
    }
  }
}
