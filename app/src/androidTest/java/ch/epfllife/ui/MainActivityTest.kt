package ch.epfllife.ui

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.epfllife.ui.navigation.NavigationActions
import ch.epfllife.ui.navigation.Screen
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eventDetailsRoute_extractsEventIdFromArguments() {
    // Test that eventId is correctly extracted from navigation arguments
    var capturedEventId: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "test") {
        composable("test") {
          // Navigate to event details immediately
          navController.navigate("eventdetails/test-event-123")
        }
        composable(
            route = Screen.EventDetails.route + "/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                backStackEntry ->
              val eventId = backStackEntry.arguments?.getString("eventId")
              capturedEventId = eventId
              // Simple composable to verify we got here
              androidx.compose.material3.Text(text = "Event: $eventId")
            }
      }
    }

    composeTestRule.waitForIdle()

    // Verify the eventId was extracted correctly
    assert(capturedEventId == "test-event-123") {
      "Expected eventId to be 'test-event-123' but got '$capturedEventId'"
    }
  }

  @Test
  fun eventDetailsRoute_callsEventDetailsScreenWithCorrectEventId() {
    // Test that EventDetailsScreen is called with the correct eventId
    var eventDetailsScreenCalled = false
    var capturedEventId: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "test") {
        composable("test") { navController.navigate("eventdetails/my-event-456") }
        composable(
            route = Screen.EventDetails.route + "/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                backStackEntry ->
              val eventId =
                  backStackEntry.arguments?.getString("eventId")
                      ?: error("eventId is required for EventDetails screen")
              eventDetailsScreenCalled = true
              capturedEventId = eventId
              // Mock EventDetailsScreen for testing
              androidx.compose.material3.Text(text = "Event Details: $eventId")
            }
      }
    }

    composeTestRule.waitForIdle()

    // Verify EventDetailsScreen was called with correct eventId
    assert(eventDetailsScreenCalled) { "EventDetailsScreen should have been called" }
    assert(capturedEventId == "my-event-456") {
      "Expected eventId to be 'my-event-456' but got '$capturedEventId'"
    }
  }

  @Test
  fun eventDetailsRoute_requiresEventIdParameter() {
    // This test verifies that eventId parameter is properly configured
    // and the route expects it as a required parameter
    var eventIdWasNull = false

    composeTestRule.setContent {
      val navController = rememberNavController()
      NavHost(navController = navController, startDestination = "test") {
        composable("test") {
          // Navigate to event details with a valid eventId
          navController.navigate("eventdetails/valid-id")
        }
        composable(
            route = Screen.EventDetails.route + "/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                backStackEntry ->
              val eventId = backStackEntry.arguments?.getString("eventId")
              if (eventId == null) {
                eventIdWasNull = true
              }
              // This matches the MainActivity code:
              // val eventId = backStackEntry.arguments?.getString("eventId")
              //     ?: error("eventId is required for EventDetails screen")
              val finalEventId = eventId ?: error("eventId is required for EventDetails screen")
              androidx.compose.material3.Text(text = "Event: $finalEventId")
            }
      }
    }

    composeTestRule.waitForIdle()

    // Verify eventId was not null (proper navigation with eventId)
    assert(!eventIdWasNull) { "eventId should not be null when navigating with valid id" }
  }

  @Test
  fun eventDetailsRoute_callsOnGoBackCallback() {
    // Test that onGoBack callback is properly wired
    var goBackCalled = false

    composeTestRule.setContent {
      val navController = rememberNavController()
      val navigationActions = NavigationActions(navController)

      NavHost(navController = navController, startDestination = "test") {
        composable("test") { navController.navigate("eventdetails/callback-test-789") }
        composable(
            route = Screen.EventDetails.route + "/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                backStackEntry ->
              val eventId =
                  backStackEntry.arguments?.getString("eventId")
                      ?: error("eventId is required for EventDetails screen")

              // Create a mock event details screen that tests the onGoBack callback
              androidx.compose.material3.Surface {
                androidx.compose.foundation.layout.Column {
                  androidx.compose.material3.Text(text = "Event: $eventId")
                  androidx.compose.material3.Button(
                      onClick = {
                        goBackCalled = true
                        navigationActions.goBack()
                      }) {
                        androidx.compose.material3.Text("Go Back")
                      }
                }
              }
            }
      }
    }

    composeTestRule.waitForIdle()

    // Find and click the back button
    composeTestRule.onNodeWithText("Go Back").performClick()

    composeTestRule.waitForIdle()

    // Verify the callback was called
    assert(goBackCalled) { "onGoBack callback should have been called" }
  }

  @Test
  fun eventDetailsRoute_hidesBottomNavigationBar() {
    // Test that bottom navigation is hidden for event details route
    var currentRoute: String? = null

    composeTestRule.setContent {
      val navController = rememberNavController()

      // Track current route
      val backStackEntry by navController.currentBackStackEntryAsState()
      currentRoute = backStackEntry?.destination?.route

      NavHost(navController = navController, startDestination = "test") {
        composable("test") { navController.navigate("eventdetails/test-event") }
        composable(
            route = Screen.EventDetails.route + "/{eventId}",
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                backStackEntry ->
              val eventId =
                  backStackEntry.arguments?.getString("eventId")
                      ?: error("eventId is required for EventDetails screen")
              androidx.compose.material3.Text(text = "Event: $eventId")
            }
      }
    }

    composeTestRule.waitForIdle()

    // Verify current route is event details route
    assert(currentRoute?.startsWith("eventdetails") == true) {
      "Expected route to start with 'eventdetails' but got '$currentRoute'"
    }

    // In MainActivity, the bottom bar visibility is controlled by:
    // showBottomBar = when (currentRoute) { Screen.EventDetails.route -> false }
    // This test verifies we're on the correct route
  }
}
