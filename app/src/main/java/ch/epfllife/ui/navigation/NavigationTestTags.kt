package ch.epfllife.ui.navigation

object NavigationTestTags {

  const val BOTTOM_NAVIGATION_MENU = "BottomNavigationMenu"
  const val GO_BACK_BUTTON = "GoBackButton"
  const val TOP_BAR_TITLE = "TopBarTitle"
  const val HOMESCREEN_TAB = "HomeScreen"
  const val ASSOCIATIONBROWSER_TAB = "AssociationBrowser"
  const val MYEVENTS_TAB = "MyEvents"
  const val SETTINGS_TAB = "Settings"

  fun getTabTestTag(tab: Tab): String =
      when (tab) {
        is Tab.HomeScreen -> HOMESCREEN_TAB
        is Tab.AssociationBrowser -> ASSOCIATIONBROWSER_TAB
        is Tab.MyEvents -> MYEVENTS_TAB
        is Tab.Settings -> SETTINGS_TAB
      }
}
