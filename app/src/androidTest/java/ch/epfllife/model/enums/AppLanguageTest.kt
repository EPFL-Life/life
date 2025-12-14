package ch.epfllife.model.enums

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

  @Test
  fun mapsLocaleTagsAndFallsBackToSystem() {
    // localeTag round-trips
    assertEquals("system", AppLanguage.SYSTEM.localeTag)
    assertEquals("en", AppLanguage.ENGLISH.localeTag)
    assertEquals("fr", AppLanguage.FRENCH.localeTag)

    // fromTag returns correct enum for known tags
    assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag("system"))
    assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTag("en"))
    assertEquals(AppLanguage.FRENCH, AppLanguage.fromTag("fr"))

    // unknown or null tags fall back to SYSTEM
    assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag("es"))
    assertEquals(AppLanguage.SYSTEM, AppLanguage.fromTag(null))
  }
}
