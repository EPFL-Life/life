package ch.epfllife.model.association

import ch.epfllife.model.event.EventCategory

data class Association(
    val id: String,
    val name: String,
    val description: String,
    val pictureUrl: String? = null,
    val eventCategory: EventCategory
)
