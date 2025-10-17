package ch.epfllife.ui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfllife.ui.navigation.BottomNavigationMenu
import ch.epfllife.ui.navigation.NavigationActions
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab

@Composable
fun Settings(
    navigationActions: NavigationActions? = null,
) {
  Scaffold(
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Settings,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      }) { innerPadding ->
        Text(text = "Settings", modifier = Modifier.padding(innerPadding))
      }
}
