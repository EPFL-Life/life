package ch.epfllife.ui.association

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.Event
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.EventCard
import ch.epfllife.ui.theme.LifeRed
import ch.epfllife.ui.theme.Theme
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Association details screen.
 *
 * @param associationId The ID of the association to display.
 * @param onGoBack Callback when the user presses the back button.
 */
object AssociationDetailsTestTags {
  const val ASSOCIATION_IMAGE = "association_image"
  const val BACK_BUTTON = "back_button"
  const val NAME_TEXT = "name_text"
  const val DESCRIPTION_TEXT = "description_text"
  const val ABOUT_SECTION = "about_section"
  const val SOCIAL_LINKS_ROW = "social_links_row"
  const val UPCOMING_EVENTS_COLUMN = "upcoming_events_column"
  const val SUBSCRIBE_BUTTON = "subscribe_button"
  const val UNSUBSCRIBE_BUTTON = "unsubscribe_button"
  const val LOADING_INDICATOR = "loading_indicator"
  const val ERROR_MESSAGE = "error_message"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssociationDetailsScreen(
    associationId: String,
    viewModel: AssociationDetailsViewModel = viewModel(),
    onGoBack: () -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsState()
  LaunchedEffect(associationId) {
    viewModel.loadAssociation(associationId)
  } // this is triggered once the screen opens

  when (val state = uiState) {
    is AssociationDetailsUIState.Loading -> {
      // Show loading spinner
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.testTag(AssociationDetailsTestTags.LOADING_INDICATOR))
      }
    }

    is AssociationDetailsUIState.Error -> {
      // Show error message
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = state.message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag(AssociationDetailsTestTags.ERROR_MESSAGE),
        )
      }
    }

    is AssociationDetailsUIState.Success -> {
      // Show event content
      AssociationDetailsContent(
          association = state.association,
          events = state.events ?: emptyList(),
          onGoBack = onGoBack,
      )
    }
  }
}

@Composable
fun AssociationDetailsContent(
    association: Association,
    modifier: Modifier = Modifier,
    events: List<Event> = emptyList(),
    onGoBack: () -> Unit,
) {
  var isSubscribed by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()
  val context = LocalContext.current

  Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
    // Header Image
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
      Box(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model =
                ImageRequest.Builder(LocalContext.current)
                    .data(association.pictureUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = stringResource(R.string.association_image_description),
            contentScale = ContentScale.Crop,
            modifier =
                Modifier.fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                    .testTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE),
        )
      }

      // Content Below Header
      Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
            text = association.name,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.testTag(AssociationDetailsTestTags.NAME_TEXT),
        )
        Text(
            text = association.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(AssociationDetailsTestTags.DESCRIPTION_TEXT),
        )

        // Subscribe Button
        val subscribeButtonTag =
            if (isSubscribed) {
              AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON
            } else {
              AssociationDetailsTestTags.SUBSCRIBE_BUTTON
            }

        Button(
            onClick = { isSubscribed = !isSubscribed },
            modifier = Modifier.fillMaxWidth().testTag(subscribeButtonTag),
            shape = RoundedCornerShape(6.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (isSubscribed) Color.Gray else LifeRed,
                    contentColor = Color.White,
                ),
        ) {
          Text(
              text =
                  if (isSubscribed) stringResource(R.string.unsubscribe_from, association.name)
                  else stringResource(R.string.subscribe_to, association.name))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // About Section
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.testTag(AssociationDetailsTestTags.ABOUT_SECTION),
        ) {
          Text(
              stringResource(R.string.about_section_title),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          )
          Text(
              association.about ?: stringResource(R.string.about_placeholder),
              style = MaterialTheme.typography.bodyMedium,
          )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Social Pages
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
              stringResource(R.string.social_pages_title),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          )

          Row(
              horizontalArrangement = Arrangement.spacedBy(24.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.testTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW),
          ) {
            association.socialLinks
                ?.toList()
                ?.sortedBy { (platform, _) ->
                  SocialIcons.platformOrder.indexOf(platform.lowercase()).takeIf { it >= 0 }
                      ?: Int.MAX_VALUE
                }
                ?.forEach { (platform, url) ->
                  val iconRes = SocialIcons.getIcon(platform) ?: R.drawable.ic_default
                  IconButton(
                      onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                        context.startActivity(intent)
                      }) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = platform,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp),
                        )
                      }
                }
          }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Upcoming Events (dummy data)
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN),
        ) {
          Text(
              stringResource(R.string.upcoming_events_title),
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          )

          events.forEach { event -> EventCard(event = event, onClick = {}) }
        }
      }
    }
    BackButton(
        modifier =
            Modifier.align(Alignment.TopStart).testTag(AssociationDetailsTestTags.BACK_BUTTON),
        onGoBack = onGoBack,
    )
  }
}

@Preview(showBackground = true)
@Composable
fun AssociationDetailsPreview() {
  Theme { AssociationDetailsScreen(associationId = "1", onGoBack = {}) }
}
