package ch.epfllife.ui.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


/**
 * Represents the UI state for authentication.
 *
 * @property isLoading Whether an authentication operation is in progress.
 * @property user The currently signed-in [FirebaseUser], or null if not signed in.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property signedOut True if a sign-out operation has completed.
 */
data class AuthUIState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val errorMsg: String? = null,
    val signedOut: Boolean = false,
)

/**
 * ViewModel for the Sign-In view.
 *
 * @property auth The auth handler
 */
class SignInViewModel(private val auth: Auth) : ViewModel() {

  private val mutUiState = MutableStateFlow(AuthUIState())
  val uiState: StateFlow<AuthUIState> = mutUiState

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    mutUiState.update { it.copy(errorMsg = null) }
  }

  /** Initiates the Google sign-in flow and updates the UI state on success or failure. */
  fun signIn(context: Context) {
    if (mutUiState.value.isLoading) return

    viewModelScope.launch {
      mutUiState.update { it.copy(isLoading = true, errorMsg = null) }

      val signInResult = auth.signInFromContext(context)
      val newState =
          when (signInResult) {
            is SignInResult.Success -> {
              AuthUIState(
                  isLoading = false,
                  user = signInResult.user,
                  errorMsg = null,
                  signedOut = false,
              )
            }
            SignInResult.Cancelled -> {
              AuthUIState(
                  isLoading = false,
                  user = null,
                  errorMsg = context.getString(R.string.signin_cancelled_message),
                  signedOut = true,
              )
            }
            SignInResult.Failure -> {
              AuthUIState(
                  isLoading = false,
                  user = null,
                  errorMsg = context.getString(R.string.signin_failure_message),
                  signedOut = true,
              )
            }
          }
      mutUiState.update { newState }
    }
  }
}
