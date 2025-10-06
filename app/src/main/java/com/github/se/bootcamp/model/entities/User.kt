package com.github.se.bootcamp.model.entities

data class User(
    val id: String,
    val name: String,
    val subscriptions: Set<String> = emptySet(),
    val userSettings: UserSettings = UserSettings()
)
