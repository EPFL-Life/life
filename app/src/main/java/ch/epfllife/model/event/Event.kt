package ch.epfllife.model.event

import ch.epfllife.model.association.Association
import ch.epfllife.model.map.Location

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: Location,
    val time: String, // for the moment it will be a string
    val association: Association,
    val tags: List<String>,
    val price: UInt = 0u,
    val pictureUrl: String? = null
)
