package ch.epfllife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.credentials.CredentialManager
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.association.AssociationBrowser
import ch.epfllife.ui.association.AssociationDetailsScreen
import ch.epfllife.ui.authentication.SignInScreen
import ch.epfllife.ui.calendar.CalendarScreen
import ch.epfllife.ui.eventDetails.EventDetailsScreen
import ch.epfllife.ui.home.HomeScreen
import ch.epfllife.ui.navigation.BottomNavigationMenu
import ch.epfllife.ui.navigation.NavigationActions
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Screen
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.SettingsScreen
import ch.epfllife.ui.theme.Theme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent { ThemedApp(auth = Auth(CredentialManager.create(LocalContext.current))) }
  }
}

@Composable
fun ThemedApp(auth: Auth) {
  Theme { Surface(modifier = Modifier.fillMaxSize()) { App(auth) } }
}

/**
 * `App` is the main composable function that sets up the whole app UI. It initializes the
 * navigation controller and defines the navigation graph.
 *
 * @param auth The auth handler.
 */
@Composable
fun App(
    auth: Auth,
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val startDestination =
      if (auth.auth.currentUser == null) Screen.SignIn.route else Screen.HomeScreen.route

  // keep the current destination of the nav
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route

  // list with all the tabs available
  val allTabs = Tab.tabs

  // we obtain the current Tab, if we don't find the route, will be redirected to the HomeScreen
  val selectedTab =
      remember(currentRoute) {
        allTabs.firstOrNull { it.destination.route == currentRoute } ?: Tab.HomeScreen
      }

  val showBottomBar =
      when (currentRoute) {
        Screen.HomeScreen.route,
        Screen.AssociationBrowser.route,
        Screen.Calendar.route,
        Screen.Settings.route -> true
        else -> false
      }

  Scaffold(
      bottomBar = {
        if (showBottomBar) {
          BottomNavigationMenu(
              selectedTab = selectedTab,
              onTabSelected = { tab -> navigationActions.navigateTo(tab.destination) },
              modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
          )
        }
      }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
          composable(Screen.SignIn.route) {
            SignInScreen(
                auth = auth,
                onSignedIn = { navigationActions.navigateTo(Screen.HomeScreen) },
            )
          }

          // pass navigation callback to HomeScreen
          composable(Screen.HomeScreen.route) {
            HomeScreen(onEventClick = { id -> navigationActions.navigateToEventDetails(id) })
          }

          composable(Screen.AssociationBrowser.route) {
            AssociationBrowser(
                onAssociationClick = { associationId ->
                  navigationActions.navigateToAssociationDetails(associationId)
                })
          }

          composable(Screen.Calendar.route) {
            CalendarScreen(
                allEvents = emptyList(),
                enrolledEvents = emptyList(),
                onEventClick = { eventId -> navigationActions.navigateToEventDetails(eventId) })
          }

          composable(
              route = Screen.EventDetails.route + "/{eventId}",
              arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                  backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailsScreen(eventId = eventId, onGoBack = { navController.popBackStack() })
              }

          composable(
              route = Screen.AssociationDetails.route + "/{associationId}",
              arguments = listOf(navArgument("associationId") { type = NavType.StringType })) {
                  backStackEntry ->
                val associationId = backStackEntry.arguments?.getString("associationId") ?: ""
                AssociationDetailsScreen(
                    associationId = associationId, onGoBack = { navController.popBackStack() })
              }

          // Event details route
          composable(
              route = Screen.EventDetails.route + "/{eventId}",
              arguments = listOf(navArgument("eventId") { type = NavType.StringType })) {
                  backStackEntry ->
                val eventId =
                    backStackEntry.arguments?.getString("eventId")
                        ?: error("eventId is required for EventDetails screen")
                EventDetailsScreen(eventId = eventId, onGoBack = { navigationActions.goBack() })
              }

          composable(Screen.Settings.route) {
            SettingsScreen(
                auth = auth,
                onSignedOut = { navigationActions.navigateTo(Screen.SignIn) },
            )
          }
        }
      }
}
