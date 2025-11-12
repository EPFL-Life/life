package ch.epfllife.ui.authentication

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepository
import ch.epfllife.model.user.UserRepositoryFirestore
import ch.epfllife.model.user.UserSettings
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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
 * @property userRepo The user repository to create/check user documents
 */
class SignInViewModel(
    private val auth: Auth,
    // Add UserRepository, providing a default instance just like in your other ViewModels
    private val userRepo: UserRepository = UserRepositoryFirestore(FirebaseFirestore.getInstance())
) : ViewModel() {

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
              // Logic to make sure there is a user object for the user signing in otherwise create
              // a new user object
              val firebaseUser = signInResult.user
              // Check if the user document already exists in Firestore
              if (userRepo.getUser(firebaseUser.uid) == null) {
                // It's a new user, create their document
                val newUser =
                    User(
                        id = firebaseUser.uid, // Use Auth UID as document ID
                        name = firebaseUser.displayName ?: "New User",
                        subscriptions = emptyList(),
                        enrolledEvents = emptyList(),
                        userSettings = UserSettings())
                try {
                  userRepo.createUser(newUser)
                } catch (e: Exception) {
                  // this should be handled properly but for now we just throw
                  throw java.lang.Exception("Problem creating new User on first sign-in")
                }
              }

              AuthUIState(
                  isLoading = false,
                  user = firebaseUser,
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
