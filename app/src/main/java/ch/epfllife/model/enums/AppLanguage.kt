package ch.epfllife.model.enums

enum class AppLanguage(val localeTag: String) {
  SYSTEM("system"),
  ENGLISH("en"),
  FRENCH("fr");

  companion object {
    fun fromTag(tag: String?): AppLanguage = values().find { it.localeTag == tag } ?: SYSTEM
  }
}
