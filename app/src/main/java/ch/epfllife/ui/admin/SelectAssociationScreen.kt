package ch.epfllife.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.ui.composables.AssociationCard
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.Refreshable
import ch.epfllife.ui.composables.SettingsButton

object SelectAssociationTestTags {
  const val ASSOCIATION_LIST = "SelectAssociation_AssociationList"
  const val ADD_NEW_BUTTON = "SelectAssociation_AddNewButton"

  fun associationCard(id: String) = "SelectAssociation_Card_$id"
}

@Composable
fun SelectAssociationScreen(
    db: Db,
    onGoBack: () -> Unit,
    onAssociationSelected: (Association) -> Unit,
    onAddNewAssociation: () -> Unit = {},
    viewModel: SelectAssociationViewModel = viewModel { SelectAssociationViewModel(db) }
) {
  val uiState by viewModel.uiState.collectAsState()

  Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
    Refreshable(onRefresh = { finishRefreshing -> viewModel.reload { finishRefreshing() } }) {
      when (uiState) {
        is SelectAssociationUIState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }

        is SelectAssociationUIState.Error -> {
          Column(
              modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.error_loading_association),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.reload() }) { Text(stringResource(R.string.retry)) }
              }
        }

        is SelectAssociationUIState.Success -> {
          val state = uiState as SelectAssociationUIState.Success
          val associations = state.associations

          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .verticalScroll(rememberScrollState())
                      .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                      .testTag(SelectAssociationTestTags.ASSOCIATION_LIST),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // --- Header Title ---
                Text(
                    text = stringResource(R.string.settings_screen_association),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))

                // Add New Association button
                SettingsButton(
                    text = stringResource(R.string.add_new_association),
                    onClick = onAddNewAssociation,
                    modifier = Modifier.testTag(SelectAssociationTestTags.ADD_NEW_BUTTON))

                // List of associations
                associations.forEach { association ->
                  AssociationCard(
                      association = association,
                      testTag = SelectAssociationTestTags.associationCard(association.id),
                      onClick = {
                        viewModel.selectAssociation(association.id)
                        onAssociationSelected(association)
                      })
                }
              }
        }
      }
    }

    // --- Back Button Overlay ---
    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onGoBack)
  }
}
