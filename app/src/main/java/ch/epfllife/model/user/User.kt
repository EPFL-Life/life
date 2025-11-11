package ch.epfllife.model.user

data class User(
    val id: String,
    val name: String,
    val subscriptions: Set<String> = emptySet(),
    val enrolledEvents: List<String> = emptyList(),
    val userSettings: UserSettings = UserSettings()
)
