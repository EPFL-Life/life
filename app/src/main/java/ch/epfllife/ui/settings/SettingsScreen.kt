package ch.epfllife.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.UserRole
import ch.epfllife.ui.composables.SettingsButton
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.theme.LifeRed
import ch.epfllife.utils.SystemToastHelper
import ch.epfllife.utils.ToastHelper

object SettingsScreenTestTags {
  const val SIGN_OUT_BUTTON = "signOutButton"
  const val ADMIN_CONSOLE_BUTTON = "adminConsoleButton"
}

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    auth: Auth,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel(auth, Db.firestore) },
    onSignedOut: () -> Unit,
    toastHelper: ToastHelper = SystemToastHelper(),
    onAdminConsoleClick: () -> Unit,
    onNavigateToManageProfile: () -> Unit
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  LaunchedEffect(uiState.signInState) {
    if (uiState.signInState is SignInState.SignedOut) {
      onSignedOut()
      toastHelper.show(context, R.string.signout_successful)
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

        SettingsButton(
            text = "Manage Profile",
            icon = Icons.Default.Person,
            onClick = { onNavigateToManageProfile() })
        Spacer(Modifier.height(32.dp))

        if (uiState.userRole == UserRole.ADMIN || uiState.userRole == UserRole.ASSOCIATION_ADMIN) {
          SettingsButton(
              text = stringResource(R.string.admin_console),
              icon = Icons.Default.Build,
              onClick = onAdminConsoleClick,
              modifier =
                  Modifier.fillMaxWidth().testTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON))
          Spacer(Modifier.height(32.dp))
        }

        Spacer(Modifier.weight(1f))

        Button(
            modifier =
                Modifier.fillMaxWidth()
                    .height(56.dp)
                    .testTag(SettingsScreenTestTags.SIGN_OUT_BUTTON),
            onClick = { viewModel.signOut() },
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(containerColor = LifeRed, contentColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)) {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sign_out),
                        style =
                            MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold))
                  }
            }

        Spacer(Modifier.height(24.dp))
      }
}
