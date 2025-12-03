package ch.epfllife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import ch.epfllife.model.association.Association
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.db.Db
import ch.epfllife.model.map.Location
import ch.epfllife.ui.admin.AddEditAssociationScreen
import ch.epfllife.ui.admin.AddEditAssociationViewModel
import ch.epfllife.ui.admin.SelectAssociationScreen
import ch.epfllife.ui.association.AssociationBrowser
import ch.epfllife.ui.association.AssociationDetailsScreen
import ch.epfllife.ui.authentication.SignInScreen
import ch.epfllife.ui.calendar.CalendarScreen
import ch.epfllife.ui.eventDetails.EventDetailsScreen
import ch.epfllife.ui.eventDetails.MapScreen
import ch.epfllife.ui.home.HomeScreen
import ch.epfllife.ui.navigation.BottomNavigationMenu
import ch.epfllife.ui.navigation.NavigationActions
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Screen
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.SettingsScreen
import ch.epfllife.ui.theme.Theme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val selectedAssociationIdKey = "selectedAssociationId"
private const val selectedAssociationNameKey = "selectedAssociationName"

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      ThemedApp(auth = Auth(CredentialManager.create(LocalContext.current)), db = Db.firestore)
    }
  }
}

@Composable
fun ThemedApp(auth: Auth, db: Db) {
  Theme { Surface(modifier = Modifier.fillMaxSize()) { App(auth, db) } }
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
    db: Db,
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
            HomeScreen(
                onEventClick = { id -> navigationActions.navigateToEventDetails(id) }, db = db)
          }

          composable(Screen.AssociationBrowser.route) {
            AssociationBrowser(
                onAssociationClick = { associationId ->
                  navigationActions.navigateToAssociationDetails(associationId)
                },
                db = db)
          }

          composable(Screen.Calendar.route) {
            CalendarScreen(
                onEventClick = { eventId -> navigationActions.navigateToEventDetails(eventId) },
                db = db)
          }

          composable(
              route = Screen.AssociationDetails.route + "/{associationId}",
              arguments = listOf(navArgument("associationId") { type = NavType.StringType }),
          ) { backStackEntry ->
            val associationId = backStackEntry.arguments?.getString("associationId") ?: ""
            AssociationDetailsScreen(
                associationId = associationId,
                onGoBack = { navController.popBackStack() },
                onEventClick = { id -> navigationActions.navigateToEventDetails(id) },
                db = db,
            )
          }

          // Event details route
          composable(
              route = Screen.EventDetails.route + "/{eventId}",
              arguments = listOf(navArgument("eventId") { type = NavType.StringType }),
          ) { backStackEntry ->
            val eventId =
                backStackEntry.arguments?.getString("eventId")
                    ?: error("eventId is required for EventDetails screen")
            EventDetailsScreen(
                eventId = eventId,
                onGoBack = { navigationActions.goBack() },
                onOpenMap = { location ->
                  val encodedLocation = Json.encodeToString(location)
                  navigationActions.navigateToScreenWithId(Screen.Map, encodedLocation)
                },
                db = db,
            )
          }

          composable(
              route = Screen.Map.route + "/{location}",
              arguments = listOf(navArgument("location") { type = NavType.StringType }),
          ) { backStackEntry ->
            val encodedLocation =
                backStackEntry.arguments?.getString("location")
                    ?: error("eventId is required for EventDetails screen")
            val location = Json.decodeFromString<Location>(encodedLocation)
            MapScreen(location = location, onGoBack = { navigationActions.goBack() })
          }

          composable(Screen.Settings.route) { backStackEntry ->
            val selectedAssociationId by
                backStackEntry.savedStateHandle
                    .getStateFlow(selectedAssociationIdKey, null as String?)
                    .collectAsState()
            val selectedAssociationName by
                backStackEntry.savedStateHandle
                    .getStateFlow(selectedAssociationNameKey, null as String?)
                    .collectAsState()
            SettingsScreen(
                auth = auth,
                onSignedOut = { navigationActions.navigateTo(Screen.SignIn) },
                onSelectAssociationClick = {
                  navigationActions.navigateTo(Screen.SelectAssociation)
                },
                onManageAssociationClick = { associationId ->
                  navigationActions.navigateToAddEditAssociation(associationId)
                },
                onManageAssociationEventsClick = { _ -> // use AssociationID
                  // TODO: placeholder, can navigate to manage events later
                },
                selectedAssociationId = selectedAssociationId,
                selectedAssociationName = selectedAssociationName,
                onAddNewAssociationClick = { navigationActions.navigateToAddEditAssociation() })
          }

          composable(Screen.SelectAssociation.route) {
            SelectAssociationScreen(
                db = db,
                onGoBack = { navController.popBackStack() },
                onAssociationSelected = { association ->
                  val previousEntry = navController.previousBackStackEntry
                  previousEntry?.savedStateHandle?.set(selectedAssociationIdKey, association.id)
                  previousEntry?.savedStateHandle?.set(selectedAssociationNameKey, association.name)
                  val popped = navController.popBackStack()
                  if (!popped) {
                    navigationActions.navigateTo(Screen.Settings)
                  }
                },
                onAddNewAssociation = { navigationActions.navigateToAddEditAssociation() })
          }

          composable(
              route = Screen.AddEditAssociation.route,
              arguments =
                  listOf(
                      navArgument(Screen.AddEditAssociation.ASSOCIATION_ID_ARG) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                      })) { backStackEntry ->
                val associationId =
                    backStackEntry.arguments?.getString(
                        Screen.AddEditAssociation.ASSOCIATION_ID_ARG)

                val association by
                    produceState<Association?>(initialValue = null, associationId) {
                      value = associationId?.let { db.assocRepo.getAssociation(it) }
                    }

                AddEditAssociationScreen(
                    viewModel = AddEditAssociationViewModel(association),
                    onBack = { navController.popBackStack() },
                    onSubmitSuccess = { navController.popBackStack() })
              }
        }
      }
}
