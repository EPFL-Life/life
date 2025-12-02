package ch.epfllife.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.theme.LifeRed

@Composable
fun AddEditEventScreen(
    db: Db,
    association: Association,
    initialEvent: Event? = null,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    viewModel: AddEditEventViewModel = viewModel {
      AddEditEventViewModel(db, association, initialEvent)
    }
) {
  val formState by viewModel.formState.collectAsState()
  val scroll = rememberScrollState()

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(scroll)
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
              text =
                  if (initialEvent == null) stringResource(R.string.add_new_event)
                  else stringResource(R.string.edit_event),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

          Divider()

          OutlinedTextField(
              value = formState.title,
              onValueChange = { viewModel.updateTitle(it) },
              label = { Text(stringResource(R.string.event_title_required)) },
              modifier = Modifier.fillMaxWidth())

          OutlinedTextField(
              value = formState.description,
              onValueChange = { viewModel.updateDescription(it) },
              label = { Text(stringResource(R.string.event_description_required)) },
              modifier = Modifier.fillMaxWidth().height(120.dp))

          OutlinedTextField(
              value = formState.locationName,
              onValueChange = { viewModel.updateLocationName(it) },
              label = { Text(stringResource(R.string.event_location_required)) },
              modifier = Modifier.fillMaxWidth())

          OutlinedTextField(
              value = formState.time,
              onValueChange = { viewModel.updateTime(it) },
              label = { Text(stringResource(R.string.event_time_required)) },
              modifier = Modifier.fillMaxWidth())

          OutlinedTextField(
              value = formState.priceText,
              onValueChange = { viewModel.updatePriceText(it) },
              label = { Text(stringResource(R.string.event_price_optional)) },
              modifier = Modifier.fillMaxWidth())

          OutlinedTextField(
              value = formState.tagsText,
              onValueChange = { viewModel.updateTagsText(it) },
              label = { Text(stringResource(R.string.event_tags)) },
              modifier = Modifier.fillMaxWidth())

          OutlinedTextField(
              value = formState.pictureUrl,
              onValueChange = { viewModel.updatePictureUrl(it) },
              label = { Text(stringResource(R.string.event_picture_url)) },
              modifier = Modifier.fillMaxWidth())

          Spacer(Modifier.height(12.dp))

          Button(
              onClick = { viewModel.submit(onSubmitSuccess) },
              enabled = viewModel.isFormValid(),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = LifeRed, contentColor = MaterialTheme.colorScheme.onPrimary),
              modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                  Text(stringResource(R.string.submit))
                }
              }
        }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
  }
}
