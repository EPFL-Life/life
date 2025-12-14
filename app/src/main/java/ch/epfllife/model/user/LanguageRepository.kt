package ch.epfllife.model.user

import ch.epfllife.model.enums.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class LanguageRepository(private val userRepository: UserRepository) {

  /** Emits the current user's language, defaulting to SYSTEM if no user is logged in. */
  val languageFlow: Flow<AppLanguage> =
      flow {
            while (true) {
              val user = userRepository.getCurrentUser()
              emit(user?.userSettings?.language ?: AppLanguage.SYSTEM)
              kotlinx.coroutines.delay(500) // poll every 500ms; adjust as needed
            }
          }
          .distinctUntilChanged()

  /** Updates the currently logged in user's language. */
  suspend fun setLanguage(language: AppLanguage) {
    val user = userRepository.getCurrentUser() ?: return
    val updatedUser = user.copy(userSettings = user.userSettings.copy(language = language))
    userRepository.updateUser(user.id, updatedUser)
  }
}
