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

  object EditDisplayName : Screen(route = "edit_display_name", name = "EditDisplayName")

  object LanguageSelection : Screen(route = "language_selection", name = "LanguageSelection")

  object AssociationDetails : Screen(route = "associationdetails", name = "AssociationDetails")

  object EventDetails : Screen(route = "eventdetails", name = "EventDetails")

  object Map : Screen(route = "map", name = "Map")

  object SelectAssociation : Screen("selectassociation", name = "SelectAssociation")

  object AddEditAssociation :
      Screen(
          route = "add_edit_association?associationId={associationId}",
          name = "AddEditAssociation") {
    private const val BASE_ROUTE = "add_edit_association"
    const val ASSOCIATION_ID_ARG = "associationId"

    fun createRoute(associationId: String? = null): String =
        if (associationId.isNullOrBlank()) BASE_ROUTE
        else "$BASE_ROUTE?$ASSOCIATION_ID_ARG=$associationId"
  }

  object ManageEvents : Screen("manage_events/{associationId}", name = "ManageEvents") {
    const val ARG_ASSOCIATION_ID = "associationId"

    fun createRoute(associationId: String) = "manage_events/$associationId"
  }

  object AddEditEvent : Screen("add_edit_event", "AddEditEvent") {
    const val ARG_ASSOCIATION_ID = "associationId"
    const val ARG_EVENT_ID = "eventId"

    const val ROUTE_ADD = "add_edit_event/{associationId}"
    const val ROUTE_EDIT = "add_edit_event/{associationId}/{eventId}"

    fun createRouteAdd(associationId: String) = "add_edit_event/$associationId"

    fun createRouteEdit(associationId: String, eventId: String) =
        "add_edit_event/$associationId/$eventId"
  }

  object AssociationAdmin :
      Screen("association_admin?associationId={associationId}", "AssociationAdmin") {
    const val ARG_ASSOCIATION_ID = "associationId"

    fun createRoute(associationId: String? = null) =
        if (associationId == null) "association_admin"
        else "association_admin?associationId=$associationId"
  }
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
  fun navigateToEventDetails(eventId: String) {
    val route = "${Screen.EventDetails.route}/$eventId"
    navController.navigate(route)
  }

  fun navigateToAssociationDetails(associationId: String) {
    navigateToScreenWithId(Screen.AssociationDetails, associationId)
  }

  fun navigateToAddEditAssociation(associationId: String? = null) {
    navController.navigate(Screen.AddEditAssociation.createRoute(associationId))
  }

  fun navigateToEditDisplayName() {
    navController.navigate(Screen.EditDisplayName.route)
  }

  fun navigateToManageEvents(associationId: String) {
    navController.navigate(Screen.ManageEvents.createRoute(associationId))
  }

  fun navigateToAddEditEvent(associationId: String, eventId: String? = null) {
    val route =
        if (eventId == null) Screen.AddEditEvent.createRouteAdd(associationId)
        else Screen.AddEditEvent.createRouteEdit(associationId, eventId)

    navController.navigate(route)
  }

  fun navigateToAssociationAdmin(associationId: String? = null) {
    navController.navigate(Screen.AssociationAdmin.createRoute(associationId))
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
