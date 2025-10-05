package com.android.sample.model.entities

import com.android.sample.model.enums.Category

data class Association(
    val id: String,
    val name: String,
    val description: String,
    val pictureUrl: String? = null,
    val category: Category
)
