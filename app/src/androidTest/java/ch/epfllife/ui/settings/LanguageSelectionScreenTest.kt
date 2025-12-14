package ch.epfllife.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.model.db.Db
import ch.epfllife.model.enums.AppLanguage
import ch.epfllife.model.user.LanguageRepository
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepositoryLocal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class LanguageSelectionScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun allLanguagesAreDisplayed() {
    val db = Db.freshLocal()
    val languageRepository = LanguageRepository(db.userRepo)

    // Arrange
    composeTestRule.setContent {
      LanguageSelectionScreen(languageRepository = languageRepository, onBack = {})
    }

    // Assert
    composeTestRule.onNodeWithTag(LanguageSelectionTags.SYSTEM).assertIsDisplayed()
    composeTestRule.onNodeWithTag(LanguageSelectionTags.ENGLISH).assertIsDisplayed()
    composeTestRule.onNodeWithTag(LanguageSelectionTags.FRENCH).assertIsDisplayed()
  }

  @Test
  fun selectingLanguageUpdatesRepositoryAndCallsOnBack() {
    val db = Db.freshLocal()
    val repo = db.userRepo as UserRepositoryLocal

    // Arrange
    // Create a user to ensure we can update settings
    runBlocking {
      repo.createUser(User(id = "test-user", name = "Test User"))
      repo.simulateLogin("test-user")
    }

    val languageRepository = LanguageRepository(repo)
    var backCalled = false

    // Arrange
    composeTestRule.setContent {
      LanguageSelectionScreen(
          languageRepository = languageRepository, onBack = { backCalled = true })
    }

    // Click English
    composeTestRule.onNodeWithTag(LanguageSelectionTags.ENGLISH).performClick()
    composeTestRule.waitForIdle()

    Assert.assertTrue("onBack should be called", backCalled)

    // Assert
    // Verify repository state
    runBlocking {
      val currentLang = languageRepository.languageFlow.first()
      Assert.assertEquals(AppLanguage.ENGLISH, currentLang)
    }
  }

  @Test
  fun selectingFrenchUpdatesRepository() {
    // 🇫🇷🇫🇷🇫🇷🇫🇷🇫🇷🇫🇷🇫🇷🇫🇷🇫🇷🇫🇷🇫🇷🇫🇷
    val db = Db.freshLocal()
    val repo = db.userRepo as UserRepositoryLocal

    // Arrange
    runBlocking {
      repo.createUser(User(id = "test-user", name = "Test User"))
      repo.simulateLogin("test-user")
    }

    val languageRepository = LanguageRepository(repo)

    composeTestRule.setContent {
      LanguageSelectionScreen(languageRepository = languageRepository, onBack = {})
    }

    // ACT
    composeTestRule.onNodeWithTag(LanguageSelectionTags.FRENCH).performClick()
    composeTestRule.waitForIdle()

    // Arrange
    runBlocking {
      val currentLang = languageRepository.languageFlow.first()
      Assert.assertEquals(AppLanguage.FRENCH, currentLang)
    }
  }
}
