package ch.epfllife.ui.navigation

import androidx.navigation.NavHostController

sealed class Screen(
    val route: String,
    val name: String,
    val isTopLevelDestination: Boolean = false
) {
  object SignIn : Screen(route = "signin", name = "SignIn")

  object HomeScreen :
      Screen(route = "homescreen", name = "HomeScreen", isTopLevelDestination = true)

  object AssociationBrowser :
      Screen(
          route = "associationbrowser", name = "AssociationBrowser", isTopLevelDestination = true)

  object Calendar : Screen(route = "calendar", name = "Calendar", isTopLevelDestination = true)

  object Settings : Screen(route = "settings", name = "Settings", isTopLevelDestination = true)

  object AssociationDetails : Screen(route = "associationdetails", name = "AssociationDetails")

  object EventDetails : Screen(route = "eventdetails", name = "EventDetails")
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
      if (screen !is Screen.SignIn) {
        // Restore state when reselecting a previously selected item
        restoreState = true
      }
    }
  }

  /** Navigate to a screen with an ID parameter (e.g., event details, association details). */
  open fun navigateToScreenWithId(screen: Screen, id: String) {
    val route = "${screen.route}/$id"
    navController.navigate(route)
  }

  /** Convenience helper to navigate to event details with a concrete id. */

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

  fun navigateToEventDetails(eventId: String) {
    val route = "${Screen.EventDetails.route}/$eventId"
    navController.navigate(route)
  }

  fun navigateToAssociationDetails(associationId: String) {
    val route = "${Screen.AssociationDetails.route}/$associationId"
    navController.navigate(route)
  }
}
