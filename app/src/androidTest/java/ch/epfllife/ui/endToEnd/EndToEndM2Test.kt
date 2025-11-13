package ch.epfllife.ui.endToEnd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.eventDetails.EventDetailsTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.FirestoreLifeTest
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.setUpEmulatorAuth
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class EndToEndM2Test : FirestoreLifeTest() {
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        // CI tests sometimes fail because of an open system dialog
        // (likely caused through the fact that it runs too slow)
        // This should fix it, by closing open system dialogs before starting the test.
        // Source:
        // https://stackoverflow.com/questions/39457305/android-testing-waited-for-the-root-of-the-view-hierarchy-to-have-window-focus
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            .executeShellCommand("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS")
        setUpEmulatorAuth(auth, "EndToEndTest")
    }

    private fun useLoggedInApp() {
        runTest {
            val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
            Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
        }
        composeTestRule.setContent { ThemedApp(auth) }
    }

    private fun useLoggedOutApp() {
        composeTestRule.setContent { ThemedApp(auth) }
    }

    private fun getSubscribedClickableCount(): Int {
        composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.assertTagIsDisplayed(tag = DisplayedEventsTestTags.BUTTON_SUBSCRIBED)

        val collection = composeTestRule.onAllNodes(hasClickAction())
        val nodes = collection.fetchSemanticsNodes()
        val headerClickableCount = 1
        return maxOf(0, nodes.size - headerClickableCount)
    }

    private fun clickToFirstEvent(){
        // check that there is any event and click on the first one
        val collection = composeTestRule.onAllNodes(hasClickAction())
        val nodes = collection.fetchSemanticsNodes()
        if (nodes.size <= 1) error("No events available")

        collection[1].performClick()
    }

    private suspend fun prepareData() {
        val assoc1 = ExampleAssociations.association1
        assocRepository.createAssociation(assoc1)
        val event = ExampleEvents.event1
        eventRepository.createEvent(event)

        val authUid = Firebase.auth.uid!!

        // to avoid problems, we assign the sample user the UID of the
        // authenticated user, thus simulating that they are the same.
        val user1 = ExampleUsers.user1
        userRepository.createUser(user1.copy(id = authUid))
    }

    @Ignore
    @Test
    fun endToEndEnrollmentFlow(){
        // This end to end test addresses the user story #121:
        // As a student I want to enroll in a event

        // 1. log in as an authenticated user
        useLoggedInApp()

        runTest { prepareData() }

        // 2: we are going to save the number of events we are enrolled initially
        val initialNumberEnrolledEvents = getSubscribedClickableCount()

        // 3. go to the all events screen
        composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.assertTagIsDisplayed(tag = DisplayedEventsTestTags.BUTTON_ALL)

        // 4. we click on the first event card
        clickToFirstEvent()

        val enrollNode = composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON)
        enrollNode.assertIsDisplayed()
        enrollNode.performScrollTo()
        enrollNode.performClick()
        composeTestRule.waitForIdle()

        // 6. go back to home screen
        composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.assertTagIsDisplayed(tag = DisplayedEventsTestTags.BUTTON_ALL)

        // 7. check if the numbers of events have increased
        val finalNumberEnrolledEvents = getSubscribedClickableCount()
        Assert.assertTrue(finalNumberEnrolledEvents > initialNumberEnrolledEvents)

        //8. we log out finally
        useLoggedOutApp()

    }


}