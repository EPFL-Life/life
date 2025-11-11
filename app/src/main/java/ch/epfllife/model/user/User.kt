package ch.epfllife.model.user

/** If this is modified please also fix documentToUser() parser accordingly! */
data class User(
    val id: String,
    val name: String,
    val subscriptions: Set<String> = emptySet(),
    val userSettings: UserSettings = UserSettings()
)
