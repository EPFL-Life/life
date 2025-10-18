package ch.epfllife.model.user

/** Represents a repository that manages User data. */
interface UserRepository {

  /**
   * Retrieves a specific user's profile by their unique identifier.
   *
   * @param userId The unique ID of the user to fetch.
   * @return The [User] object, or null if not found.
   */
  suspend fun getUser(userId: String): User?

  /**
   * Creates a new user profile in the data source. Typically used during the sign-up process.
   *
   * @param user The [User] object to create.
   */
  suspend fun createUser(user: User)

  /**
   * Updates an existing user's profile.
   *
   * @param userId The ID of the user to update.
   * @param newUser The [User] object containing the new data.
   */
  suspend fun updateUser(userId: String, newUser: User)

  /**
   * Retrieves the profile of the currently authenticated user. This is a common convenience method.
   *
   * @return The [User] object for the current user, or null if not logged in or profile doesn't
   *   exist.
   */
  suspend fun getCurrentUser(): User?
}
