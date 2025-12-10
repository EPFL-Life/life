package ch.epfllife.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.association.AssociationDetailsTestTags
import ch.epfllife.ui.composables.AssociationCardTestTags
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.composables.EventCardTestTags
import ch.epfllife.ui.eventDetails.EventDetailsTestTags
import ch.epfllife.ui.home.HomeScreenTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import org.hamcrest.Matchers.not
import org.junit.Assert

/**
 * Assert that the composable triggers the given callback when the node with the given tag is
 * clicked.
 *
 * This ensures that the composable is wired correctly and the UI can respond to user interactions.
 */
fun ComposeContentTestRule.assertClickable(
    composable: @Composable ((callback: () -> Unit) -> Unit),
    tag: String,
) {
  var clicked = false
  this.setContent { composable { clicked = true } }
  this.onNodeWithTag(tag).performClick()

  Assert.assertTrue("$tag should be clickable", clicked)
}

fun ComposeContentTestRule.assertTagIsDisplayed(tag: String) {
  this.onNodeWithTag(tag, useUnmergedTree = true)
      .assertExists("$tag must exist")
      .assertIsDisplayed()
}

fun ComposeContentTestRule.assertTagTextEquals(tag: String, text: String) {
  this.onNodeWithTag(tag, useUnmergedTree = true)
      .assertExists("$tag must exist")
      .assertIsDisplayed()
      .assertTextEquals(text)
}

fun ComposeContentTestRule.navigateToTab(tab: Tab) {
  val tabTag = NavigationTestTags.getTabTestTag(tab)
  val screenTag = NavigationTestTags.getScreenTestTagForTab(tab)
  this.onNodeWithTag(tabTag, useUnmergedTree = true).performClick()
  this.onNodeWithTag(screenTag, useUnmergedTree = true).assertIsDisplayed()
}

fun ComposeContentTestRule.navigateToEvent(eventId: String) {
  this.navigateToTab(Tab.HomeScreen)
  this.onNodeWithTag(HomeScreenTestTags.BUTTON_ALL).performClick()
  // Wait for event cards to load
  this.waitUntil(3000) {
    this.onNodeWithTag(EventCardTestTags.getEventCardTestTag(eventId)).isDisplayed()
  }
  this.waitForIdle()
  this.onNodeWithTag(EventCardTestTags.getEventCardTestTag(eventId)).performClick()
  // Wait for screen to load
  this.waitUntil(3000) { this.onNodeWithTag(EventDetailsTestTags.CONTENT).isDisplayed() }
  this.waitForIdle()
}

fun ComposeContentTestRule.navigateToAssociation(associationId: String) {
  this.navigateToTab(Tab.AssociationBrowser)
  this.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()
  // Wait for assoc cards to load
  this.waitUntil(3000) {
    this.onNodeWithTag(AssociationCardTestTags.getAssociationCardTestTag(associationId))
        .isDisplayed()
  }
  this.waitForIdle()
  this.onNodeWithTag(AssociationCardTestTags.getAssociationCardTestTag(associationId))
      .performClick()
  // Wait for screen to load
  this.waitUntil(3000) { this.onNodeWithTag(AssociationDetailsTestTags.CONTENT).isDisplayed() }
  this.waitForIdle()
}

fun setUpEmulator(auth: Auth, test: String) {
  Assert.assertTrue(
      "Firebase emulator must be running for local tests in -> $test",
      FirebaseEmulator.isRunning,
  )
  // Reset to signed out state
  auth.signOut()
  FirebaseEmulator.clearAuthEmulator() // this does not seem to sign out users though
  FirebaseEmulator.clearFirestoreEmulator()
}

fun assertToastMessage(
    decorView: android.view.View,
    message: Int,
) {
  val messageString = decorView.context.getString(message)
  val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

  // 1. Use textContains (handles partial matches/whitespace)
  // 2. Increase wait to 5000ms (emulators can be slow)
  val toastAppeared = device.wait(Until.hasObject(By.textContains(messageString)), 5000)

  Assert.assertTrue("Toast containing '$messageString' not found", toastAppeared)
}

fun ComposeContentTestRule.triggerRefresh(tag: String) {
  this.onNodeWithTag(tag).performTouchInput {
    swipeDown(
        startY = 0f,
        endY = 500f,
    )
  }
  this.waitForIdle()
  this.mainClock.advanceTimeBy(1000) // make sure the animation completes
}
