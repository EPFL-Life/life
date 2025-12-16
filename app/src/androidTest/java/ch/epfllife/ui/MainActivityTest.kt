package ch.epfllife.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import ch.epfllife.ThemedApp
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.navigateToTab
import ch.epfllife.utils.setUpEmulator
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private lateinit var db: Db

  @Before
  fun setUp() {
    db = Db.freshLocal()
    setUpEmulator(auth, "MainActivityTest")
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
  }

  @Test
  fun themedApp_startsWithHomeScreen() {
    composeTestRule.setContent { ThemedApp(auth, db) }

    composeTestRule.waitForIdle()

    // Verify home screen is displayed
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun themedApp_showsBottomNavigationOnMainScreens() {
    composeTestRule.setContent { ThemedApp(auth, db) }

    composeTestRule.waitForIdle()

    // Bottom bar should be visible on home screen
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()

    // Bottom bar should be visible on all main tabs
    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule.navigateToTab(Tab.Calendar)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun themedApp_hasAllBottomNavigationTabs() {
    composeTestRule.setContent { ThemedApp(auth, db) }

    composeTestRule.waitForIdle()

    // Verify all tabs are present
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.CALENDAR_TAB, useUnmergedTree = true)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.SETTINGS_TAB, useUnmergedTree = true)
        .assertIsDisplayed()
  }
}
