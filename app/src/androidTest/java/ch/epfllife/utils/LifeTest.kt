package ch.epfllife.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import ch.epfllife.model.authentication.Auth
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

fun setUpEmulatorAuth(auth: Auth, test: String) {
  Assert.assertTrue(
      "Firebase emulator must be running for local $test tests",
      FirebaseEmulator.isRunning,
  )
  // Reset to signed out state
  auth.signOut()
}

fun assertToastMessage(
    decorView: android.view.View,
    message: Int,
) {
  // Got this technique from:
  // https://stackoverflow.com/questions/28390574/checking-toast-message-in-android-espresso
  Espresso.onView(withText(message))
      .inRoot(withDecorView(not(decorView)))
      .check(matches(isDisplayed()))
}
