package ch.epfllife.ui.settings

import android.net.Uri
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

sealed interface ManageProfileUiState {
  object Loading : ManageProfileUiState

  object Success : ManageProfileUiState

  data class Error(val messageRes: Int, val details: String? = null) : ManageProfileUiState
}

class ManageProfileViewModel(private val db: Db) : ViewModel() {

  private val userRepo = db.userRepo

  var displayName by mutableStateOf("")
    private set

  var photoUrl by mutableStateOf("")
    private set

  var isUploadingPhoto by mutableStateOf(false)
    private set

  private var userSnapshot: User? = null

  private val _uiState = MutableStateFlow<ManageProfileUiState>(ManageProfileUiState.Loading)
  val uiState: StateFlow<ManageProfileUiState> = _uiState

  init {
    loadUser()
  }

  private fun loadUser() {
    viewModelScope.launch {
      val user = userRepo.getCurrentUser()
      if (user != null) {
        userSnapshot = user
        displayName = user.name
        photoUrl = user.photoUrl ?: ""
        _uiState.value = ManageProfileUiState.Success
      } else {
        Log.e("ManageProfileVM", "User not found")
        _uiState.value = ManageProfileUiState.Error(R.string.error_loading_user)
      }
    }
  }

  fun updateDisplayName(value: String) {
    displayName = value
  }

  fun onPhotoSelected(uri: Uri) {
    val user = userSnapshot ?: return
    viewModelScope.launch {
      isUploadingPhoto = true
      try {
        Log.d("ManageProfileVM", "onPhotoSelected: $uri")
        userRepo
            .uploadUserImage(user.id, uri)
            .onSuccess { url -> photoUrl = url }
            .onFailure { e ->
              Log.e("ManageProfileVM", "Failed to upload photo", e)
              _uiState.value =
                  ManageProfileUiState.Error(
                      R.string.error_updating_profile, "Photo Upload Failed: ${e.message}")
            }
      } finally {
        isUploadingPhoto = false
      }
    }
  }

  fun isFormValid(): Boolean = displayName.trim().isNotBlank() && !isUploadingPhoto

  fun submit(onSuccess: () -> Unit) {
    val user = userSnapshot ?: return
    if (!isFormValid()) return

    viewModelScope.launch {
      val updatedUser = user.copy(name = displayName.trim(), photoUrl = photoUrl.ifBlank { null })

      userRepo
          .updateUser(user.id, updatedUser)
          .onSuccess { onSuccess() }
          .onFailure {
            Log.e("ManageProfileVM", "Failed to update profile", it)
            _uiState.value = ManageProfileUiState.Error(R.string.error_updating_profile)
          }
    }
  }
}
