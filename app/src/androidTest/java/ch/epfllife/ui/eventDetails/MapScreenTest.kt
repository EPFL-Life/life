package ch.epfllife.ui.eventDetails

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.MapTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import org.junit.Rule
import org.junit.Test

class MapScreenTest {
  @get:Rule
  // This is a nullable platform type, so we need to specify the type explicitly
  val permissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION,
      )

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun contentIsDisplayed() {
    composeTestRule.setContent {
      MapScreen(
          location = Location(46.520278, 6.565556, "EPFL"),
          onGoBack = {},
      )
    }
    composeTestRule.waitForIdle()
    listOf(
            NavigationTestTags.MAP_SCREEN,
            MapScreenTestTags.BACK_BUTTON,
            MapTestTags.MAP_LOCATION_ENABLED,
        )
        .forEach { composeTestRule.onNodeWithTag(it).assertExists() }
  }
}
