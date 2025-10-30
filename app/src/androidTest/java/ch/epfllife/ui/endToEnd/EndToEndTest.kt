package ch.epfllife.ui.endToEnd

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoActivityResumedException
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
    assertBackPressWouldExit()
  }

  @Test fun canExitWithDoubleBackPressFromSettings() = canExitWithDoublePressFromTab(Tab.Settings)

  @Test
  fun canExitWithDoubleBackPressFromAssociationBrowser() =
      canExitWithDoublePressFromTab(Tab.AssociationBrowser)

  @Test fun canExitWithDoubleBackPressFromMyEvents() = canExitWithDoublePressFromTab(Tab.MyEvents)

  private fun canExitWithDoublePressFromTab(tab: Tab) {
    composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
    Espresso.pressBack()
    assertBackPressWouldExit()
  }

  private fun assertBackPressWouldExit() {
    // In theory we should be able to detect that the app closes with
    // - `composeTestRule.activityRule.scenario.state == Lifecycle.State.DESTROYED`
    // - `composeTestRule.activity.isFinishing`
    // In practice, that does not seems to work,
    // so instead we abuse the fact that Espresso.pressBack() throws in case the app would be
    // closed.
    try {
      Espresso.pressBack()
    } catch (e: NoActivityResumedException) {
      // NOTE: The documentation for Espresso.pressBack() states
      // that a `PerformException` is thrown,
      // but actually it is a `NoActivityResumedException`.
      return
    }
    throw AssertionError("Expected app to exit on back press, but it did not.")
  }
}
