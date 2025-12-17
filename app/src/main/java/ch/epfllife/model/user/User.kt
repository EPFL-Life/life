package ch.epfllife.model.user

/** If this is modified please also fix documentToUser() parser accordingly! */
data class User(
    val id: String,
    val name: String,
    val subscriptions: List<String> = emptyList(),
    val enrolledEvents: List<String> = emptyList(),
    val userSettings: UserSettings = UserSettings(),
    val role: UserRole = UserRole.USER,
    val managedAssociationIds: List<String> =
        emptyList(), // List of Assoc that this user can manage
    val photoUrl: String? = null,
    val following: List<String> = emptyList(), // User IDs of followed users
)

enum class UserRole {
  USER, // Default user -> read-only access
  ASSOCIATION_ADMIN, // Can manage specific associations
  ADMIN, // Global admin, can manage everything (for dev purposes)
}

fun sortUsers(user: User, others: List<User>): List<User> {
  val following = user.following.toSet()
  return others
      .partition { it.id in following }
      .toList()
      .flatMap { it.sortedBy { user -> user.name } }
}
