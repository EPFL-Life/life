package ch.epfllife.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.ui.association.SocialIcons
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.theme.LifeRed

object AddEditAssociationTestTags {
  const val HEADER = "AddEditAssociation_Header"
  const val NAME_FIELD = "AddEditAssociation_NameField"
  const val DESCRIPTION_FIELD = "AddEditAssociation_DescriptionField"
  const val ABOUT_FIELD = "AddEditAssociation_AboutField"
  const val SUBMIT_BUTTON = "AddEditAssociation_SubmitButton"
}

@Composable
fun AddEditAssociationScreen(
    viewModel: AddEditAssociationViewModel = viewModel(),
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
  val scrollState = rememberScrollState()
  val formState = viewModel.formState

  Box(modifier = Modifier.fillMaxSize()) {

    // Scrollable form content
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    top = 72.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp), // leave space for back arrow
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
          // --- Header ---
          Text(
              text = stringResource(R.string.add_new_association),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.testTag(AddEditAssociationTestTags.HEADER))
          HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant)

          // --- General Info ---
          Text(
              text = stringResource(R.string.general_info),
              color = MaterialTheme.colorScheme.outline,
              style = MaterialTheme.typography.titleSmall)
          HorizontalDivider(color = MaterialTheme.colorScheme.outline)

          OutlinedTextField(
              value = formState.name,
              onValueChange = { viewModel.updateName(it) },
              label = { Text(stringResource(R.string.association_name_required)) },
              modifier = Modifier.fillMaxWidth().testTag(AddEditAssociationTestTags.NAME_FIELD))

          OutlinedTextField(
              value = formState.description,
              onValueChange = { viewModel.updateDescription(it) },
              label = { Text(stringResource(R.string.short_description_required)) },
              modifier =
                  Modifier.fillMaxWidth().testTag(AddEditAssociationTestTags.DESCRIPTION_FIELD))

          OutlinedTextField(
              value = formState.about,
              onValueChange = { viewModel.updateAbout(it) },
              label = { Text(stringResource(R.string.about_association_required)) },
              modifier =
                  Modifier.fillMaxWidth()
                      .height(120.dp)
                      .testTag(AddEditAssociationTestTags.ABOUT_FIELD))

          // --- Social Pages ---
          Text(
              text = stringResource(R.string.social_pages_title),
              color = MaterialTheme.colorScheme.outline,
              style = MaterialTheme.typography.titleSmall)
          HorizontalDivider(color = MaterialTheme.colorScheme.outline)

          formState.socialMedia.forEach { sm ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                  Checkbox(
                      checked = sm.enabled,
                      onCheckedChange = { viewModel.updateSocialMedia(sm.platform, it) },
                      modifier = Modifier.padding(0.dp))

                  Spacer(modifier = Modifier.width(8.dp))

                  Icon(
                      painter =
                          painterResource(
                              SocialIcons.getIcon(sm.platform) ?: R.drawable.ic_default),
                      contentDescription = sm.platform,
                      tint = Color.Unspecified,
                      modifier = Modifier.size(32.dp))

                  Spacer(modifier = Modifier.width(16.dp))

                  OutlinedTextField(
                      value = sm.link,
                      onValueChange = { viewModel.updateSocialMediaLink(sm.platform, it) },
                      label = { Text(stringResource(R.string.website_description)) },
                      enabled = sm.enabled,
                      modifier = Modifier.weight(1f))
                }
          }

          // --- Upload Images (URLs) ---
          Text(
              text = stringResource(R.string.upload_images),
              color = MaterialTheme.colorScheme.outline,
              style = MaterialTheme.typography.titleSmall)
          HorizontalDivider(color = MaterialTheme.colorScheme.outline)

          OutlinedTextField(
              value = formState.logoUrl,
              onValueChange = { viewModel.updateLogoUrl(it) },
              label = { Text(stringResource(R.string.logo_url)) },
              modifier = Modifier.fillMaxWidth())

          OutlinedTextField(
              value = formState.bannerUrl,
              onValueChange = { viewModel.updateBannerUrl(it) },
              label = { Text(stringResource(R.string.banner_url)) },
              modifier = Modifier.fillMaxWidth())

          Spacer(Modifier.height(24.dp))

          // --- Submit Button ---
          Button(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(50.dp)
                      .testTag(AddEditAssociationTestTags.SUBMIT_BUTTON),
              onClick = { viewModel.submit(onSubmitSuccess) },
              shape = RoundedCornerShape(6.dp),
              colors =
                  ButtonDefaults.buttonColors(containerColor = LifeRed, contentColor = Color.White),
              enabled = viewModel.isFormValid()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                  Text(
                      text = stringResource(R.string.submit),
                      style =
                          MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                }
              }

          Spacer(Modifier.height(24.dp))
        }

    // --- Back Button Overlay ---
    BackButton(onGoBack = onBack)
  }
}
