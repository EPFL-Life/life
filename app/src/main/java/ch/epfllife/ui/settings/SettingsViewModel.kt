package ch.epfllife.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.association.AssociationRepository
import ch.epfllife.model.authentication.Auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(val signInState: SignInState)

sealed class SignInState {
  data object SignedIn : SignInState()

  data object SignedOut : SignInState()
}

class SettingsViewModel(
    private val auth: Auth,
    private val associationRepository: AssociationRepository
) : ViewModel() {
  private val mutUiState = MutableStateFlow(UiState(signInState = SignInState.SignedIn))
  val uiState: StateFlow<UiState> = mutUiState.asStateFlow()

  fun signOut() {
    auth.signOut()
    mutUiState.value = mutUiState.value.copy(signInState = SignInState.SignedOut)
  }

  fun deleteAssociation(
      associationId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    viewModelScope.launch {
      try {
        val result = associationRepository.deleteAssociation(associationId)
        if (result.isSuccess) {
          onSuccess()
        } else {
          onFailure(Exception("Failed to delete association"))
        }
      } catch (e: Exception) {
        onFailure(e)
      }
    }
  }
}
