package ch.epfllife.model.event

import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.Price

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: Location,
    val time: String, // for the moment it will be a string
    val associationId: String,
    val tags: Set<String>,
    val price: Price,
    val imageUrl: String? = null // referenced Image //TODO implement this in firebase
)
