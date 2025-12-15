package ch.epfllife.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.SubmitButton

@Composable
fun EditDisplayNameScreen(
    db: Db,
    viewModel: EditDisplayNameViewModel = viewModel { EditDisplayNameViewModel(db) },
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()

  Box(modifier = Modifier.fillMaxSize()) {
    when (uiState) {
      EditDisplayNameUiState.Loading -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is EditDisplayNameUiState.Error -> {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = stringResource((uiState as EditDisplayNameUiState.Error).messageRes),
                  color = MaterialTheme.colorScheme.error)
              Spacer(Modifier.height(16.dp))
              Button(onClick = onBack) { Text(stringResource(R.string.back_button_description)) }
            }
      }

      EditDisplayNameUiState.Success -> {
        EditDisplayNameContent(viewModel, onSubmitSuccess)
      }
    }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
  }
}

@Composable
private fun EditDisplayNameContent(
    viewModel: EditDisplayNameViewModel,
    onSubmitSuccess: () -> Unit
) {
  val scrollState = rememberScrollState()

  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scrollState)
              .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.edit_display_name),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

        HorizontalDivider()

        OutlinedTextField(
            value = viewModel.displayName,
            onValueChange = viewModel::updateDisplayName,
            label = { Text(stringResource(R.string.display_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true)

        Text(
            text = stringResource(R.string.display_name_helper),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(Modifier.height(24.dp))

        SubmitButton(
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = viewModel.isFormValid(),
            onClick = { viewModel.submit(onSubmitSuccess) })
      }
}
