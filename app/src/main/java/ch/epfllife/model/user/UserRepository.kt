package ch.epfllife.model.user

/**
 * Represents a repository that manages User data, acting as the bridge between authentication
 * (Firebase Auth) and the user database (Firestore).
 */
interface UserRepository {

  /**
   * Retrieves the profile of the *currently authenticated* user.
   *
   * This method connects the authentication service (Firebase Auth) to the user database. It first
   * finds *who* is logged in, then fetches their corresponding User profile.
   *
   * @return The [User] object for the current user, or null if not logged in or if their profile
   *   doesn't exist in the database.
   */
  suspend fun getCurrentUser(): User?

  /**
   * Retrieves all User profiles from the repository.
   *
   * @return A list of all Users.
   */
  suspend fun getAllUsers(): List<User>

  /**
   * Retrieves a specific User by their unique identifier.
   *
   * @param userId The unique identifier of the User to retrieve.
   * @return The User with the specified identifier.
   */
  suspend fun getUser(userId: String): User?

  /**
   * Adds a new User's profile to the database (e.g., Firestore). This is typically called once
   * right after sign-up.
   *
   * @param newUser The User to add. The **user.id must be the Firebase Auth UID**.
   * @return A [Result] indicating success or failure.
   */
  suspend fun createUser(newUser: User): Result<Unit>

  /**
   * Edits an existing User in the repository.
   *
   * After careful consideration we pass the userID and User *separately* so the function call is
   * more clear and understandable. This is especially because the ID here is bound to an auth
   * account. (discuss with @Daniel if you want to change this)
   *
   * @param userId The unique identifier of the User to edit.
   * @param newUser The new value for the User.
   * @return A [Result] indicating success or failure.
   */
  suspend fun updateUser(userId: String, newUser: User): Result<Unit>

  /**
   * Deletes a User's profile from the database.
   *
   * @param userId The unique identifier of the User to delete.
   * @return A [Result] indicating success or failure.
   */
  suspend fun deleteUser(userId: String): Result<Unit>

  /**
   * Adds a new event to the currently authenticated user's subscriptions.
   *
   * @param eventId The unique identifier of the Event to subscribe to.
   * @return A [Result] indicating success or failure.
   */
  suspend fun subscribeToEvent(eventId: String): Result<Unit>

  /**
   * Removes an event ID from the currently authenticated user's subscriptions.
   *
   * @param eventId The unique identifier of the Event to unsubscribe from.
   * @return A [Result] indicating success or failure.
   */
  suspend fun unsubscribeFromEvent(eventId: String): Result<Unit>
}
