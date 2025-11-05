package ch.epfllife.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.setUpEmulatorAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  val auth = Auth(FakeCredentialManager.withDefaultTestUser)

  @Before
  fun setUp() {
    setUpEmulatorAuth(auth, "SettingsScreenTest")
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult.isSuccess)
    }
  }

  @Test
  fun contentIsDisplayed() {
    composeTestRule.setContent { SettingsScreen(auth = auth, onSignedOut = {}) }
    listOf(
            NavigationTestTags.SETTINGS_SCREEN,
            SettingsScreenTestTags.SIGN_OUT_BUTTON,
        )
        .map(composeTestRule::assertTagIsDisplayed)
  }

  @Test
  fun canSignOut() {
    Assert.assertNotNull(Firebase.auth.currentUser)
    var clicked = false
    composeTestRule.setContent { SettingsScreen(auth = auth, onSignedOut = { clicked = true }) }
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SIGN_OUT_BUTTON).performClick()
    composeTestRule.waitUntil(5000) { clicked }
    Assert.assertNull(Firebase.auth.currentUser)
  }
}
