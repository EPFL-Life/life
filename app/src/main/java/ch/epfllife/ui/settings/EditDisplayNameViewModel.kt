package ch.epfllife.ui.settings

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface EditDisplayNameUiState {
  object Loading : EditDisplayNameUiState

  object Success : EditDisplayNameUiState

  data class Error(val messageRes: Int) : EditDisplayNameUiState
}

class EditDisplayNameViewModel(private val db: Db) : ViewModel() {

  private val userRepo = db.userRepo

  var displayName by mutableStateOf("")
    private set

  private var userSnapshot: User? = null

  private val _uiState = MutableStateFlow<EditDisplayNameUiState>(EditDisplayNameUiState.Loading)
  val uiState: StateFlow<EditDisplayNameUiState> = _uiState

  init {
    loadUser()
  }

  private fun loadUser() {
    viewModelScope.launch {
      val user = userRepo.getCurrentUser()
      if (user != null) {
        userSnapshot = user
        displayName = user.name
        _uiState.value = EditDisplayNameUiState.Success
      } else {
        Log.e("EditDisplayNameVM", "User not found")
        _uiState.value = EditDisplayNameUiState.Error(R.string.error_loading_user)
      }
    }
  }

  fun updateDisplayName(value: String) {
    displayName = value
  }

  fun isFormValid(): Boolean = displayName.trim().isNotBlank()

  fun submit(onSuccess: () -> Unit) {
    val user = userSnapshot ?: return
    if (!isFormValid()) return

    viewModelScope.launch {
      val updatedUser = user.copy(name = displayName.trim())

      userRepo
          .updateUser(user.id, updatedUser)
          .onSuccess { onSuccess() }
          .onFailure {
            Log.e("EditDisplayNameVM", "Failed to update name", it)
            _uiState.value = EditDisplayNameUiState.Error(R.string.error_updating_profile)
          }
    }
  }
}
