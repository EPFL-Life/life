package ch.epfllife.model.user

/** Represents a repository that manages User data. */
interface UserRepository {

  /** Generates and returns a new unique identifier for a User. */
  fun getNewUid(): String

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
  suspend fun getUser(userId: String): User

  /**
   * Adds a new User to the repository.
   *
   * @param user The User to add.
   */
  suspend fun createUser(user: User): Result<Unit>

  /**
   * Edits an existing User in the repository.
   *
   * @param userId The unique identifier of the User to edit.
   * @param newUser The new value for the User.
   */
  suspend fun updateUser(userId: String, newUser: User): Result<Unit>

  /**
   * Deletes a User from the repository.
   *
   * @param userId The unique identifier of the User to delete.
   */
  suspend fun deleteUser(userId: String): Result<Unit>

  /**
   * This is an additional function because this class also relates to authentication (here we use a
   * pragmatic approach to prevent creating an extra class) Retrieves the profile of the currently
   * authenticated user. This is a common convenience method.
   *
   * @return The [User] object for the current user, or null if not logged in or profile doesn't
   *   exist.
   */
  suspend fun getCurrentUser(): User?
}
