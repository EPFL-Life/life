package ch.epfllife.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.ThemedApp
import ch.epfllife.ui.home.HomeScreenTestTags
import ch.epfllife.utils.navigateToTab
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
    composeTestRule
        .onNodeWithTag(NavigationTestTags.SETTINGS_TAB, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun canGoThroughAllTabsAndBottomBarIsVisible() {
    Tab.tabs.forEach { tab ->
      val tabTag = NavigationTestTags.getTabTestTag(tab)
      val screenTag = NavigationTestTags.getScreenTestTagForTab(tab)
      composeTestRule.navigateToTab(tabTag, screenTag)
      composeTestRule
          .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun bottomNavigationIsDisplayedForAssociationBrowser() {
    composeTestRule.navigateToTab(
        NavigationTestTags.ASSOCIATIONBROWSER_TAB, NavigationTestTags.ASSOCIATIONBROWSER_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun navigationStartsOnHomeScreen() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(HomeScreenTestTags.EPFLLOGO, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun canNavigateToAssociationBrowser() {
    composeTestRule.navigateToTab(
        NavigationTestTags.ASSOCIATIONBROWSER_TAB, NavigationTestTags.ASSOCIATIONBROWSER_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun canNavigateToAssociationBrowserAndBackToHome() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB, useUnmergedTree = true)
        .performClick()
    composeTestRule.navigateToTab(
        NavigationTestTags.HOMESCREEN_TAB, NavigationTestTags.HOMESCREEN_SCREEN)
  }

  @Test
  fun bottomNavigationIsDisplayedForMyEvents() {

    composeTestRule.navigateToTab(
        NavigationTestTags.MYEVENTS_TAB, NavigationTestTags.MYEVENTS_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun bottomNavigationIsDisplayedForSettings() {
    composeTestRule.navigateToTab(
        NavigationTestTags.SETTINGS_TAB, NavigationTestTags.SETTINGS_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun canNavigateToMyEvents() {
    composeTestRule.navigateToTab(
        NavigationTestTags.MYEVENTS_TAB, NavigationTestTags.MYEVENTS_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun canNavigateToSettings() {
    composeTestRule.navigateToTab(
        NavigationTestTags.SETTINGS_TAB, NavigationTestTags.SETTINGS_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun canNavigateToSettingsAndBackToHome() {
    composeTestRule.navigateToTab(
        NavigationTestTags.SETTINGS_TAB, NavigationTestTags.SETTINGS_SCREEN)
    composeTestRule.navigateToTab(
        NavigationTestTags.HOMESCREEN_TAB, NavigationTestTags.HOMESCREEN_SCREEN)
    composeTestRule
        .onNodeWithTag(HomeScreenTestTags.EPFLLOGO, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun canNavigateAcrossAllTabsAndReturnHome() {
    // Home -> AssociationBrowser
    composeTestRule.navigateToTab(
        NavigationTestTags.ASSOCIATIONBROWSER_TAB, NavigationTestTags.ASSOCIATIONBROWSER_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // AssociationBrowser -> MyEvents
    composeTestRule.navigateToTab(
        NavigationTestTags.MYEVENTS_TAB, NavigationTestTags.MYEVENTS_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // MyEvents -> Settings
    composeTestRule.navigateToTab(
        NavigationTestTags.SETTINGS_TAB, NavigationTestTags.SETTINGS_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // Settings -> Home
    composeTestRule.navigateToTab(
        NavigationTestTags.HOMESCREEN_TAB, NavigationTestTags.HOMESCREEN_SCREEN)
  }

  @Test
  fun canClickTwice() {
    composeTestRule.navigateToTab(
        NavigationTestTags.ASSOCIATIONBROWSER_TAB, NavigationTestTags.ASSOCIATIONBROWSER_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule.navigateToTab(
        NavigationTestTags.ASSOCIATIONBROWSER_TAB, NavigationTestTags.ASSOCIATIONBROWSER_SCREEN)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun returningToHomeRestoresHomeUiElements() {
    composeTestRule.navigateToTab(
        NavigationTestTags.MYEVENTS_TAB, NavigationTestTags.MYEVENTS_SCREEN)
    composeTestRule.navigateToTab(
        NavigationTestTags.HOMESCREEN_TAB, NavigationTestTags.HOMESCREEN_SCREEN)
    composeTestRule
        .onNodeWithTag(HomeScreenTestTags.EPFLLOGO, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
