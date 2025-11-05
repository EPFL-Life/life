package ch.epfllife.ui.endToEnd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfllife.ThemedApp
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.authentication.SignInScreenTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.SettingsScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.FirebaseEmulator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EndToEndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)

  @Before
  fun setup() {
    // CI tests sometimes fail because of an open system dialog
    // (likely caused through the fact that it runs too slow)
    // This should fix it, by closing open system dialogs before starting the test.
    // Source:
    // https://stackoverflow.com/questions/39457305/android-testing-waited-for-the-root-of-the-view-hierarchy-to-have-window-focus
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        .executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
    assertTrue(
        "Firebase emulator must be running for local end-to-end tests",
        FirebaseEmulator.isRunning,
    )
    // Reset to signed out state
    val signOutResult = auth.signOut()
    assertTrue("Sign out must succeed", signOutResult.isSuccess)
  }

  fun useLoggedInApp() {
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      assertTrue("Sign in must succeed", signInResult.isSuccess)
    }
    composeTestRule.setContent { ThemedApp(auth) }
  }

  fun useLoggedOutApp() {
    composeTestRule.setContent { ThemedApp(auth) }
  }

  @Test
  fun canGoThroughAllTabs() {
    useLoggedInApp()
    Tab.tabs.forEach { tab ->
      composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
      composeTestRule
          .onNodeWithTag(NavigationTestTags.getScreenTestTagForTab(tab))
          .assertIsDisplayed()
    }
  }

  @Test
  fun canExitWithBackPressFromHome() {
    useLoggedInApp()
    assertBackPressWouldExit()
  }

  @Test
  fun canExitWithDoubleBackPressFromSettings() {
    useLoggedInApp()
    canExitWithDoublePressFromTab(Tab.Settings)
  }

  @Test
  fun canExitWithDoubleBackPressFromAssociationBrowser() {
    useLoggedInApp()
    canExitWithDoublePressFromTab(Tab.AssociationBrowser)
  }

  @Test
  fun canExitWithDoubleBackPressFromMyEvents() {
    useLoggedInApp()
    canExitWithDoublePressFromTab(Tab.MyEvents)
  }

  @Test
  fun canSignInAndOutAgain() {
    useLoggedOutApp()
    assertNull(Firebase.auth.currentUser)
    composeTestRule.onNodeWithTag(NavigationTestTags.SIGN_IN_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).isDisplayed()
    }
    assertNotNull(Firebase.auth.currentUser)
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SIGN_OUT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SIGN_IN_SCREEN).assertIsDisplayed()
    assertNull(Firebase.auth.currentUser)
  }

  private fun canExitWithDoublePressFromTab(tab: Tab) {
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
    Espresso.pressBack()
    assertBackPressWouldExit()
  }

  private fun assertBackPressWouldExit() {
    // In theory we should be able to detect that the app closes with
    // - `composeTestRule.activityRule.scenario.state == Lifecycle.State.DESTROYED`
    // - `composeTestRule.activity.isFinishing`
    // In practice, that does not seems to work,
    // so instead we abuse the fact that Espresso.pressBack() throws in case the app would be
    // closed.
    try {
      Espresso.pressBack()
    } catch (e: NoActivityResumedException) {
      // NOTE: The documentation for Espresso.pressBack() states
      // that a `PerformException` is thrown,
      // but actually it is a `NoActivityResumedException`.
      return
    }
    throw AssertionError("Expected app to exit on back press, but it did not.")
  }
}
