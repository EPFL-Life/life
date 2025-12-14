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
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.Association
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.LanguageRepository
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.ui.admin.AddEditEventTestTags
import ch.epfllife.ui.admin.AssociationAdminScreenTestTags
import ch.epfllife.ui.admin.ManageEventsTestTags
import ch.epfllife.ui.admin.SelectAssociationTestTags
import ch.epfllife.ui.composables.AssociationCardTestTags
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.SettingsScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.navigateToTab
import ch.epfllife.utils.setUpEmulator
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AdminEndToEndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private lateinit var db: Db

  @Before
  fun setup() {
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        .executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
    setUpEmulator(auth, "AdminEndToEndTest")
    db = Db.freshLocal()

    // Seed the association and event
    runTest {
      db.assocRepo.createAssociation(ExampleAssociations.association2)
      db.eventRepo.createEvent(ExampleEvents.event2)

      // Login
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
    val languageRepository = LanguageRepository(db.userRepo)
    composeTestRule.setContent { ThemedApp(auth, db, languageRepository) }
  }

  private fun loginAsAdmin(): User {
    val adminUser = ExampleUsers.adminUser
    runTest {
      val userRepo = db.userRepo as UserRepositoryLocal
      userRepo.createUser(adminUser)
      userRepo.simulateLogin(adminUser.id)
    }
    return adminUser
  }

  private fun loginAsAssocAdmin(assoc: Association): User {
    val assocAdminUser =
        ExampleUsers.associationAdminUser.copy(managedAssociationIds = listOf(assoc.id))
    runTest {
      val userRepo = db.userRepo as UserRepositoryLocal
      userRepo.createUser(assocAdminUser)
      userRepo.simulateLogin(assocAdminUser.id)
    }
    return assocAdminUser
  }

  @Test
  fun adminCanCreateAndEditEvent() {
    // 1. Login
    loginAsAdmin()

    // 2. Go to Settings
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Settings)).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()

    // 3. Select Association
    // Go to Admin Console
    composeTestRule.waitUntil(10000) {
      try {
        composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(AssociationAdminScreenTestTags.SCREEN).assertIsDisplayed()

    // Select Association inside Admin Console
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON)
        .performClick()
    composeTestRule.onNodeWithTag(SelectAssociationTestTags.ASSOCIATION_LIST).assertIsDisplayed()

    // Click on the association card
    composeTestRule
        .onNodeWithTag(
            AssociationCardTestTags.getAssociationCardTestTag(ExampleAssociations.association2.id))
        .performClick()

    // 4. Verify back in Admin Console and "Manage Events" is visible
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON)
        .assertIsDisplayed()

    // 5. Go to Manage Events
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON)
        .performClick()
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
        composeTestRule.onNodeWithTag(AssociationAdminScreenTestTags.SCREEN).isDisplayed()
      } catch (e: Exception) {
        false
      }
    }
    composeTestRule.onNodeWithTag(AssociationAdminScreenTestTags.SCREEN).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.BACK_BUTTON)
        .performClick() // Back to Settings

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

  @Test
  fun createEventAsAssocAdmin() {
    val assoc = ExampleAssociations.association2
    val eventTitle = "New Event"
    loginAsAssocAdmin(assoc)

    // Select association to manage
    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON)
        .performClick()
    composeTestRule
        .onNodeWithTag(AssociationCardTestTags.getAssociationCardTestTag(assoc.id))
        .performClick()
    composeTestRule.waitForIdle() // automatic navigation back to settings

    // Create new event
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON)
        .performClick()
    composeTestRule.onNodeWithTag(ManageEventsTestTags.ADD_EVENT_BUTTON).performClick()
    // add location first, otherwise we have a few problems with injecting tough input
    composeTestRule.onNodeWithTag(AddEditEventTestTags.LOCATION_FIELD).performTextInput("EPFL")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddEditEventTestTags.TITLE_FIELD).performTextInput(eventTitle)
    composeTestRule
        .onNodeWithTag(AddEditEventTestTags.DESCRIPTION_FIELD)
        .performTextInput("Event Description")
    Espresso.closeSoftKeyboard()
    composeTestRule.onNodeWithTag(AddEditEventTestTags.TIME_PICKER_BOX).performClick()
    // Interact with DatePicker (Click OK to accept default/current date)
    onView(withText("OK")).perform(click())
    // Interact with TimePicker (Click OK to accept default/current time)
    onView(withText("OK")).perform(click())
    composeTestRule.onNodeWithTag(AddEditEventTestTags.SUBMIT_BUTTON).performScrollTo()
    composeTestRule.onNodeWithTag(AddEditEventTestTags.SUBMIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify event created on manage events screen
    composeTestRule.waitUntil { composeTestRule.onNodeWithText(eventTitle).isDisplayed() }

    // Verify event created on home screen
    composeTestRule.onNodeWithTag(ManageEventsTestTags.BACK_BUTTON).performClick()
    composeTestRule.onNodeWithTag(AssociationAdminScreenTestTags.BACK_BUTTON).performClick()
    composeTestRule.navigateToTab(Tab.HomeScreen)
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil { composeTestRule.onNodeWithText(eventTitle).isDisplayed() }
  }
}
