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

  data class Success(
      val association: Association,
      val events: List<Event>,
      val enrolledEvents: List<String>,
      val isSubscribed: Boolean,
  ) : AssociationDetailsUIState()

  data class Error(val message: String) : AssociationDetailsUIState()
}

/**
 * ViewModel for the AssociationDetails screen.
 *
 * Uses `AssociationRepository` to fetch an Association by id.
 */
class AssociationDetailsViewModel(
    private val db: Db,
) : ViewModel() {

  private val _uiState =
      MutableStateFlow<AssociationDetailsUIState>(AssociationDetailsUIState.Loading)
  val uiState: StateFlow<AssociationDetailsUIState> = _uiState.asStateFlow()

  /** Loads association details by ID using AssociationRepository.getAssociation. */
  fun loadAssociation(associationId: String, context: Context) {
    viewModelScope.launch {
      try {
        val association = db.assocRepo.getAssociation(associationId)
        val eventsResult = db.assocRepo.getEventsForAssociation(associationId)
        val currentUser = db.userRepo.getCurrentUser()
        val enrolledEventIds = currentUser?.enrolledEvents ?: emptyList()

        if (association != null) {
          val events = eventsResult.getOrElse { emptyList() }
          val isSubscribed = currentUser?.subscriptions?.contains(associationId) ?: false
          _uiState.value =
              AssociationDetailsUIState.Success(
                  association = association,
                  events = events,
                  enrolledEvents = enrolledEventIds,
                  isSubscribed = isSubscribed)
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

  fun subscribeToAssociation(associationId: String, context: Context) {
    viewModelScope.launch {
      val currentState = _uiState.value
      if (currentState is AssociationDetailsUIState.Success) {
        // Optimistic update
        _uiState.value = currentState.copy(isSubscribed = true)

        db.userRepo
            .subscribeToAssociation(associationId)
            .onFailure {
              // Revert on failure
              _uiState.value = currentState.copy(isSubscribed = false)
              // Optionally show an error message (could use a separate error channel/state)
            }
            .onSuccess {
              // Refresh data to ensure consistency
              loadAssociation(associationId, context)
            }
      }
    }
  }

  fun unsubscribeFromAssociation(associationId: String, context: Context) {
    viewModelScope.launch {
      val currentState = _uiState.value
      if (currentState is AssociationDetailsUIState.Success) {
        // Optimistic update
        _uiState.value = currentState.copy(isSubscribed = false)

        db.userRepo
            .unsubscribeFromAssociation(associationId)
            .onFailure {
              // Revert on failure
              _uiState.value = currentState.copy(isSubscribed = true)
            }
            .onSuccess {
              // Refresh data to ensure consistency
              loadAssociation(associationId, context)
            }
      }
    }
  }
}
