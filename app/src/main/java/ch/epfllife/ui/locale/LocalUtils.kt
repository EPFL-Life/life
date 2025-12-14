package ch.epfllife.ui.locale

import ch.epfllife.model.enums.AppLanguage
import java.util.Locale

object LocaleUtils {

  fun resolve(appLanguage: AppLanguage): Locale {
    return when (appLanguage) {
      AppLanguage.ENGLISH -> Locale.ENGLISH
      AppLanguage.FRENCH -> Locale.FRENCH
      AppLanguage.SYSTEM -> {
        when (Locale.getDefault().language.lowercase()) {
          "fr" -> Locale.FRENCH
          "en" -> Locale.ENGLISH
          else -> Locale.ENGLISH // fallback
        }
      }
    }
  }
}
