package ch.epfllife.model.entities

import ch.epfllife.model.map.Location

// import com.google.firebase.Timestamp

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: Location,
    val time: String, // for the moment it will be a string
    val associationId: String,
    val tags: Set<String>,
    val price: Int? = null,
    val imageUrl: String? = null // referenced Image //TODO implement this in firebase
)
