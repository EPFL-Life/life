package ch.epfllife.ui.composables

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.rule.GrantPermissionRule
import ch.epfllife.model.map.Location
import org.junit.Rule
import org.junit.Test

class MapTest {
  // Grant location permission for all tests.
  // The `GoogleMap` composable fails otherwise,
  // if location settings are enabled.
  // The logic is actually tested by providing a separate callback
  // that just returns true or false
  @get:Rule
  // This is a nullable platform type, so we need to specify the type explicitly
  val permissionRule: GrantPermissionRule? =
      GrantPermissionRule.grant(
          Manifest.permission.ACCESS_FINE_LOCATION,
      )

  @get:Rule val composeTestRule = createComposeRule()
  val location = Location(46.520278, 6.565556, "EPFL")

  fun useMapWith(enabledControls: Boolean, permissionGranted: Boolean) {
    composeTestRule.setContent {
      Map(
          target = location,
          enableControls = enabledControls,
          locationPermissionRequest = { it(permissionGranted) },
      )
    }
  }

  fun assertLocationControlStatus(
      enabledControls: Boolean,
      permissionGranted: Boolean,
      expectedShown: Boolean,
  ) {
    useMapWith(enabledControls = enabledControls, permissionGranted = permissionGranted)
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(
            if (expectedShown) MapTestTags.MAP_LOCATION_ENABLED
            else MapTestTags.MAP_LOCATION_DISABLED,
            useUnmergedTree = true,
        )
        .assertExists()
  }

  @Test
  fun showsLocationControlsWhenGranted() =
      assertLocationControlStatus(
          enabledControls = true,
          permissionGranted = true,
          expectedShown = true,
      )

  @Test
  fun hidesLocationControlsWhenNotGranted() =
      assertLocationControlStatus(
          enabledControls = true,
          permissionGranted = false,
          expectedShown = false,
      )

  @Test
  fun hidesLocationWhenControlsDisabledWithPermissionGranted() =
      assertLocationControlStatus(
          enabledControls = false,
          permissionGranted = true,
          expectedShown = false,
      )

  @Test
  fun hidesLocationWhenControlsDisabledWithPermissionDenied() =
      assertLocationControlStatus(
          enabledControls = false,
          permissionGranted = false,
          expectedShown = false,
      )
}
