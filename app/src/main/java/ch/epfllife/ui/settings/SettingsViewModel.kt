package ch.epfllife.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(val signInState: SignInState, val userRole: UserRole = UserRole.USER)

sealed class SignInState {
  data object SignedIn : SignInState()

  data object SignedOut : SignInState()
}

class SettingsViewModel(private val auth: Auth, private val db: Db) : ViewModel() {
  private val mutUiState = MutableStateFlow(UiState(signInState = SignInState.SignedIn))
  val uiState: StateFlow<UiState> = mutUiState.asStateFlow()

  init {
    viewModelScope.launch {
      val user = db.userRepo.getCurrentUser()
      if (user != null) {
        mutUiState.value = mutUiState.value.copy(userRole = user.role)
      }
    }
  }

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
        val result = db.assocRepo.deleteAssociation(associationId)
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
