package ch.epfllife.utils

import androidx.compose.runtime.Composable
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
