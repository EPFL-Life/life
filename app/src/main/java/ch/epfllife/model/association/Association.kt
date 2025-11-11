package ch.epfllife.model.association

import ch.epfllife.model.event.EventCategory

/** If this is modified please also fix documentToAssociation() parser accordingly! */
data class Association(
    val id: String,
    val name: String,
    val description: String,
    val pictureUrl: String? = null,
    val eventCategory: EventCategory,
    val about: String? = null,
    val socialLinks: Map<String, String>? = null
)
// add about, social links
