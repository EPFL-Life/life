package ch.epfllife.model.event

import ch.epfllife.model.association.Association
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.Price

/** If this is modified please also fix documentToEvent() parser accordingly! */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: Location,
    val time: String, // for the moment it will be a string
    val association: Association,
    val tags: List<String>,
    val price: Price,
    val pictureUrl: String? = null
)
