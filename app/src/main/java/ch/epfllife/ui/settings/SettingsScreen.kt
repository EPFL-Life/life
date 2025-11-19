package ch.epfllife.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.navigation.NavigationTestTags

object SettingsScreenTestTags {
  const val SIGN_OUT_BUTTON = "signOutButton"
}

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

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = 15.dp)
              .testTag(NavigationTestTags.SETTINGS_SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(32.dp))

        Button(
            modifier = Modifier.fillMaxWidth().testTag(SettingsScreenTestTags.SIGN_OUT_BUTTON),
            onClick = { viewModel.signOut() },
            shape = RoundedCornerShape(6.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626), contentColor = Color.White)) {
              Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Text(
                    text = stringResource(R.string.sign_out),
                    style =
                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
              }
            }
      }
}
