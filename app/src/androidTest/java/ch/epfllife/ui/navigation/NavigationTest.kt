package ch.epfllife.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.ThemedApp
import ch.epfllife.ui.home.HomeScreenTestTags
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    composeTestRule.setContent { ThemedApp() }
  }

  @Test
  fun testTagsAreCorrectlySet() {
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB).assertIsDisplayed()
  }

  @Test
  fun canGoThroughAllTabs_and_bottomBarIsVisible() {
    Tab.tabs.forEach { tab ->
      composeTestRule.onNodeWithTag(NavigationTestTags.getTabTestTag(tab)).performClick()
      composeTestRule
          .onNodeWithTag(NavigationTestTags.getScreenTestTagForTab(tab))
          .assertIsDisplayed()
      composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
    }
  }

  @Test
  fun bottomNavigationIsDisplayedForAssociationBrowser() {
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun tabsAreClickable() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB)
        .assertIsDisplayed()
        .performClick()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB)
        .assertIsDisplayed()
        .performClick()
  }

  @Test
  fun navigationStartsOnHomeScreen() {
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.EPFLLOGO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun canNavigateToAssociationBrowser() {
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertDoesNotExist()
  }

  @Test
  fun canNavigateToAssociationBrowserAndBackToHome() {
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
  }

  @Test
  fun bottomNavigationIsDisplayedForMyEvents() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun bottomNavigationIsDisplayedForSettings() {
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun canNavigateToMyEvents() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertDoesNotExist()
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN).assertDoesNotExist()
  }

  @Test
  fun canNavigateToSettings() {
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertDoesNotExist()
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN).assertDoesNotExist()
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN).assertDoesNotExist()
  }

  @Test
  fun canNavigateToSettingsAndBackToHome() {
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.EPFLLOGO).assertIsDisplayed()
  }

  @Test
  fun canNavigateAcrossAllTabsAndReturnHome() {
    // Home -> AssociationBrowser
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertDoesNotExist()

    // AssociationBrowser -> MyEvents
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN).assertDoesNotExist()

    // MyEvents -> Settings
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.SETTINGS_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN).assertDoesNotExist()

    // Settings -> Home
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
  }

  @Test
  fun clickingSameTopLevelTabTwice_staysOnSameScreen_andBottomBarVisible() {
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU).assertIsDisplayed()
  }

  @Test
  fun returningToHomeRestoresHomeUiElements() {
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB).performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
    composeTestRule.onNodeWithTag(HomeScreenTestTags.EPFLLOGO).assertIsDisplayed()
  }
}
