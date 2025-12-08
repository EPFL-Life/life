package ch.epfllife.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.epfllife.model.event.Event
import ch.epfllife.ui.theme.Enrolled
import ch.epfllife.ui.theme.LifeRed
import coil.compose.AsyncImage

object EventCardTestTags {
  fun getEventCardTestTag(eventId: String) = "eventCard_$eventId"
}

@Composable
fun EventCard(
    event: Event,
    isEnrolled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {

  Card(
      onClick = onClick,
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.elevatedCardElevation(5.dp),
      modifier = modifier.fillMaxWidth().testTag(EventCardTestTags.getEventCardTestTag(event.id))) {
        Column {
          // Banner Image
          AsyncImage(
              model =
                  event.pictureUrl
                      ?: "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png",
              contentDescription = "Event Image",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.fillMaxWidth()
                      .height(200.dp)
                      .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)))

          Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      text = event.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.SemiBold,
                      modifier = Modifier.weight(1f))
                  if (isEnrolled) {
                    Box(
                        modifier =
                            Modifier.background(color = Enrolled, shape = RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                          Text(
                              text = "Enrolled",
                              style = MaterialTheme.typography.labelSmall,
                              color = Color.White)
                        }
                  } else {
                    Box(
                        modifier =
                            Modifier.background(color = LifeRed, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)) {
                          Text(
                              text = "Enroll",
                              style = MaterialTheme.typography.labelSmall,
                              color = Color.White)
                        }
                  }
                  Spacer(Modifier.width(6.dp))

                  Icon(
                      imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

            Spacer(Modifier.height(2.dp))

            Text(
                text = event.association.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                  InfoItem(
                      icon = Icons.Outlined.CalendarMonth,
                      text = event.location.name,
                      modifier = Modifier.weight(1f, fill = false))
                  Spacer(Modifier.width(16.dp))
                  InfoItem(icon = Icons.Outlined.AccessTime, text = event.time)
                }
          }
        }
      }
}

@Composable
private fun InfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    modifier: Modifier = Modifier
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.width(8.dp))
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
