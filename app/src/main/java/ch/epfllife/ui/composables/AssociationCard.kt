package ch.epfllife.ui.composables

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.model.association.Association

@Composable
fun AssociationCard(
    association: Association,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
  Card(
      onClick = onClick,
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.elevatedCardElevation(5.dp),
      modifier = modifier.fillMaxWidth().padding(5.dp).testTag(AssociationCardTestTags.ASSOCIATION_CARD)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              // Club/association icon
              Image(
                  painter = painterResource(id = R.drawable.placeholder),
                  contentDescription = "${association.name} logo",
                  modifier = Modifier.size(56.dp).align(Alignment.CenterVertically))

              // Text section (name + description)
              Column(modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
                Text(
                    text = association.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(AssociationCardTestTags.ASSOCIATION_NAME))
                Spacer(Modifier.height(4.dp))
                Text(
                    text = association.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(AssociationCardTestTags.ASSOCIATION_DESCRIPTION))
              }

              // Right arrow icon
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = "Go to ${association.name}",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
      }
}

object AssociationCardTestTags {
  const val ASSOCIATION_CARD = "association_card"
  const val ASSOCIATION_NAME = "association_name"
  const val ASSOCIATION_DESCRIPTION = "association_description"
}
