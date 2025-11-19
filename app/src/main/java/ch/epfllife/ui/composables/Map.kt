package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfllife.model.map.Location
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

object MapTestTags {
  const val MAP_LOCATION_ENABLED = "MAP_LOCATION_ENABLED"
  const val MAP_LOCATION_DISABLED = "MAP_LOCATION_DISABLED"
}

/**
 * A composable that displays a map centered on the given target location. The target location is
 * marked with a marker. If [enableControls] is false, the map is shown with limited controls for
 * better embedding in other UIs.
 *
 * It requests location permission, and if granted, shows the current user location on the map as
 * well.
 */
@Composable
fun Map(
    target: Location,
    enableControls: Boolean,
    locationPermissionRequest: @Composable ((Boolean) -> Unit) -> Unit,
    compassEnabled: Boolean = enableControls,
) {
  val targetPos = LatLng(target.latitude, target.longitude)
  val cameraStartPos = CameraPosition.fromLatLngZoom(targetPos, 15f)
  var isLocationGranted by remember { mutableStateOf(false) }
  val showLocationControls by remember { derivedStateOf { isLocationGranted && enableControls } }

  locationPermissionRequest { isGranted -> isLocationGranted = isGranted }

  GoogleMap(
      modifier =
          Modifier.fillMaxSize()
              .testTag(
                  if (showLocationControls) MapTestTags.MAP_LOCATION_ENABLED
                  else MapTestTags.MAP_LOCATION_DISABLED),
      cameraPositionState = CameraPositionState(cameraStartPos),
      properties = MapProperties(isMyLocationEnabled = isLocationGranted),
      uiSettings =
          MapUiSettings(
              myLocationButtonEnabled = showLocationControls,
              compassEnabled = compassEnabled,
              zoomControlsEnabled = enableControls,
              scrollGesturesEnabled = enableControls,
              zoomGesturesEnabled = enableControls,
              tiltGesturesEnabled = enableControls,
              rotationGesturesEnabled = enableControls,
          ),
  ) {
    Marker(
        state = MarkerState(position = targetPos),
        title = target.name,
    )
  }
}
