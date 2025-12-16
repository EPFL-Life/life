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
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.ui.association.AssociationDetailsTestTags
import ch.epfllife.ui.authentication.SignInScreenTestTags
import ch.epfllife.ui.composables.AssociationCardTestTags
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.composables.EventCardTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.SettingsScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.navigateToAssociation
import ch.epfllife.utils.setUpEmulator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EndToEndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private val db = Db.firestore

  @Before
  fun setup() {
    // CI tests sometimes fail because of an open system dialog
    // (likely caused through the fact that it runs too slow)
    // This should fix it, by closing open system dialogs before starting the test.
    // Source:
    // https://stackoverflow.com/questions/39457305/android-testing-waited-for-the-root-of-the-view-hierarchy-to-have-window-focus
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        .executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
    setUpEmulator(auth, "EndToEndTest")
  }

  fun useLoggedInApp() {
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
    composeTestRule.setContent { ThemedApp(auth, db) }
  }

  fun useLoggedOutApp() {
    composeTestRule.setContent { ThemedApp(auth, db) }
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
  fun canExitWithDoubleBackPressFromCalendar() {
    useLoggedInApp()
    canExitWithDoublePressFromTab(Tab.Calendar)
  }

  @Test
  fun canSignInAndOutAgain() {
    useLoggedOutApp()
    Assert.assertNull(Firebase.auth.currentUser)
    composeTestRule.onNodeWithTag(NavigationTestTags.SIGN_IN_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).performClick()
    composeTestRule.waitUntil(5000) {
      composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).isDisplayed()
    }
    Assert.assertNotNull(Firebase.auth.currentUser)
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SIGN_OUT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SIGN_IN_SCREEN).assertIsDisplayed()
    Assert.assertNull(Firebase.auth.currentUser)
  }

  private fun canExitWithDoublePressFromTab(tab: Tab) {
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
    Espresso.pressBack()
    assertBackPressWouldExit()
  }

  @Test
  fun subscribeToAssociation() = runTest {
    // Setup
    useLoggedInApp()
    val association = ExampleAssociations.association1
    val event = ExampleEvents.event1
    db.userRepo.createUser(ExampleUsers.user1.copy(id = Firebase.auth.currentUser!!.uid))
    db.assocRepo.createAssociation(association)
    db.eventRepo.createEvent(event)

    // Subscribe
    composeTestRule.navigateToAssociation(association.id)
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    // If we leave too quickly, the coroutine gets cancelled
    Thread.sleep(300)

    // Verify subscription in association browser
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).performClick()
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil {
      composeTestRule
          .onNodeWithTag(AssociationCardTestTags.getAssociationCardTestTag(association.id))
          .isDisplayed()
    }

    // Verify subscription in home screen by viewing this association's events
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB).performClick()
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil {
      composeTestRule.onNodeWithTag(EventCardTestTags.getEventCardTestTag(event.id)).isDisplayed()
    }
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
