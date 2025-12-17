package ch.epfllife.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.SubmitButton
import coil.compose.AsyncImage

// handle the screen (e.g. loading/error/success)
@Composable
fun ManageProfileScreen(
    db: Db,
    viewModel: ManageProfileViewModel = viewModel { ManageProfileViewModel(db) },
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()

  Box(modifier = Modifier.fillMaxSize()) {
    when (val state = uiState) {
      ManageProfileUiState.Loading -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }

      is ManageProfileUiState.Error -> {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = stringResource(state.messageRes), color = MaterialTheme.colorScheme.error)

              if (state.details != null) {
                Text(
                    text = state.details,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
              }
              Spacer(Modifier.height(16.dp))
              Button(onClick = onBack) { Text(stringResource(R.string.back_button_description)) }
            }
      }

      ManageProfileUiState.Success -> {
        ManageProfileContent(viewModel, onSubmitSuccess)
      }
    }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
  }
}

// handle the actual (success) screen
@Composable
private fun ManageProfileContent(viewModel: ManageProfileViewModel, onSubmitSuccess: () -> Unit) {
  val scrollState = rememberScrollState()

  val photoLauncher =
      rememberLauncherForActivityResult(
          contract = ActivityResultContracts.PickVisualMedia(),
          onResult = { uri -> if (uri != null) viewModel.onPhotoSelected(uri) })

  Column(
      modifier =
          Modifier.fillMaxSize()
              .verticalScroll(scrollState)
              .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text =
                "Manage Profile", // hardcoded per requirement "rebranded to manage profile" (maybe
            // try to change to use Resource (tests are weirs though))
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

        HorizontalDivider()

        // Profile Picture Section
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Box(
              modifier =
                  Modifier.size(120.dp)
                      .clip(CircleShape)
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                      .clickable(enabled = !viewModel.isUploadingPhoto) {
                        photoLauncher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly))
                      },
              contentAlignment = Alignment.Center) {
                if (viewModel.photoUrl.isNotBlank()) {
                  AsyncImage(
                      model = viewModel.photoUrl,
                      contentDescription = "Profile Picture",
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize())
                } else {
                  Icon(
                      imageVector = Icons.Default.Person,
                      contentDescription = "Placeholder",
                      modifier = Modifier.size(60.dp),
                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (viewModel.isUploadingPhoto) {
                  CircularProgressIndicator()
                }
              }
        }

        Text(
            text = "Tap to change profile picture",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))

        Spacer(Modifier.height(8.dp))

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
