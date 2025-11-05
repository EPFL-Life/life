package ch.epfllife.ui.settings

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.authentication.AuthRepository
import ch.epfllife.model.authentication.AuthRepositoryFirebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UiState(val signInState: SignInState)

sealed class SignInState {
  data object SignedIn : SignInState()

  data object SignedOut : SignInState()

  data class SignOutFailed(val message: String) : SignInState()
}

class SettingsViewModel(private val authRepository: AuthRepository = AuthRepositoryFirebase()) :
    ViewModel() {
  private val mutUiState = MutableStateFlow(UiState(signInState = SignInState.SignedIn))
  val uiState: StateFlow<UiState> = mutUiState.asStateFlow()

  fun signOut(credentialManager: CredentialManager) {
    viewModelScope.launch {
      authRepository
          .signOut()
          .fold(
              onSuccess = {
                mutUiState.value = mutUiState.value.copy(signInState = SignInState.SignedOut)
              },
              onFailure = { throwable ->
                mutUiState.value =
                    mutUiState.value.copy(
                        signInState =
                            SignInState.SignOutFailed(
                                throwable.localizedMessage ?: "Sign out failed"))
              },
          )
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
  }
}
