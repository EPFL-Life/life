package com.android.sample.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.sample.model.entities.Event
import com.android.sample.model.map.Location
import com.android.sample.ui.composables.EventCard
import com.android.sample.ui.composables.SearchBar


@Composable
fun HomeScreen(
    events: List<Event>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("EPFL Life", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(Modifier.height(12.dp))
        SearchBar()

        Spacer(Modifier.height(16.dp))
        Text("Your Subscriptions", color = Color.Gray)
        HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(events, key = { it.id }) { ev ->
                EventCard(event = ev)
            }
        }
    }
}


@Preview
@Composable
private fun HomeScreenPreview() {
    val sampleEvents = listOf(
        Event(
            id = "1",
            title = "Event Title 1",
            description = "Live concert by local bands",
            location = Location(0.0, 0.0, "MED 0 1418"),
            time = "18:30",
            associationId = "AgePoly",
            tags = setOf("Music", "Live")
        ),
        Event(
            id = "2",
            title = "Event Title 2",
            description = "Blockchain 101 workshop",
            location = Location(0.0, 0.0, "CE 1 1212"),
            time = "19:00",
            associationId = "Blockchain",
            tags = setOf("Tech", "Workshop")
        ),
        Event(
            id = "3",
            title = "Event Title 3",
            description = "Trivia night for students",
            location = Location(0.0, 0.0, "BC 3 221"),
            time = "20:00",
            associationId = "TriviaPoly",
            tags = setOf("Fun", "Quiz")
        )
    )

    MaterialTheme {
        HomeScreen(events = sampleEvents)
    }
}