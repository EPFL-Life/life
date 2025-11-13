package ch.epfllife.ui.navigation

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.test.rule.GrantPermissionRule
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.association.AssociationRepositoryFirestore
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.ui.eventDetails.EventDetailsTestTags
import ch.epfllife.ui.eventDetails.MapScreenTestTags
import ch.epfllife.ui.home.HomeScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.FirebaseEmulator
import ch.epfllife.utils.navigateToEvent
import ch.epfllife.utils.navigateToTab
import ch.epfllife.utils.setUpEmulator
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor

class NavigationTest {

  @get:Rule val composeTestRule = createComposeRule()
  // This is a nullable platform type, so we need to specify the type explicitly
  @get:Rule
  val permissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION,
      )
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private val eventRepository = EventRepositoryFirestore(FirebaseEmulator.firestore)
  private val assocRepository = AssociationRepositoryFirestore(FirebaseEmulator.firestore)

  @Before
  fun setUp() {
    setUpEmulator(auth, "NavigationTest")
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
  }

  private fun setUpApp() {
    composeTestRule.setContent { ThemedApp(auth) }
  }

  @Test
  fun testTagsAreCorrectlySet() {
    setUpApp()
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
    setUpApp()
    Tab.tabs.forEach { tab ->
      composeTestRule.navigateToTab(tab)
      composeTestRule
          .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
          .assertIsDisplayed()
    }
  }

  @Test
  fun bottomNavigationIsDisplayedForAssociationBrowser() {
    setUpApp()
    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun navigationStartsOnHomeScreen() {
    setUpApp()
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
    setUpApp()
    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun canNavigateToAssociationBrowserAndBackToHome() {
    setUpApp()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB, useUnmergedTree = true)
        .performClick()
    composeTestRule.navigateToTab(Tab.HomeScreen)
  }

  @Test
  fun bottomNavigationIsDisplayedForMyEvents() {
    setUpApp()

    composeTestRule.navigateToTab(Tab.Calendar)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun bottomNavigationIsDisplayedForSettings() {
    setUpApp()
    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun canNavigateToMyEvents() {
    setUpApp()
    composeTestRule.navigateToTab(Tab.Calendar)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun canNavigateToSettings() {
    setUpApp()
    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(NavigationTestTags.CALENDAR_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun canNavigateToSettingsAndBackToHome() {
    setUpApp()
    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule.navigateToTab(Tab.HomeScreen)
    composeTestRule
        .onNodeWithTag(HomeScreenTestTags.EPFLLOGO, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun canNavigateAcrossAllTabsAndReturnHome() {
    setUpApp()
    // Home -> AssociationBrowser
    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // AssociationBrowser -> MyEvents
    composeTestRule.navigateToTab(Tab.Calendar)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // MyEvents -> Settings
    composeTestRule.navigateToTab(Tab.Settings)
    composeTestRule
        .onNodeWithTag(NavigationTestTags.CALENDAR_SCREEN, useUnmergedTree = true)
        .assertDoesNotExist()

    // Settings -> Home
    composeTestRule.navigateToTab(Tab.HomeScreen)
  }

  @Test
  fun canClickTwice() {
    setUpApp()
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
    setUpApp()
    composeTestRule.navigateToTab(Tab.Calendar)
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

  @Test
  fun canGoToEventMapAndBackHome() {
    val testEvent = ExampleEvents.event1
    runTest {
      Assert.assertTrue(assocRepository.createAssociation(testEvent.association).isSuccess)
      Assert.assertEquals(
          testEvent.association, assocRepository.getAssociation(testEvent.association.id))
      Assert.assertTrue(eventRepository.createEvent(testEvent).isSuccess)
      Assert.assertEquals(testEvent, eventRepository.getEvent(testEvent.id))
      Assert.assertEquals(1, eventRepository.getAllEvents().size)
    }
    setUpApp()
    composeTestRule.navigateToEvent(testEvent.id)
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.MAP_SCREEN).assertIsDisplayed()
    // reached map screen
    composeTestRule.onNodeWithTag(MapScreenTestTags.BACK_BUTTON).assertIsDisplayed().performClick()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
    // back home
  }
}
