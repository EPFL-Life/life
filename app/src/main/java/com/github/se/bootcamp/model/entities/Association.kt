package com.github.se.bootcamp.model.entities

import com.github.se.bootcamp.model.enums.Category

data class Association(
    val id: String,
    val name: String,
    val description: String,
    val pictureUrl: String? = null,
    val category: Category
)
