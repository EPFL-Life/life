package ch.epfllife.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepository
import ch.epfllife.model.user.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ManageFriendsUiState {
  data object Loading : ManageFriendsUiState()

  data class Success(val users: List<User>) : ManageFriendsUiState()

  data class Error(val message: Int) : ManageFriendsUiState()
}

class ManageFriendsViewModel(private val userRepository: UserRepository) : ViewModel() {

  private val _uiState = MutableStateFlow<ManageFriendsUiState>(ManageFriendsUiState.Loading)
  val uiState: StateFlow<ManageFriendsUiState> = _uiState.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private var allUsers: List<User> = emptyList()

  init {
    loadUsers()
  }

  fun loadUsers() {
    viewModelScope.launch {
      _uiState.value = ManageFriendsUiState.Loading
      try {
        val currentUser = userRepository.getCurrentUser()
        if (currentUser == null) {
          _uiState.value = ManageFriendsUiState.Error(ch.epfllife.R.string.user_not_logged_in)
          return@launch
        }

        val users = userRepository.getAllUsers()

        // Filter:
        // 1. Exclude self
        // 2. Exclude ADMIN and ASSOCIATION_ADMIN !!!IMPORTANT TO KEEP IN MIND
        // 3. Keep only USER role (implicit)
        allUsers = users.filter { it.id != currentUser.id && it.role == UserRole.USER }

        filterUsers()
      } catch (_: Exception) {
        _uiState.value = ManageFriendsUiState.Error(ch.epfllife.R.string.error_loading_users)
      }
    }
  }

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
    filterUsers()
  }

  private fun filterUsers() {
    val query = _searchQuery.value
    val filtered =
        if (query.isBlank()) {
          allUsers
        } else {
          allUsers.filter { it.name.contains(query, ignoreCase = true) }
        }
    _uiState.value = ManageFriendsUiState.Success(filtered)
  }
}
