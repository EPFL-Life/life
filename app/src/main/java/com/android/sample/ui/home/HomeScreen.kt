package com.android.sample.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.sample.model.entities.Event
import com.android.sample.model.map.Location
import com.android.sample.ui.composables.EventCard
import com.android.sample.ui.composables.SearchBar
import com.android.sample.R
import androidx.compose.foundation.Image
import com.android.sample.ui.composables.EventsFilterButtons
import com.android.sample.ui.enums.EventsFilter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf(EventsFilter.Subscribed) }

    val myEvents = remember {
        listOf(
            Event(
                id = "1",
                title = "Via Ferrata",
                description = "Excursion to the Alps",
                location = Location(0.0, 0.0, "Lausanne Train Station"),
                time = "Oct 4th, 6:50am",
                associationId = "ESN Lausanne",
                tags = setOf("Sport", "Outdoor"),
                price = 30
            )

        )
    }

    val allEvents = remember {
        listOf(
            Event(
                id = "1",
                title = "Via Ferrata",
                description = "Excursion to the Alps",
                location = Location(0.0, 0.0, "Lausanne Train Station"),
                time = "Oct 4th, 6:50am",
                associationId = "ESN Lausanne",
                tags = setOf("Sport", "Outdoor"),
                price = 30
            ),
            Event(
                id = "2",
                title = "Music Festival",
                description = "Outdoor concert organized by the Cultural Club",
                location = Location(0.0, 0.0, "Esplanade"),
                time = "Nov 3rd, 5:00PM",
                associationId = "Cultural Club",
                tags = setOf("Music", "Festival"),
                price = 10
            )
        )
    }

    val shownEvents = if (selected == EventsFilter.Subscribed) myEvents else allEvents

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.epfl_life_logo),
                contentDescription = "EPFL Life Logo",
                modifier = Modifier.height(40.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(Modifier.height(12.dp))
        SearchBar()

        Spacer(Modifier.height(12.dp))
        EventsFilterButtons(
            selected = selected,
            onSelected = { selected = it }
        )

        Spacer(Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(shownEvents, key = { it.id }) { ev ->
                EventCard(event = ev)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}