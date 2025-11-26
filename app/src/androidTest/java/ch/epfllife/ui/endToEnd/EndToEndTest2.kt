package ch.epfllife.ui.endToEnd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.db.Db
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.eventDetails.EventDetailsTestTags
import ch.epfllife.utils.FirestoreLifeTest
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.navigateToEvent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EndToEndTest2 : FirestoreLifeTest() {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  private fun setUpApp() {
    composeTestRule.setContent { ThemedApp(auth, Db.firestore) }
  }

  private suspend fun prepareData() {
    val assoc1 = ExampleAssociations.association1
    db.assocRepo.createAssociation(assoc1)
    val event = ExampleEvents.event1
    db.eventRepo.createEvent(event)

    val authUid = Firebase.auth.uid!!

    // to avoid problems, we assign the sample user the UID of the
    // authenticated user, thus simulating that they are the same.
    val user1 = ExampleUsers.user1
    db.userRepo.createUser(user1.copy(id = authUid))
  }

  @Test
  fun endToEndEnrollmentFlow() {
    // This end to end test addresses the user story #121:
    // As a student I want to enroll in a event

    // 1. log in as an authenticated user
    runTest { prepareData() }
    setUpApp()

    // 2. we click on the first event card
    composeTestRule.navigateToEvent(ExampleEvents.event1.id)

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.waitForIdle()

    // IMPORTANT: ensure the asynchronous Firestore write operation completes in the emulator before
    // reading the updated data
    Thread.sleep(1000)
    // 3. go back to home screen
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.assertTagIsDisplayed(tag = DisplayedEventsTestTags.BUTTON_ALL)
    composeTestRule.waitForIdle()

    Thread.sleep(1000)
    // 4. check that the event is displayed
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
  }
}
