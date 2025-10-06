package com.github.se.bootcamp.model.entities

import com.github.se.bootcamp.model.map.Location

// import com.google.firebase.Timestamp

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: Location,
    val time: String, // for the moment it will be a string
    val associationId: String,
    val tags: Set<String>
)
