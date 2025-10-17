package ch.epfllife.ui.home

// import androidx.compose.ui.test.assertExists

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * This test class verifies the correct behavior and visual rendering of the HomeScreen composable.
 * It ensures that elements such as the logo, search bar, filter buttons, and event list behave as
 * expected.
 */
class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Prepare the composable before each test
    composeTestRule.setContent { HomeScreen() }
  }

  @Test
  fun testUiElementsDisplayed() {
    arrayOf(
            HomeScreenTestTags.EPFLLOGO,
            DisplayedEventsTestTags.BUTTON_ALL,
            DisplayedEventsTestTags.BUTTON_SUBSCRIBED)
        .forEach { assertDisplayed(it) }
  }

  fun assertDisplayed(testTag: String) {
    composeTestRule.onNodeWithTag(testTag).assertIsDisplayed()
  }
}
