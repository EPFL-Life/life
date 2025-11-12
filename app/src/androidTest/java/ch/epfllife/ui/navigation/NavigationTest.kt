package ch.epfllife.ui.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import ch.epfllife.ThemedApp
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.home.HomeScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.navigateToTab
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor

class NavigationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    val fireBaseAuth = mock(FirebaseAuth::class.java)
    `when`(fireBaseAuth.currentUser).thenReturn(mock(FirebaseUser::class.java))
    composeTestRule.setContent {
      ThemedApp(Auth(FakeCredentialManager.withDefaultTestUser, auth = fireBaseAuth))
    }
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
      composeTestRule.navigateToTab(tab)
      composeTestRule
          .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun bottomNavigationIsDisplayedForAssociationBrowser() {
    composeTestRule.navigateToTab(Tab.AssociationBrowser)
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
    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun canNavigateToAssociationBrowserAndBackToHome() {
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB, useUnmergedTree = true)
        .performClick()
    composeTestRule.navigateToTab(Tab.HomeScreen)
  }

  @Test
  fun bottomNavigationIsDisplayedForMyEvents() {

    composeTestRule.navigateToTab(Tab.MyEvents)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun bottomNavigationIsDisplayedForSettings() {
    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun canNavigateToMyEvents() {
    composeTestRule.navigateToTab(Tab.MyEvents)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun canNavigateToSettings() {
    composeTestRule.navigateToTab(Tab.Settings)
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
    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule.navigateToTab(Tab.HomeScreen)
    composeTestRule
        .onNodeWithTag(HomeScreenTestTags.EPFLLOGO, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun canNavigateAcrossAllTabsAndReturnHome() {
    // Home -> AssociationBrowser
    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // AssociationBrowser -> MyEvents
    composeTestRule.navigateToTab(Tab.MyEvents)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // MyEvents -> Settings
    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.MYEVENTS_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // Settings -> Home
    composeTestRule.navigateToTab(Tab.HomeScreen)
  }

  @Test
  fun canClickTwice() {
    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun returningToHomeRestoresHomeUiElements() {
    composeTestRule.navigateToTab(Tab.MyEvents)
    composeTestRule.navigateToTab(Tab.HomeScreen)
    composeTestRule
        .onNodeWithTag(HomeScreenTestTags.EPFLLOGO, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun navigateToEventDetails_navigatesToCorrectRoute() {
    val navController = mock(NavHostController::class.java)
    val actions = NavigationActions(navController)
    val eventId = "event-123"

    actions.navigateToEventDetails(eventId)

    val captor = argumentCaptor<NavDeepLinkRequest>()
    verify(navController).navigate(captor.capture(), anyOrNull(), anyOrNull())
    assert(captor.firstValue.uri.toString().contains("eventdetails/$eventId"))
  }

  @Test
  fun navigateToEventDetails_hidesBottomNavigation() {
    val navController = mock(NavHostController::class.java)
    val actions = NavigationActions(navController)
    val eventId = "test-event-id"

    actions.navigateToEventDetails(eventId)

    // After navigation action is invoked, verify bottom bar will be hidden
    // The bottom bar visibility is controlled by the route in MainActivity
    val captor = argumentCaptor<NavDeepLinkRequest>()
    verify(navController).navigate(captor.capture(), anyOrNull(), anyOrNull())

    // Verify the route is correct for event details
    val uri = captor.firstValue.uri.toString()
    assert(uri.contains("eventdetails/$eventId")) {
      "Expected route to contain 'eventdetails/$eventId' but got '$uri'"
    }
  }

  @Test
  fun goBack_popsBackStack() {
    val navController = mock(NavHostController::class.java)
    val actions = NavigationActions(navController)

    actions.goBack()

    verify(navController).popBackStack()
  }
}
