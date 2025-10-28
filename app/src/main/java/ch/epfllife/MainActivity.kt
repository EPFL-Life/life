package ch.epfllife

import android.content.Context
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ch.epfllife.model.authentication.AuthRepository
import ch.epfllife.ui.association.AssociationBrowser
import ch.epfllife.ui.authentication.SignInScreen
import ch.epfllife.ui.home.HomeScreen
import ch.epfllife.ui.myevents.MyEvents
import ch.epfllife.ui.navigation.BottomNavigationMenu
import ch.epfllife.ui.navigation.NavigationActions
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Screen
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.Settings
import ch.epfllife.ui.theme.Theme
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient

/**
 * *B3 only*:
 *
 * Provide an OkHttpClient client for network requests.
 *
 * Property `client` is mutable for testing purposes.
 */
object HttpClientProvider {
  var client: OkHttpClient = OkHttpClient()
}

class MainActivity : ComponentActivity() {

  private lateinit var auth: FirebaseAuth
  private lateinit var authRepository: AuthRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent { ThemedApp() }
  }
}

@Composable
fun ThemedApp() {
  Theme { Surface(modifier = Modifier.fillMaxSize()) { App() } }
}

/**
 * `App` is the main composable function that sets up the whole app UI. It initializes the
 * navigation controller and defines the navigation graph. You can add your app implementation
 * inside this function.
 *
 * @param navHostController The navigation controller used for navigating between screens.
 *
 * For B3:
 *
 * @param context The context of the application, used for accessing resources and services.
 * @param credentialManager The CredentialManager instance for handling authentication credentials.
 */
@Composable
fun App(
    context: Context = LocalContext.current,
    credentialManager: CredentialManager = CredentialManager.create(context),
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val startDestination = Screen.HomeScreen.route

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
        Screen.MyEvents.route,
        Screen.Settings.route -> true
        else -> false
      }

  Scaffold(
      bottomBar = {
        if (showBottomBar) {
          BottomNavigationMenu(
              selectedTab = selectedTab,
              onTabSelected = { tab -> navigationActions.navigateTo(tab.destination) },
              modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
        }
      }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)) {
              composable(Screen.Auth.route) {
                SignInScreen(
                    credentialManager = credentialManager,
                    onSignedIn = { navigationActions.navigateTo(Screen.HomeScreen) })
              }

              composable(Screen.HomeScreen.route) { HomeScreen() }
              composable(Screen.AssociationBrowser.route) { AssociationBrowser() }
              composable(Screen.MyEvents.route) { MyEvents() }
              composable(Screen.Settings.route) { Settings() }
            }
      }
}
