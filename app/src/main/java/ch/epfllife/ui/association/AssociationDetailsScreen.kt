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
import ch.epfllife.R
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.map.Location
import ch.epfllife.model.user.Price
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
}

@Composable
fun AssociationDetailsScreen(associationId: String, onGoBack: () -> Unit) {
  // For now, we still use a sample association.
  // In the future, you can replace this with a ViewModel fetching the association by ID.
  val sampleAssociation =
      Association(
          id = associationId, // Now we actually use the passed associationId
          name = "ESN Lausanne",
          description = "Erasmus Student Network at EPFL.",
          eventCategory = EventCategory.CULTURE,
          pictureUrl =
              "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png",
          about =
              "The Erasmus Student Network (ESN) Lausanne is a student association that helps exchange students integrate into life at EPFL and Lausanne through social and cultural activities.",
          socialLinks =
              mapOf(
                  "instagram" to "https://www.instagram.com/esnlausanne",
                  "telegram" to "https://t.me/esnlausanne",
                  "whatsapp" to "https://wa.me/41791234567",
                  "linkedin" to "https://www.linkedin.com/company/esnlausanne",
                  "website" to "https://esnlausanne.ch",
              ),
      )

  AssociationDetailsContent(association = sampleAssociation, onGoBack = onGoBack)
}

@Composable
fun AssociationDetailsContent(
    association: Association,
    modifier: Modifier = Modifier,
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

          val dummyEvents =
              listOf(
                  Event(
                      id = "1",
                      title = "Welcome Party",
                      description = "Kick off the semester with music and fun.",
                      location = Location(46.5191, 6.5668, "EPFL Esplanade"),
                      time = "2025-10-20 18:00",
                      association = association,
                      tags = listOf("party"),
                      price = Price(0u),
                      pictureUrl = null),
                  Event(
                      id = "2",
                      title = "Hiking Trip",
                      description = "Join us for a scenic hike in the mountains.",
                      location = Location(46.2, 7.0, "Les Pleiades"),
                      time = "2025-11-02 09:00",
                      association = association,
                      tags = listOf("outdoors"),
                      price = Price(15u),
                      pictureUrl = null))

          dummyEvents.forEach { event -> EventCard(event = event, onClick = {}) }
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
