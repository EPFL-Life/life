package ch.epfllife.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ch.epfllife.model.association.AssociationRepository
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.composables.SettingsButton
import ch.epfllife.ui.settings.SettingsViewModel
import ch.epfllife.ui.theme.DangerRed
import ch.epfllife.ui.theme.LifeRed
import ch.epfllife.utils.SystemToastHelper
import ch.epfllife.utils.ToastHelper

object AssociationAdminScreenTestTags {
  const val SCREEN = "associationAdminScreen"
  const val SELECT_ASSOCIATION_BUTTON = "selectAssociationButton"
  const val MANAGE_ASSOCIATION_BUTTON = "manageAssociationButton"
  const val MANAGE_EVENTS_BUTTON = "manageAssociationEventsButton"
  const val DELETE_ASSOCIATION_BUTTON = "deleteAssociationButton"

  const val BACK_BUTTON = "backButton"
}

@Composable
fun AssociationAdminScreen(
    modifier: Modifier = Modifier,
    auth: Auth,
    associationRepository: AssociationRepository,
    associationId: String? = null,
    associationName: String? = null,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel(auth, associationRepository) },
    onSelectAssociationClick: () -> Unit,
    onManageAssociationClick: (String) -> Unit,
    onManageAssociationEventsClick: (String) -> Unit,
    onAssociationDeleted: () -> Unit,
    onGoBack: () -> Unit,
    toastHelper: ToastHelper = SystemToastHelper()
) {
  val context = LocalContext.current
  var showDeleteDialog by remember { mutableStateOf(false) }

  if (showDeleteDialog && associationId != null) {
    AlertDialog(
        onDismissRequest = { showDeleteDialog = false },
        title = { Text(text = stringResource(R.string.delete_association_title)) },
        text = { Text(text = stringResource(R.string.delete_association_confirmation)) },
        confirmButton = {
          TextButton(
              onClick = {
                viewModel.deleteAssociation(
                    associationId = associationId,
                    onSuccess = {
                      showDeleteDialog = false
                      toastHelper.show(
                          context, context.getString(R.string.delete_association_success))
                      onAssociationDeleted()
                    },
                    // can happen if a association was already deleted before
                    onFailure = {
                      showDeleteDialog = false
                      toastHelper.show(
                          context, context.getString(R.string.delete_association_failure))
                    })
              }) {
                Text(stringResource(R.string.delete_button), color = DangerRed)
              }
        },
        dismissButton = {
          TextButton(onClick = { showDeleteDialog = false }) {
            Text(stringResource(R.string.cancel_button))
          }
        })
  }

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .padding(horizontal = 15.dp)
              .testTag(AssociationAdminScreenTestTags.SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Top) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.admin_console),
            style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(32.dp))

        SettingsButton(
            text =
                associationName?.let { stringResource(R.string.settings_selected_association, it) }
                    ?: stringResource(R.string.settings_screen_association),
            onClick = onSelectAssociationClick,
            modifier =
                Modifier.fillMaxWidth()
                    .testTag(AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON))

        Spacer(Modifier.height(16.dp))

        if (associationId != null) {
          SettingsButton(
              text = stringResource(R.string.manage_association, associationName ?: ""),
              onClick = { onManageAssociationClick(associationId) },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON))

          Spacer(Modifier.height(16.dp))

          SettingsButton(
              text = stringResource(R.string.manage_events_title),
              onClick = { onManageAssociationEventsClick(associationId) },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON))

          Spacer(Modifier.height(16.dp))

          Button(
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AssociationAdminScreenTestTags.DELETE_ASSOCIATION_BUTTON),
              onClick = { showDeleteDialog = true },
              shape = RoundedCornerShape(6.dp),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = LifeRed, contentColor = Color.White)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                  Text(
                      text = stringResource(R.string.delete_association_button),
                      style =
                          MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                }
              }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth().testTag(AssociationAdminScreenTestTags.BACK_BUTTON),
            onClick = onGoBack,
            shape = RoundedCornerShape(6.dp)) {
              Text(stringResource(R.string.back_button_description))
            }
      }
}
