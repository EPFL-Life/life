package ch.epfllife.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.model.enums.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LanguageProviderTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun languageProviderAppliesLocaleToContext() {
    var localeLanguage: String? = null

    composeTestRule.setContent {
      LanguageProvider(language = AppLanguage.FRENCH) {
        CaptureLocale { lang -> localeLanguage = lang }
      }
    }

    composeTestRule.waitForIdle()
    assertEquals("fr", localeLanguage)
  }
}

@Composable
private fun CaptureLocale(onCaptured: (String) -> Unit) {
  val ctx = LocalContext.current
  val lang = ctx.resources.configuration.locales.get(0).language
  onCaptured(lang)
}
