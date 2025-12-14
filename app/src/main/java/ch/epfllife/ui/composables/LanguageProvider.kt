package ch.epfllife.ui.composables

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import ch.epfllife.model.enums.AppLanguage
import ch.epfllife.ui.locale.LocaleUtils
import java.util.Locale

/**
 * Wraps the app content with a context that has the correct locale. Defaults to SYSTEM locale
 * unless the user explicitly selects another language.
 */
@Composable
fun LanguageProvider(language: AppLanguage, content: @Composable () -> Unit) {
  val baseContext = LocalContext.current
  val localizedContext = remember(language) { baseContext.updateLocale(language) }

  CompositionLocalProvider(LocalContext provides localizedContext) { content() }
}

/** Returns a Context with the specified locale applied. */
private fun Context.updateLocale(language: AppLanguage): Context {
  val locale: Locale = LocaleUtils.resolve(language)

  // Set default for Java/Compose
  Locale.setDefault(locale)

  // Update Android Configuration
  val config = Configuration(resources.configuration)
  config.setLocale(locale)

  return createConfigurationContext(config)
}
