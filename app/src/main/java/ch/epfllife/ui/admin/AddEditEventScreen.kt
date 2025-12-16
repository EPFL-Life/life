package ch.epfllife.ui.admin

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.BuildConfig
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.map.Location
import ch.epfllife.model.map.NominatimLocationRepository
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.Map
import ch.epfllife.ui.composables.SubmitButton
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import okhttp3.OkHttpClient

object AddEditEventTestTags {
  const val TITLE_FIELD = "AddEditEvent_TitleField"
  const val DESCRIPTION_FIELD = "AddEditEvent_DescriptionField"
  const val TIME_FIELD = "AddEditEvent_TimeField"
  const val TIME_PICKER_BOX = "AddEditEvent_TimePickerBox"
  const val LOCATION_FIELD = "AddEditEvent_LocationField"
  const val SUBMIT_BUTTON = "AddEditEvent_SubmitButton"
}

@Composable
fun AddEditEventScreen(
    db: Db,
    associationId: String,
    eventId: String? = null,
    viewModel: AddEditEventViewModel? = null,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit,
    onPreviewLocation: (Location) -> Unit = {},
) {
  val locationRepository = remember {
    val ua = "${BuildConfig.APPLICATION_ID}/${BuildConfig.VERSION_NAME} (contact@epfllife.app)"
    NominatimLocationRepository(OkHttpClient(), userAgent = ua, referer = "https://epfllife.app")
  }
  val finalViewModel: AddEditEventViewModel =
      viewModel
          ?: viewModel { AddEditEventViewModel(db, associationId, eventId, locationRepository) }

  val uiState by finalViewModel.uiState.collectAsState()

  Box(modifier = Modifier.fillMaxSize()) {
    when (val state = uiState) {
      is AddEditEventUIState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is AddEditEventUIState.Error -> {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                  text = stringResource(state.messageRes),
                  color = MaterialTheme.colorScheme.error,
                  style = MaterialTheme.typography.bodyLarge)
              Spacer(modifier = Modifier.height(16.dp))
              Button(onClick = onBack) { Text(stringResource(R.string.back_button_description)) }
            }
      }
      is AddEditEventUIState.Success -> {
        AddEditEventContent(
            viewModel = finalViewModel,
            isEditing = eventId != null,
            onSubmitSuccess = onSubmitSuccess,
            onPreviewLocation = onPreviewLocation)
      }
    }

    if (uiState !is AddEditEventUIState.Error) {
      BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
    }
  }
}

@Composable
private fun AddEditEventContent(
    viewModel: AddEditEventViewModel,
    isEditing: Boolean,
    onSubmitSuccess: () -> Unit,
    onPreviewLocation: (Location) -> Unit
) {
  val formState by viewModel.formState.collectAsState()
  val scroll = rememberScrollState()
  val context = LocalContext.current
  val dateTimeFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
  val interactionSource = remember { MutableInteractionSource() }

  fun parseCurrentDateTime(): LocalDateTime {
    return try {
      LocalDateTime.parse(formState.time, dateTimeFormatter)
    } catch (_: Exception) {
      LocalDateTime.now()
    }
  }

  fun showTimePicker(selectedDate: LocalDate) {
    val initial = parseCurrentDateTime()
    TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
              val combinedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute))
              viewModel.updateTime(combinedDateTime.format(dateTimeFormatter))
            },
            initial.hour,
            initial.minute,
            true)
        .show()
  }

  fun showDatePicker() {
    val initial = parseCurrentDateTime()
    DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
              val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
              showTimePicker(selectedDate)
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth)
        .show()
  }

  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scroll)
              .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // --- Header ---
        Text(
            text =
                if (!isEditing) stringResource(R.string.add_new_event)
                else stringResource(R.string.edit_event),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)

        // --- General Info ---
        Text(
            text = stringResource(R.string.general_info),
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.titleSmall)
        HorizontalDivider()

        OutlinedTextField(
            value = formState.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text(stringResource(R.string.event_title_required)) },
            modifier = Modifier.fillMaxWidth().testTag(AddEditEventTestTags.TITLE_FIELD))

        OutlinedTextField(
            value = formState.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text(stringResource(R.string.event_description_required)) },
            modifier =
                Modifier.fillMaxWidth()
                    .height(120.dp)
                    .testTag(AddEditEventTestTags.DESCRIPTION_FIELD))

        Box {
          OutlinedTextField(
              value = formState.time,
              onValueChange = {},
              label = { Text(stringResource(R.string.event_time_required)) },
              readOnly = true,
              modifier = Modifier.fillMaxWidth().testTag(AddEditEventTestTags.TIME_FIELD))
          Box(
              modifier =
                  Modifier.matchParentSize()
                      .clickable(
                          interactionSource = interactionSource,
                          indication = null,
                          onClick = { showDatePicker() })
                      .testTag(AddEditEventTestTags.TIME_PICKER_BOX))
        }

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

        // --- Location Section ---
        Text(
            text = stringResource(R.string.event_location_section_title),
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.titleSmall)
        HorizontalDivider()

        OutlinedTextField(
            value = formState.locationName,
            onValueChange = { viewModel.updateLocationName(it) },
            label = { Text(stringResource(R.string.event_location_name_label)) },
            modifier = Modifier.fillMaxWidth().testTag(AddEditEventTestTags.LOCATION_FIELD))

        Button(
            onClick = { viewModel.onManualLocationLookup() },
            enabled = formState.locationName.isNotBlank() && !formState.isLocationSearching) {
              Text(stringResource(R.string.event_location_search))
            }

        if (formState.isLocationSearching) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                Text(stringResource(R.string.event_location_searching))
              }
        }

        formState.locationErrorRes?.let { res ->
          Text(
              text = stringResource(res),
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall)
        }

        if (formState.locationLatitude != null && formState.locationLongitude != null) {
          val resolvedName = formState.resolvedLocationName
          Text(
              text = stringResource(R.string.event_location_coordinates),
              style = MaterialTheme.typography.titleMedium)
          resolvedName?.let { resolved ->
            Text(
                text = stringResource(R.string.event_location_resolved_description, resolved),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          OutlinedTextField(
              value = formState.locationLatitude.toString(),
              onValueChange = {},
              readOnly = true,
              label = { Text(stringResource(R.string.event_location_latitude)) },
              modifier = Modifier.fillMaxWidth())
          OutlinedTextField(
              value = formState.locationLongitude.toString(),
              onValueChange = {},
              readOnly = true,
              label = { Text(stringResource(R.string.event_location_longitude)) },
              modifier = Modifier.fillMaxWidth())

          val previewLocation =
              remember(formState.locationLatitude, formState.locationLongitude, resolvedName) {
                Location(
                    latitude = formState.locationLatitude!!,
                    longitude = formState.locationLongitude!!,
                    name = resolvedName ?: formState.locationName)
              }

          Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))) {
            Map(
                target = previewLocation,
                enableControls = false,
                locationPermissionRequest = { result ->
                  val granted =
                      ContextCompat.checkSelfPermission(
                          context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                          PackageManager.PERMISSION_GRANTED
                  result(granted)
                })
            Spacer(
                modifier =
                    Modifier.matchParentSize().clickable { onPreviewLocation(previewLocation) })
          }
        }

        Spacer(Modifier.height(12.dp))

        SubmitButton(
            modifier =
                Modifier.fillMaxWidth().height(50.dp).testTag(AddEditEventTestTags.SUBMIT_BUTTON),
            enabled = viewModel.isFormValid(),
            onClick = { viewModel.submit(onSubmitSuccess) })
      }
}
