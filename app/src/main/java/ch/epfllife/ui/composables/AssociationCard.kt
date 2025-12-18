package ch.epfllife.ui.composables

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
import androidx.compose.ui.unit.dp
import ch.epfllife.model.association.Association
import coil.compose.AsyncImage

private const val DEFAULT_ASSOCIATION_LOGO_URL = "https://i.ibb.co/p7wH3hJ/assoc-default.png"

@Composable
fun AssociationCard(association: Association, modifier: Modifier = Modifier, onClick: () -> Unit) {
  val baseModifier = modifier.fillMaxWidth().padding(5.dp)
  val taggedModifier =
      baseModifier.testTag(AssociationCardTestTags.getAssociationCardTestTag(association.id))
  Card(
      onClick = onClick,
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.elevatedCardElevation(5.dp),
      modifier = taggedModifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              // Club/association icon
              AsyncImage(
                  model =
                      association.logoUrl ?: association.pictureUrl ?: DEFAULT_ASSOCIATION_LOGO_URL,
                  contentDescription = "${association.name} logo",
                  modifier =
                      Modifier.size(56.dp)
                          .align(Alignment.CenterVertically)
                          .testTag(AssociationCardTestTags.ASSOCIATION_LOGO))

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
  const val ASSOCIATION_LOGO = "association_logo"
  const val ASSOCIATION_NAME = "association_name"
  const val ASSOCIATION_DESCRIPTION = "association_description"

  fun getAssociationCardTestTag(assocId: String) = "associationCard_$assocId"
}
