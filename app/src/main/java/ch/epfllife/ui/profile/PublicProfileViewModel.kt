package ch.epfllife.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PublicProfileUiState {
  data object Loading : PublicProfileUiState()

  data class Success(val user: User, val isFollowing: Boolean) : PublicProfileUiState()

  data class Error(val message: Int) : PublicProfileUiState()
}

class PublicProfileViewModel(
    private val userRepository: UserRepository,
    private val profileUserId: String
) : ViewModel() {

  private val _uiState = MutableStateFlow<PublicProfileUiState>(PublicProfileUiState.Loading)
  val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()

  init {
    loadProfile()
  }

  fun loadProfile() {
    viewModelScope.launch {
      _uiState.value = PublicProfileUiState.Loading
      try {
        val user = userRepository.getUser(profileUserId)
        val currentUser = userRepository.getCurrentUser()

        if (user == null || currentUser == null) {
          _uiState.value =
              PublicProfileUiState.Error(ch.epfllife.R.string.error_user_not_found_or_logged_in)
          return@launch
        }

        val isFollowing = currentUser.following.contains(profileUserId)
        _uiState.value = PublicProfileUiState.Success(user, isFollowing)
      } catch (_: Exception) {
        _uiState.value = PublicProfileUiState.Error(ch.epfllife.R.string.error_loading_profile)
      }
    }
  }

  fun toggleFollow() {
    val currentState = _uiState.value
    if (currentState !is PublicProfileUiState.Success) return

    viewModelScope.launch {
      val isFollowing = currentState.isFollowing
      val result =
          if (isFollowing) {
            userRepository.unfollowUser(profileUserId)
          } else {
            userRepository.followUser(profileUserId)
          }

      result
          .onSuccess {
            // Refresh state to reflect change (e.g. reload current user to get updated following
            // list)
            loadProfile()
          }
          .onFailure {
            // TODO Ideally show error toast, for now just stay in same state or reload
            loadProfile()
          }
    }
  }
}
