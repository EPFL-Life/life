package ch.epfllife.ui.composables

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.epfllife.model.event.Event

object EventCardTestTags {
  const val EVENT_CARD = "eventCard"
}

@Composable
fun EventCard(event: Event, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {

  Card(
      onClick = onClick,
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.elevatedCardElevation(5.dp),
      modifier = modifier.fillMaxWidth().testTag(EventCardTestTags.EVENT_CARD)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f))
            Text(
                text =
                    if (event.price == 0u) {
                      "Free"
                    } else {
                      "CHF ${event.price}"
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          Spacer(Modifier.height(2.dp))

          Text(
              text = event.associationId,
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
