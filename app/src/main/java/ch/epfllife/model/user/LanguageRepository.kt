package ch.epfllife.model.user

import ch.epfllife.model.enums.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanguageRepository(private val userRepository: UserRepository) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val _languageFlow = MutableStateFlow(AppLanguage.SYSTEM)

  /** Emits the current user's language, defaulting to SYSTEM if no user is logged in. */
  val languageFlow: StateFlow<AppLanguage> = _languageFlow.asStateFlow()

  init {
    scope.launch { loadInitialLanguage() }
  }

  private suspend fun loadInitialLanguage() {
    val user = userRepository.getCurrentUser()
    _languageFlow.value = user?.userSettings?.language ?: AppLanguage.SYSTEM
  }

  /** Updates the currently logged in user's language. */
  suspend fun setLanguage(language: AppLanguage) {
    val user = userRepository.getCurrentUser() ?: return
    val updatedUser = user.copy(userSettings = user.userSettings.copy(language = language))
    userRepository.updateUser(user.id, updatedUser)
    _languageFlow.value = language
  }
}
