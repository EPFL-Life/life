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
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
    credentialManager: CredentialManager,
    onSignedOut: () -> Unit,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  LaunchedEffect(uiState.signInState) {
    when (uiState.signInState) {
      is SignInState.SignedIn -> {
        /* show ui as usual */
      }
      is SignInState.SignOutFailed -> {
        val message = (uiState.signInState as SignInState.SignOutFailed).message
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
      }
      SignInState.SignedOut -> {
        onSignedOut()
        Toast.makeText(context, "Sign out successful", Toast.LENGTH_SHORT).show()
      }
    }
  }
  Box(
      modifier = modifier.fillMaxSize().testTag(NavigationTestTags.SETTINGS_SCREEN),
      contentAlignment = Alignment.Center,
  ) {
    Text(text = "SettingsScreen")
    Button(onClick = { viewModel.signOut(credentialManager) }) { Text("Sign out") }
  }
}
