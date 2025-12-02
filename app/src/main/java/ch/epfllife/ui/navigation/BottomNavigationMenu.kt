package ch.epfllife.ui.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

sealed class Tab(val name: String, val icon: ImageVector, val destination: Screen) {
  object HomeScreen : Tab("HomeScreen", Icons.Outlined.Home, Screen.HomeScreen)

  object AssociationBrowser :
      Tab("AssociationBrowser", Icons.Outlined.Groups, Screen.AssociationBrowser)

  object Calendar : Tab("Calendar", Icons.Outlined.CalendarToday, Screen.Calendar)

  object Settings : Tab("Settings", Icons.Outlined.Settings, Screen.Settings)

  companion object {
    val tabs = listOf(HomeScreen, AssociationBrowser, Calendar, Settings)
  }
}

@Composable
fun BottomNavigationMenu(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
) {
  NavigationBar(
      modifier =
          modifier.fillMaxWidth().height(60.dp).testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU),
      containerColor = MaterialTheme.colorScheme.surface,
      content = {
        Tab.tabs.forEach { tab ->
          NavigationBarItem(
              icon = { Icon(tab.icon, contentDescription = null) },
              // label = { Text(tab.name) },
              selected = tab == selectedTab,
              onClick = { onTabSelected(tab) },
              modifier =
                  Modifier.clip(RoundedCornerShape(50.dp))
                      .testTag(NavigationTestTags.getTabTestTag(tab)))
        }
      },
  )
}
