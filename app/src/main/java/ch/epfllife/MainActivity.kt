package ch.epfllife

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import ch.epfllife.model.authentication.AuthRepository
import ch.epfllife.ui.associationsbrowser.AssociationBrowser
import ch.epfllife.ui.authentication.SignInScreen
import ch.epfllife.ui.home.HomeScreen
import ch.epfllife.ui.myevents.MyEvents
import ch.epfllife.ui.navigation.NavigationActions
import ch.epfllife.ui.navigation.Screen
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

    setContent { Theme { Surface(modifier = Modifier.fillMaxSize()) { App() } } }
  }
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

  NavHost(navController = navController, startDestination = startDestination) {
    navigation(
        startDestination = Screen.Auth.route,
        route = Screen.Auth.name,
    ) {
      composable(Screen.Auth.route) {
        SignInScreen(
            credentialManager = credentialManager,
            onSignedIn = { navigationActions.navigateTo(Screen.HomeScreen) })
      }
    }

    navigation(
        startDestination = Screen.HomeScreen.route,
        route = Screen.HomeScreen.name,
    ) {
      composable(Screen.HomeScreen.route) { HomeScreen(navigationActions = navigationActions) }
    }

    navigation(
        startDestination = Screen.AssociationBrowser.route,
        route = Screen.AssociationBrowser.name,
    ) {
      composable(Screen.AssociationBrowser.route) {
        AssociationBrowser(navigationActions = navigationActions)
      }
    }

    navigation(
        startDestination = Screen.MyEvents.route,
        route = Screen.MyEvents.name,
    ) {
      composable(Screen.MyEvents.route) { MyEvents(navigationActions = navigationActions) }
    }

    navigation(
        startDestination = Screen.Settings.route,
        route = Screen.Settings.name,
    ) {
      composable(Screen.Settings.route) { Settings(navigationActions = navigationActions) }
    }
  }
}
