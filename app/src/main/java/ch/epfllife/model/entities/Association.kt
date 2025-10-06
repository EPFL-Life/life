package ch.epfllife.model.entities

import ch.epfllife.model.enums.Category

data class Association(
    val id: String,
    val name: String,
    val description: String,
    val pictureUrl: String? = null,
    val category: Category
)
