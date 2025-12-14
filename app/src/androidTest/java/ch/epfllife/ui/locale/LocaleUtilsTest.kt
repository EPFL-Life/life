package ch.epfllife.ui.locale

import ch.epfllife.model.enums.AppLanguage
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleUtilsTest {

  @Test
  fun resolvesDirectLanguages() {
    assertEquals(Locale.ENGLISH, LocaleUtils.resolve(AppLanguage.ENGLISH))
    assertEquals(Locale.FRENCH, LocaleUtils.resolve(AppLanguage.FRENCH))
  }

  @Test
  fun resolvesSystemLanguageBasedOnDefault() {
    val original = Locale.getDefault()
    try {
      Locale.setDefault(Locale.FRENCH)
      assertEquals(Locale.FRENCH, LocaleUtils.resolve(AppLanguage.SYSTEM))

      Locale.setDefault(Locale("es"))
      assertEquals(Locale.ENGLISH, LocaleUtils.resolve(AppLanguage.SYSTEM))
    } finally {
      Locale.setDefault(original)
    }
  }
}
