package ch.epfllife.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.epfllife.model.enums.SubscriptionFilter

@Composable
fun DisplayedSubscriptionFilter(
    selected: SubscriptionFilter,
    onSelected: (SubscriptionFilter) -> Unit,
    subscribedLabel: String,
    allLabel: String,
    modifier: Modifier = Modifier,
    underlineWidth: Int = 80
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically) {
        FilterText(
            text = subscribedLabel,
            selected = selected == SubscriptionFilter.Subscribed,
            onClick = { onSelected(SubscriptionFilter.Subscribed) },
            underlineWidth = underlineWidth,
            modifier = Modifier.testTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED))

        FilterText(
            text = allLabel,
            selected = selected == SubscriptionFilter.All,
            onClick = { onSelected(SubscriptionFilter.All) },
            underlineWidth = underlineWidth,
            modifier = Modifier.testTag(DisplayedEventsTestTags.BUTTON_ALL))
      }
}

object DisplayedEventsTestTags {

  const val BUTTON_SUBSCRIBED = "BUTTON_SUBSCRIBED"
  const val BUTTON_ALL = "BUTTON_ALL"
}

@Composable
private fun FilterText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    underlineWidth: Int,
    modifier: Modifier
) {
  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = modifier.clickable(onClick = onClick)) {
        Text(
            text = text,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
            color =
                if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant)
        if (selected) {
          Spacer(Modifier.height(2.dp))
          HorizontalDivider(
              modifier = Modifier.width(underlineWidth.dp),
              thickness = 2.dp,
              color = MaterialTheme.colorScheme.onSurface)
        }
      }
}
