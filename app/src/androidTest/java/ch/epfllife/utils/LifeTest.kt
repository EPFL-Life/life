package ch.epfllife.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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

fun ComposeContentTestRule.navigateToTab(tab: String, screenTag: String) {

  this.onNodeWithTag(tab, useUnmergedTree = true).performClick()
  this.onNodeWithTag(screenTag, useUnmergedTree = true).assertIsDisplayed()
}
