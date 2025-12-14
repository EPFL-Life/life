package ch.epfllife.model.user

import ch.epfllife.model.enums.AppLanguage

data class UserSettings(
    val isDarkMode: Boolean = false,
    val language: AppLanguage = AppLanguage.SYSTEM
)
