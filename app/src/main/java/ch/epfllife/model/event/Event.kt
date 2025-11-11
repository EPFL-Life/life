package ch.epfllife.model.event

import ch.epfllife.model.association.Association
import ch.epfllife.model.map.Location

/** If this is modified please also fix documentToEvent() parser accordingly! */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: Location,
    val time: String, // for the moment it will be a string
    val association: Association,
    val tags: Set<String>,
    val price: UInt = 0u,
    val pictureUrl: String? = null
)
