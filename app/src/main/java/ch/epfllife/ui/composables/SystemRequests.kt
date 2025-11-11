package ch.epfllife.ui.composables

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun LocationPermissionRequest(callback: (Boolean) -> Unit) {
  val locationPermissionRequest =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission ->
        callback(permission)
      }

  // Request location permission on first composition,
  // but dialog is skipped if the user set a value previously
  LaunchedEffect(Unit) {
    locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
  }
}
