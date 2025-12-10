package ch.epfllife.ui.settings

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.FakeToastHelper
import ch.epfllife.utils.SystemToastHelper
import ch.epfllife.utils.ToastHelper
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.setUpEmulator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private lateinit var decorView: View
  private val fakeToastHelper = FakeToastHelper()

  @Before
  fun setUp() {
    setUpEmulator(auth, "SettingsScreenTest")
    fakeToastHelper.lastMessage = null
    composeTestRule.waitForIdle()
    composeTestRule.activityRule.scenario.onActivity { activity ->
      decorView = activity.window.decorView
    }
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
  }

  // we do this extra helper function because it makes the tests more readable and allows to show
  // the focus on what is beeing tested
  // we do this extra helper function because it makes the tests more readable and allows to show
  // the focus on what is beeing tested
  private fun setSettingsScreenContent(
      onSignedOut: () -> Unit = {},
      onAdminConsoleClick: () -> Unit = {},
      toastHelper: ToastHelper = SystemToastHelper()
  ) {
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          associationRepository = Db.freshLocal().assocRepo,
          onSignedOut = onSignedOut,
          toastHelper = toastHelper,
          onAdminConsoleClick = onAdminConsoleClick)
    }
  }

  @Test
  fun contentIsDisplayed() {
    setSettingsScreenContent()
    listOf(
            NavigationTestTags.SETTINGS_SCREEN,
            SettingsScreenTestTags.SIGN_OUT_BUTTON,
            SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON)
        .map(composeTestRule::assertTagIsDisplayed)
  }

  @Test
  fun canSignOut() {
    Assert.assertNotNull(Firebase.auth.currentUser)
    var clicked = false
    setSettingsScreenContent(onSignedOut = { clicked = true }, toastHelper = fakeToastHelper)
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SIGN_OUT_BUTTON).performClick()
    composeTestRule.waitForIdle()
    Assert.assertNull(Firebase.auth.currentUser)
    Assert.assertEquals(
        composeTestRule.activity.getString(R.string.signout_successful),
        fakeToastHelper.lastMessage)
  }

  @Test
  fun adminConsoleButtonInvokesCallback() {
    var clicked = false
    setSettingsScreenContent(onAdminConsoleClick = { clicked = true })

    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).performClick()
    Assert.assertTrue(clicked)
  }
}
