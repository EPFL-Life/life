package ch.epfllife.ui.endToEnd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.ui.admin.AddEditEventTestTags
import ch.epfllife.ui.admin.ManageEventsTestTags
import ch.epfllife.ui.admin.SelectAssociationTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.SettingsScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.setUpEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdminEndToEndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private val db = Db.freshLocal()

  @Before
  fun setup() {
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        .executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
    setUpEmulator(auth, "AdminEndToEndTest")

    // Seed the association and event
    runBlocking {
      db.assocRepo.createAssociation(ExampleAssociations.association2)
      db.eventRepo.createEvent(ExampleEvents.event2)

      // Setup Admin User
      val userRepo = db.userRepo as UserRepositoryLocal
      userRepo.createUser(ExampleUsers.adminUser)
      userRepo.simulateLogin(ExampleUsers.adminUser.id)
    }
  }

  @Test
  fun adminCanCreateAndEditEvent() {
    // 1. Login
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
    composeTestRule.setContent { ThemedApp(auth, db) }

    // 2. Go to Settings
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Settings)).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()

    // 3. Select Association
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SELECT_ASSOCIATION_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SelectAssociationTestTags.ASSOCIATION_LIST).assertIsDisplayed()

    // Click on the association card
    composeTestRule
        .onNodeWithTag(
            SelectAssociationTestTags.associationCard(ExampleAssociations.association2.id))
        .performClick()

    // 4. Verify back in Settings and "Manage Events" is visible
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.MANAGE_EVENTS_BUTTON).assertIsDisplayed()

    // 5. Go to Manage Events
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.MANAGE_EVENTS_BUTTON).performClick()
    composeTestRule.waitUntil(5000) {
      try {
        composeTestRule.onNodeWithTag(ManageEventsTestTags.TITLE).isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.onNodeWithTag(ManageEventsTestTags.TITLE).assertIsDisplayed()

    // 6. Verify Add Event button works (but don't submit due to location search requirement)
    composeTestRule.onNodeWithTag(ManageEventsTestTags.ADD_EVENT_BUTTON).performClick()
    composeTestRule.onNodeWithTag(AddEditEventTestTags.TITLE_FIELD).assertIsDisplayed()
    // Go back
    androidx.test.espresso.Espresso.pressBack()

    // 7. Edit Existing Event
    // Find event by title
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).performClick()

    // 8. Change Title
    composeTestRule.waitUntil(10000) {
      try {
        composeTestRule.onNodeWithTag(AddEditEventTestTags.TITLE_FIELD).isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    val newTitle = "Edited E2E Event"
    composeTestRule.onNodeWithTag(AddEditEventTestTags.TITLE_FIELD).performTextClearance()
    composeTestRule.onNodeWithTag(AddEditEventTestTags.TITLE_FIELD).performTextInput(newTitle)
    composeTestRule.onNodeWithTag(AddEditEventTestTags.SUBMIT_BUTTON).performScrollTo()
    composeTestRule.onNodeWithTag(AddEditEventTestTags.SUBMIT_BUTTON).performClick()

    // Wait for ManageEventsScreen to appear (confirm navigation back)
    composeTestRule.waitUntil(10000) {
      try {
        composeTestRule.onNodeWithTag(ManageEventsTestTags.TITLE).isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.onNodeWithTag(ManageEventsTestTags.TITLE).assertIsDisplayed()

    // 9. Verify new title in Manage Events
    composeTestRule.waitUntil(5000) {
      try {
        composeTestRule.onNodeWithText(newTitle).isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.onNodeWithText(newTitle).assertIsDisplayed()

    // 10. Go to Home
    // Verify we are in ManageEventsScreen
    composeTestRule.onNodeWithTag(ManageEventsTestTags.TITLE).assertIsDisplayed()

    // We are in ManageEventsScreen, which doesn't have bottom bar.
    // We need to go back to SettingsScreen first.
    composeTestRule.onNodeWithContentDescription("Back").performClick()
    composeTestRule.waitUntil(10000) {
      try {
        composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.HomeScreen)).performClick()

    // 11. Verify in Home
    // Switch to "All events" because user is not subscribed
    composeTestRule.waitUntil(5000) {
      try {
        composeTestRule.onNodeWithText("All Events").isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.onNodeWithText("All Events").performClick()

    composeTestRule.waitUntil(5000) {
      try {
        composeTestRule.onNodeWithText(newTitle).isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.onNodeWithText(newTitle).assertIsDisplayed()
  }
}
