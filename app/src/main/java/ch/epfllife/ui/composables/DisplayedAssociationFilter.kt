package ch.epfllife.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.epfllife.model.enums.AssociationsFilter
import androidx.compose.ui.res.stringResource
import ch.epfllife.R

@Composable
fun DisplayedAssociationFilter(
    selected: AssociationsFilter,
    onSelected: (AssociationsFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterText(
            text = stringResource(id = R.string.subscribed_filter),
            selected = selected == AssociationsFilter.Subscribed,
            onClick = { onSelected(AssociationsFilter.Subscribed) }
        )
        FilterText(
            text = stringResource(id = R.string.all_associations_filter),
            selected = selected == AssociationsFilter.All,
            onClick = { onSelected(AssociationsFilter.All) }
        )
    }
}

@Composable
private fun FilterText(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (selected) {
            Spacer(Modifier.height(2.dp))
            HorizontalDivider(
                modifier = Modifier.width(100.dp),
                thickness = 2.dp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}