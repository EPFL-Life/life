package ch.epfllife.ui.endToEnd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.ui.eventDetails.EventDetailsTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.utils.FirestoreLifeTest
import ch.epfllife.utils.navigateToEvent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CalendarEndToEndTest : FirestoreLifeTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private fun setUpApp() {
    composeTestRule.setContent { ThemedApp(auth, db) }
  }

  private suspend fun prepareData() {
    val assoc3 = ExampleAssociations.association3
    db.assocRepo.createAssociation(assoc3)
    val event3 = ExampleEvents.event3
    db.eventRepo.createEvent(event3)

    val authUid = Firebase.auth.uid!!
    val user1 = ExampleUsers.user1
    db.userRepo.createUser(user1.copy(id = authUid))
  }

  @Test
  fun calendarGridInteractionFlow() {
    runTest { prepareData() }
    setUpApp()

    // 1. Browse Events & Enroll in Event 3
    composeTestRule.navigateToEvent(ExampleEvents.event3.id)

    // Click Enroll
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000) // Wait for Firestore

    // Go back to Home to see the Bottom Bar
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // 2. Go to Calendar Screen
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(Tab.Calendar)).performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000) // Wait for calendar load

    // 3. Click on the day on the grid (Dec 5)
    // Tag: "calendar_day_5"
    composeTestRule.onNodeWithTag("calendar_day_5").assertIsDisplayed().performClick()
    composeTestRule.waitForIdle()

    // 4. Make sure event exists (in the list below)
    composeTestRule.onNodeWithText(ExampleEvents.event3.title).assertIsDisplayed()

    // 5. Click on it
    composeTestRule.onNodeWithText(ExampleEvents.event3.title).performClick()
    composeTestRule.waitForIdle()

    // Wait for screen to load
    composeTestRule.waitUntil(5000) {
      composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).isDisplayed()
    }

    // 6. Unsubscribe
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000)
  }
}
