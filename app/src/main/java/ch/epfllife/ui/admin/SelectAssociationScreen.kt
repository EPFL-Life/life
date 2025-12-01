package ch.epfllife.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.ui.composables.AssociationCard
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.Refreshable
import ch.epfllife.ui.composables.SettingsButton

@Composable
fun SelectAssociationScreen(
    db: Db,
    onGoBack: () -> Unit,
    onAssociationSelected: (String) -> Unit,
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
          val message = (uiState as SelectAssociationUIState.Error).message
          Column(
              modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.error_loading_association, message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error)

                Spacer(Modifier.height(16.dp))

                Button(onClick = { viewModel.reload() }) { Text(stringResource(R.string.retry)) }
              }
        }

        is SelectAssociationUIState.Success -> {
          val state = uiState as SelectAssociationUIState.Success
          val associations = state.associations
          val selectedId = state.selectedAssociationId

          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .verticalScroll(rememberScrollState())
                      .padding(top = 80.dp, start = 16.dp, end = 16.dp, bottom = 60.dp),
              verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // Add New Association button
                SettingsButton(
                    text = stringResource(R.string.add_new_association),
                    onClick = onAddNewAssociation)

                // Title
                Text(
                    text = stringResource(R.string.edit_existing_association),
                    style = MaterialTheme.typography.titleMedium)

                HorizontalDivider()

                // List of associations
                associations.forEach { association ->
                  AssociationCard(
                      association = association,
                      onClick = {
                        viewModel.selectAssociation(association.id)
                        onAssociationSelected(association.id)
                      })
                }
              }
        }
      }
    }

    // Back button
    BackButton(
        modifier = Modifier.align(Alignment.TopStart).padding(top = 16.dp, start = 16.dp),
        onGoBack = onGoBack)
  }
}
