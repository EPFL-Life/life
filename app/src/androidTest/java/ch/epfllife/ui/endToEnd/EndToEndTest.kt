package ch.epfllife.ui.endToEnd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import ch.epfllife.ThemedApp
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class EndToEndTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent { ThemedApp() }
  }

  @Test
  fun canGoThroughAllTabs() {
    Tab.tabs.forEach { tab ->
      composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
      composeTestRule
          .onNodeWithTag(NavigationTestTags.getScreenTestTagForTab(tab))
          .assertIsDisplayed()
    }
  }

  @Test
  fun canExitWithBackPressFromHome() {
    pressBack()
    assertDestroyed()
  }

  @Test fun canExitWithDoubleBackPressFromSettings() = canExitWithDoublePressFromTab(Tab.HomeScreen)

  @Test
  fun canExitWithDoubleBackPressFromAssociationBrowser() =
      canExitWithDoublePressFromTab(Tab.AssociationBrowser)

  @Test fun canExitWithDoubleBackPressFromMyEvents() = canExitWithDoublePressFromTab(Tab.MyEvents)

  private fun canExitWithDoublePressFromTab(tab: Tab) {
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
    pressBack()
    pressBack()
    assertDestroyed()
  }

  private fun pressBack() {
    composeTestRule.activityRule.scenario.onActivity { activity ->
      activity.onBackPressedDispatcher.onBackPressed()
    }
    composeTestRule.waitUntil { composeTestRule.activity.isFinishing }
  }

  private fun assertDestroyed() {
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.activityRule.scenario.state == Lifecycle.State.DESTROYED
    }
  }
}
