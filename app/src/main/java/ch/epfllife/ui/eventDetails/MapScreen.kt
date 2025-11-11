package ch.epfllife.ui.eventDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.LocationPermissionRequest
import ch.epfllife.ui.composables.Map
import ch.epfllife.ui.navigation.NavigationTestTags

object MapScreenTestTags {
  const val BACK_BUTTON = "MAP_SCREEN_BACK_BUTTON"
}

@Composable
fun MapScreen(location: Location, onGoBack: () -> Unit) {
  Box(modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.MAP_SCREEN)) {
    Map(
        target = location,
        enableControls = true,
        locationPermissionRequest = { LocationPermissionRequest(it) },
    )
    BackButton(
        modifier = Modifier.align(Alignment.TopStart).testTag(MapScreenTestTags.BACK_BUTTON),
        onGoBack = onGoBack,
    )
  }
}
