package ch.epfllife.ui.endToEnd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.composables.EventCardTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.profile.PublicProfileTestTags
import ch.epfllife.ui.settings.ManageFriendsTestTags
import ch.epfllife.ui.settings.SettingsScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.setUpEmulator
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FriendSystemEndToEndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private lateinit var db: Db

  @Before
  fun setup() {
    // Running this command before tests ensures a clean slate by forcibly closing these popups so
    // the test can interact with your app without obstruction.
    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        .executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
    setUpEmulator(auth, "FriendSystemEndToEndTest")
    db = Db.freshLocal()
  }

  @Test
  fun testFriendSystemFlow() {
    // 1. Setup Data
    val currentUser = ExampleUsers.user1 // "Alice", ID "1"
    val event = ExampleEvents.event1
    // Friend is enrolled in the event
    val friendUser = ExampleUsers.user2.copy(enrolledEvents = listOf(event.id))

    runTest {
      val userRepo = db.userRepo as UserRepositoryLocal
      val eventRepo = db.eventRepo

      // key: Ensure currentUser has NO friends initially
      userRepo.createUser(currentUser.copy(following = emptyList()))
      userRepo.createUser(friendUser)

      eventRepo.createEvent(event)

      // Login
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
      userRepo.simulateLogin(currentUser.id)
    }

    composeTestRule.setContent { ThemedApp(auth, db) }

    // 2. Navigate to Settings -> Manage Friends
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Settings)).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()

    // Click "Manage Friends"
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.MANAGE_FRIENDS_BUTTON).performClick()

    // Verify Manage Friends Screen
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag(ManageFriendsTestTags.USER_LIST).isDisplayed()
    }

    // 3. Find Friend and Follow
    // Should see "Bob"
    composeTestRule.onNodeWithText(friendUser.name).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText(friendUser.name).performClick()

    // Verify Public Profile
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).isDisplayed()
    }

    // Click Follow
    composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).assertTextEquals("Follow")
    composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).performClick()

    // Wait for "Unfollow"
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onNode(hasTestTag(PublicProfileTestTags.FOLLOW_BUTTON).and(hasText("Unfollow")))
          .isDisplayed()
    }

    // 4. Navigate to Event Details to check attendee list
    // Go back to Users List
    composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).performClick()
    // Go back to Settings
    composeTestRule.onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).performClick()

    // Go to Event Browser (Home Screen) to find the event
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.HomeScreen)).performClick()

    // Switch to "All events" to make sure the event is visible (user might not be subscribed)
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    // Find Event Card
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag(EventCardTestTags.getEventCardTestTag(event.id)).isDisplayed()
    }
    composeTestRule.onNodeWithTag(EventCardTestTags.getEventCardTestTag(event.id)).performClick()

    // Click Attendees Button (found by text "1 attending" since friend is only attendee)
    // We might need to wait for event details to load
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithText("1 attending").isDisplayed()
    }
    composeTestRule.onNodeWithText("1 attending").performScrollTo().performClick()

    // 5. Verify Friend Indicator
    // We expect to see "Bob" with "(Following)" indicator
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithText(friendUser.name).isDisplayed()
    }

    composeTestRule.onNodeWithText(friendUser.name).assertIsDisplayed()
    composeTestRule.onNodeWithText("(Following)").assertIsDisplayed()
  }
}
