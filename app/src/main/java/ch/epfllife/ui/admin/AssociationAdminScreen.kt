package ch.epfllife.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.House
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.db.Db
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.SettingsButton
import ch.epfllife.ui.settings.SettingsViewModel
import ch.epfllife.ui.theme.DangerRed
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
    db: Db,
    associationId: String? = null,
    associationName: String? = null,
    viewModel: SettingsViewModel = viewModel { SettingsViewModel(auth, db) },
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

  Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(horizontal = 15.dp)
                .testTag(AssociationAdminScreenTestTags.SCREEN),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
          Spacer(Modifier.height(72.dp)) // Padding for BackButton

          Text(
              text = stringResource(R.string.admin_console),
              style = MaterialTheme.typography.headlineMedium)

          Spacer(Modifier.height(32.dp))

          SettingsButton(
              text =
                  associationName?.let {
                    stringResource(R.string.settings_selected_association, it)
                  } ?: stringResource(R.string.settings_screen_association),
              icon = Icons.Filled.House,
              onClick = onSelectAssociationClick,
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON))

          Spacer(Modifier.height(16.dp))

          if (associationId != null) {
            SettingsButton(
                text = stringResource(R.string.manage_association, associationName ?: ""),
                icon = Icons.Default.Edit,
                onClick = { onManageAssociationClick(associationId) },
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON))

            Spacer(Modifier.height(16.dp))

            SettingsButton(
                text = stringResource(R.string.manage_events_title),
                icon = Icons.Default.FormatListNumbered,
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
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 5.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = DangerRed, contentColor = Color.White)) {
                  Row(
                      horizontalArrangement = Arrangement.Start,
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp))
                        Text(
                            text = stringResource(R.string.delete_association_button),
                            style =
                                MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold))
                      }
                }
          }
        }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onGoBack)
  }
}
