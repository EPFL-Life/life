package ch.epfllife.ui.authentication

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
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignInScreenTest {
  @get:Rule val composeTestRule = createComposeRule()
  val auth = Auth(FakeCredentialManager.withDefaultTestUser)

  @Before fun setUp() = setUpEmulatorAuth(auth, "SignInScreenTest")

  @Test
  fun contentIsDisplayed() {
    composeTestRule.setContent { SignInScreen(auth, onSignedIn = {}) }
    listOf(
            NavigationTestTags.SIGN_IN_SCREEN,
            SignInScreenTestTags.SIGN_IN_APP_LOGO,
            SignInScreenTestTags.SIGN_IN_TITLE,
            SignInScreenTestTags.SIGN_IN_BUTTON,
            SignInScreenTestTags.GOOGLE_LOGO,
        )
        .map(composeTestRule::assertTagIsDisplayed)
  }

  @Test
  fun canSignIn() {
    Assert.assertNull(Firebase.auth.currentUser)
    var clicked = false
    composeTestRule.setContent { SignInScreen(auth, onSignedIn = { clicked = true }) }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).performClick()
    composeTestRule.waitUntil(5000) { clicked }
    Assert.assertNotNull(Firebase.auth.currentUser)
  }
}
