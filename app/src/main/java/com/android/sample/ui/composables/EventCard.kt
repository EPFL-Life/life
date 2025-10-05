package com.android.sample.ui.composables

import androidx.compose.foundation.layout.Arrangement
import com.android.sample.model.entities.Event
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

@Composable
fun EventCard(event: Event) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(event.title, fontWeight = FontWeight.Bold)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(Color.Red, shape = MaterialTheme.shapes.small)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = event.associationId,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(10.dp))

            // three gray “chips” – simple Boxes with rounded background
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EventTag("Time")
                EventTag("Location")
                EventTag("Enroll")
            }
        }
    }
}