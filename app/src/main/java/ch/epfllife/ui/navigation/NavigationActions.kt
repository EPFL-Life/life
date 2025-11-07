package ch.epfllife.ui.navigation

import androidx.navigation.NavHostController

sealed class Screen(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false
) {
  object Auth : Screen(route = "auth", name = "Authentication")

  object HomeScreen :
      Screen(route = "homescreen", name = "HomeScreen", isTopLevelDestination = true)

  object AssociationBrowser :
      Screen(
          route = "associationbrowser", name = "AssociationBrowser", isTopLevelDestination = true)

  object MyEvents : Screen(route = "myevents", name = "MyEvents", isTopLevelDestination = true)

  object Settings : Screen(route = "settings", name = "Settings", isTopLevelDestination = true)

  object AssociationDetails :
      Screen(route = "associationdetails/{associationId}", name = "AssociationDetails")
}

open class NavigationActions(
    private val navController: NavHostController,
) {
  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: Screen) {
    if (screen.isTopLevelDestination && currentRoute() == screen.route) {
      // If the user is already on the top-level destination, do nothing
      return
    }
    navController.navigate(screen.route) {
      if (screen.isTopLevelDestination) {
        launchSingleTop = true
        popUpTo(screen.route) { inclusive = true }
      }
      if (screen !is Screen.Auth) {
        // Restore state when reselecting a previously selected item
        restoreState = true
      }
    }
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }
}
