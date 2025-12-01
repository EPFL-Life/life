package ch.epfllife.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.ui.association.SocialIcons
import ch.epfllife.ui.theme.LifeRed

@Composable
fun AddEditAssociationScreen(
    viewModel: AddEditAssociationViewModel = viewModel(),
    onSubmitSuccess: () -> Unit
) {
  val scrollState = rememberScrollState()
  val formState = viewModel.formState

  Column(
      modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // --- General Info ---
        Text("General Info", color = Color.Gray, style = MaterialTheme.typography.titleSmall)
        HorizontalDivider(color = Color.Gray)

        OutlinedTextField(
            value = formState.name,
            onValueChange = { viewModel.updateName(it) },
            label = { Text("Association Name*") },
            modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = formState.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text("Short Description*") },
            modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = formState.about,
            onValueChange = { viewModel.updateAbout(it) },
            label = { Text("About the Association*") },
            modifier = Modifier.fillMaxWidth().height(120.dp))

        // --- Social Pages ---
        Text("Social Pages", color = Color.Gray, style = MaterialTheme.typography.titleSmall)
        HorizontalDivider(color = Color.Gray)

        formState.socialMedia.forEach { sm ->
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = sm.enabled,
                onCheckedChange = { viewModel.updateSocialMedia(sm.platform, it) },
                modifier = Modifier.padding(0.dp))

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                painter =
                    painterResource(SocialIcons.getIcon(sm.platform) ?: R.drawable.ic_default),
                contentDescription = sm.platform,
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp))

            Spacer(modifier = Modifier.width(16.dp))

            OutlinedTextField(
                value = sm.link,
                onValueChange = { viewModel.updateSocialMediaLink(sm.platform, it) },
                label = { Text("Link") },
                enabled = sm.enabled,
                modifier = Modifier.weight(1f))
          }
        }

        // --- Upload Images (URLs) ---
        Text("Upload Images", color = Color.Gray, style = MaterialTheme.typography.titleSmall)
        HorizontalDivider(color = Color.Gray)

        OutlinedTextField(
            value = formState.logoUrl,
            onValueChange = { viewModel.updateLogoUrl(it) },
            label = { Text("Logo URL") },
            modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = formState.bannerUrl,
            onValueChange = { viewModel.updateBannerUrl(it) },
            label = { Text("Banner URL") },
            modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))

        // --- Submit Button ---
        Button(
            onClick = { viewModel.submit(onSubmitSuccess) },
            enabled = viewModel.isFormValid(),
            colors =
                ButtonDefaults.buttonColors(containerColor = LifeRed, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(50.dp)) {
              Text("Submit", style = MaterialTheme.typography.bodyLarge)
            }

        Spacer(Modifier.height(24.dp))
      }
}
