package ch.epfllife.ui.settings

import androidx.lifecycle.ViewModel
import ch.epfllife.model.authentication.Auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UiState(val signInState: SignInState)

sealed class SignInState {
  data object SignedIn : SignInState()

  data object SignedOut : SignInState()
}

class SettingsViewModel(private val auth: Auth) : ViewModel() {
  private val mutUiState = MutableStateFlow(UiState(signInState = SignInState.SignedIn))
  val uiState: StateFlow<UiState> = mutUiState.asStateFlow()

  fun signOut() {
    auth.signOut()
    mutUiState.value = mutUiState.value.copy(signInState = SignInState.SignedOut)
  }
}
