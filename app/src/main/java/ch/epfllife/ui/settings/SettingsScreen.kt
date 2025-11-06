package ch.epfllife.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    auth: Auth,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel(auth) },
    onSignedOut: () -> Unit,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  LaunchedEffect(uiState.signInState) {
    if (uiState.signInState is SignInState.SignedOut) {
      onSignedOut()
      Toast.makeText(context, R.string.signout_successful, Toast.LENGTH_SHORT).show()
    }
  }
  Box(
      modifier = modifier.fillMaxSize().testTag(NavigationTestTags.SETTINGS_SCREEN),
      contentAlignment = Alignment.Center,
  ) {
    Text(text = "SettingsScreen")
    Button(
        modifier = modifier.testTag(SettingsScreenTestTags.SIGN_OUT_BUTTON),
        onClick = { viewModel.signOut() },
    ) {
      Text("Sign out")
    }
  }
}

object SettingsScreenTestTags {
  const val SIGN_OUT_BUTTON = "signOutButton"
}
