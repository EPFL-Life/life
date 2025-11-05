package ch.epfllife.ui.navigation

object NavigationTestTags {

  const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
  const val GO_BACK_BUTTON = "GoBackButton"
  const val HOMESCREEN_TAB = "HomeScreen"
  const val ASSOCIATIONBROWSER_TAB = "AssociationBrowser"
  const val MYEVENTS_TAB = "MyEvents"
  const val SETTINGS_TAB = "Settings"

  const val HOMESCREEN_SCREEN = "HomeScreen_Screen"
  const val ASSOCIATIONBROWSER_SCREEN = "AssociationBrowser_Screen"
  const val MYEVENTS_SCREEN = "MyEvents_Screen"
  const val SIGN_IN_SCREEN = "SignIn_Screen"
  const val SETTINGS_SCREEN = "Settings_Screen"

  fun getTabTestTag(tab: Tab): String =
      when (tab) {
        is Tab.HomeScreen -> HOMESCREEN_TAB
        is Tab.AssociationBrowser -> ASSOCIATIONBROWSER_TAB
        is Tab.MyEvents -> MYEVENTS_TAB
        is Tab.Settings -> SETTINGS_TAB
      }

  fun getScreenTestTagForTab(tab: Tab): String =
      when (tab) {
        is Tab.HomeScreen -> HOMESCREEN_SCREEN
        is Tab.AssociationBrowser -> ASSOCIATIONBROWSER_SCREEN
        is Tab.MyEvents -> MYEVENTS_SCREEN
        is Tab.Settings -> SETTINGS_SCREEN
      }
}
