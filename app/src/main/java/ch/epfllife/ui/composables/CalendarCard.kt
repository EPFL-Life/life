package ch.epfllife.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.epfllife.model.event.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import ch.epfllife.ui.calendar.CalendarTestTags


@Composable
fun CalendarCard(event: Event, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(5.dp),
        modifier =
            modifier
                .fillMaxWidth()
                .padding(5.dp)
                .testTag(CalendarTestTags.EVENT_CARD)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Date box on the left
            Box(
                modifier = Modifier
                    .size(64.dp) // square box: width = height
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .testTag(CalendarTestTags.EVENT_DATE_BOX),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.formattedDate(), // already handles multi-line formatting
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Text section (name + association)
            Column(modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(CalendarTestTags.EVENT_TITLE))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = event.association.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(CalendarTestTags.EVENT_ASSOCIATION))
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go to ${event.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(CalendarTestTags.EVENT_ARROW))
        }
    }
}

private fun Event.formattedDate(): String {
    val locale = Locale.getDefault()
    val parts = this.time.split("/")
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val startDate = try {
        LocalDate.parse(parts.first().substring(0, 10), formatter)
    } catch (_: Exception) {
        return this.time
    }

    val endDate = parts.getOrNull(1)?.let {
        try {
            LocalDate.parse(it.substring(0, 10), formatter)
        } catch (_: Exception) {
            null
        }
    }

    val monthShortStart = startDate.month.getDisplayName(TextStyle.SHORT, locale)
    val monthShortEnd = endDate?.month?.getDisplayName(TextStyle.SHORT, locale)

    return when {
        endDate == null || endDate == startDate -> {
            // Single day → "Nov\n15"
            "$monthShortStart\n${startDate.dayOfMonth}"
        }
        startDate.month == endDate.month -> {
            // Same month → "Nov\n15 - 20"
            "$monthShortStart\n${startDate.dayOfMonth} - ${endDate.dayOfMonth}"
        }
        else -> {
            // Different months → "Nov 15 -\nDec 20"
            "$monthShortStart ${startDate.dayOfMonth} -\n$monthShortEnd ${endDate.dayOfMonth}"
        }
    }
}


