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
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.LanguageRepository
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.composables.EPFLLogoTestTags
import ch.epfllife.ui.eventDetails.EventDetailsTestTags
import ch.epfllife.ui.eventDetails.MapScreenTestTags
import ch.epfllife.ui.home.HomeScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
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
  private lateinit var db: Db

  @Before
  fun setUp() {
    setUpEmulator(auth, "NavigationTest")
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
    db = Db.freshLocal()
  }

  private fun setUpApp() {
    val languageRepository = LanguageRepository(db.userRepo)
    composeTestRule.setContent { ThemedApp(auth, db, languageRepository) }
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
    composeTestRule.onNodeWithTag(EPFLLogoTestTags.LOGO, useUnmergedTree = true).assertIsDisplayed()
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
    composeTestRule.onNodeWithTag(EPFLLogoTestTags.LOGO, useUnmergedTree = true).assertIsDisplayed()
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
    composeTestRule.onNodeWithTag(EPFLLogoTestTags.LOGO, useUnmergedTree = true).assertIsDisplayed()
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
      Assert.assertTrue(db.assocRepo.createAssociation(testEvent.association).isSuccess)
      Assert.assertEquals(
          testEvent.association,
          db.assocRepo.getAssociation(testEvent.association.id),
      )
      Assert.assertTrue(db.eventRepo.createEvent(testEvent).isSuccess)
      Assert.assertEquals(testEvent, db.eventRepo.getEvent(testEvent.id))
      Assert.assertEquals(1, db.eventRepo.getAllEvents().size)
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

  @Test
  fun navigateToEventDetails_showsEventDetailsScreen() {
    val testEvent = ExampleEvents.event1
    runTest {
      Assert.assertTrue(db.assocRepo.createAssociation(testEvent.association).isSuccess)
      Assert.assertTrue(db.eventRepo.createEvent(testEvent).isSuccess)
    }

    setUpApp()
    composeTestRule.navigateToEvent(testEvent.id)

    // Verify we're on the event details screen by checking for content
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.CONTENT, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun navigateToEventDetails_hidesBottomNavigationBar() {
    val testEvent = ExampleEvents.event1
    runTest {
      Assert.assertTrue(db.assocRepo.createAssociation(testEvent.association).isSuccess)
      Assert.assertTrue(db.eventRepo.createEvent(testEvent).isSuccess)
    }

    setUpApp()

    // Bottom bar should be visible on home screen
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule.navigateToEvent(testEvent.id)

    // Bottom bar should be hidden on event details screen
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun navigateToEventDetails_backButtonReturnsToHomeScreen() {
    val testEvent = ExampleEvents.event1
    runTest {
      Assert.assertTrue(db.assocRepo.createAssociation(testEvent.association).isSuccess)
      Assert.assertTrue(db.eventRepo.createEvent(testEvent).isSuccess)
    }

    setUpApp()
    composeTestRule.navigateToEvent(testEvent.id)

    // Verify we're on event details by checking for back button
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.BACK_BUTTON, useUnmergedTree = true)
        .assertIsDisplayed()

    // Click back button
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.BACK_BUTTON, useUnmergedTree = true)
        .performClick()

    // Verify we're back on home screen
    composeTestRule
        .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
        .assertIsDisplayed()

    // Bottom bar should be visible again
    composeTestRule
        .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun homeScreen_filterStateRestored_defaultSubscribed() {
    setUpApp()

    // 1. Verify initial state is Subscribed/For You
    composeTestRule.onNodeWithTag(HomeScreenTestTags.BUTTON_SUBSCRIBED).assertIsDisplayed()

    // 2. Navigate away (to a different top-level tab)
    composeTestRule.navigateToTab(Tab.Calendar)

    // 3. Return to Home
    composeTestRule.navigateToTab(Tab.HomeScreen)

    // 4. ASSERT: Filter must still be in Subscribed state
    composeTestRule.onNodeWithTag(HomeScreenTestTags.BUTTON_SUBSCRIBED).assertIsDisplayed()
  }

  @Test
  fun homeScreen_filterStateRestored_afterSelectingAll() {
    setUpApp()

    // 1. ACTION: Change filter to ALL
    composeTestRule.onNodeWithTag(HomeScreenTestTags.BUTTON_ALL).performClick()

    // 2. Navigate away
    composeTestRule.navigateToTab(Tab.AssociationBrowser)

    // 3. Return to Home
    composeTestRule.navigateToTab(Tab.HomeScreen)

    // 4. ASSERT: Filter must still be in ALL state
    composeTestRule.onNodeWithTag(HomeScreenTestTags.BUTTON_ALL).assertIsDisplayed()
  }

  @Test
  fun associationBrowser_filterStateRestored_defaultSubscribed() {
    setUpApp()

    // 1. Navigate to Association Browser
    composeTestRule.navigateToTab(Tab.AssociationBrowser)

    // 2. ASSERT: Initial state is Subscribed/For You
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).assertIsDisplayed()

    // 3. Navigate away
    composeTestRule.navigateToTab(Tab.Settings)

    // 4. Return to Association Browser
    composeTestRule.navigateToTab(Tab.AssociationBrowser)

    // 5. ASSERT: Filter must still be in Subscribed state
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).assertIsDisplayed()
  }

  @Test
  fun associationBrowser_filterStateRestored_afterSelectingAll() {
    setUpApp()

    composeTestRule.navigateToTab(Tab.AssociationBrowser)
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    // 3. Navigate away
    composeTestRule.navigateToTab(Tab.Calendar)

    // 4. Return to Association Browser
    composeTestRule.navigateToTab(Tab.AssociationBrowser)

    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).assertIsDisplayed()
  }

  @Test
  fun calendarScreen_filterStateRestored_defaultSubscribed() {
    setUpApp()

    // 1. Navigate to Calendar
    composeTestRule.navigateToTab(Tab.Calendar)

    // 2. ASSERT: Initial state is Subscribed/Enrolled
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).assertIsDisplayed()

    // 3. Navigate away
    composeTestRule.navigateToTab(Tab.HomeScreen)

    // 4. Return to Calendar
    composeTestRule.navigateToTab(Tab.Calendar)

    // 5. ASSERT: Filter must still be in Subscribed state
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).assertIsDisplayed()
  }

  @Test
  fun calendarScreen_filterStateRestored_afterSelectingAll() {
    setUpApp()

    // 1. Navigate to Calendar
    composeTestRule.navigateToTab(Tab.Calendar)

    // 2. ACTION: Change filter to ALL
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    // 3. Navigate away
    composeTestRule.navigateToTab(Tab.Settings)

    // 4. Return to Calendar
    composeTestRule.navigateToTab(Tab.Calendar)

    // 5. ASSERT: Filter must still be in ALL state
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).assertIsDisplayed()
  }

  @Test
  fun homeScreen_retainsAllFilter_onReturningFromDetails() {
    prepareEventDataForPersistence()
    setUpApp()

    composeTestRule.navigateToEvent(ExampleEvents.event1.id)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.BACK_BUTTON, useUnmergedTree = true)
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(HomeScreenTestTags.BUTTON_ALL).assertIsDisplayed()
  }

  private fun prepareEventDataForPersistence() = runTest {
    val event1 = ExampleEvents.event1
    val event2 = ExampleEvents.event2
    val assoc1 = ExampleAssociations.association1

    Assert.assertTrue(db.assocRepo.createAssociation(assoc1).isSuccess)
    Assert.assertTrue(db.eventRepo.createEvent(event1).isSuccess)
    Assert.assertTrue(db.eventRepo.createEvent(event2).isSuccess)

    db.userRepo.subscribeToEvent(event1.id)
  }
}
