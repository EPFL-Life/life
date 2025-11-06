package ch.epfllife.model.user

class UserRepositoryLocal : UserRepository {

  // In-memory storage for users (use this only for testing)
  private val users = mutableMapOf<String, User>()

  // Currently logged in user (simulated)
  private var currentUserId: String? = null

  /**
   * Helper method (not in interface) to simulate user login
   *
   * @param userId The Id of the user to login (retrived from Auth firebase) We allow for null as
   *   userId for an guest account (no login)
   */
  fun simulateLogin(userId: String?) {
    currentUserId = userId
  }

  override suspend fun getCurrentUser(): User? {
    // if currentUserId != null return user with currentUserId else return null
    return users[currentUserId]
  }

  override suspend fun getAllUsers(): List<User> {
    // return list of values(users)
    return users.values.toList()
  }

  override suspend fun getUser(userId: String): User? {
    return users[userId]
  }

  override suspend fun createUser(newUser: User): Result<Unit> {
    // check if user with userId already exists
    if (users.containsKey(newUser.id)) {
      return Result.failure(IllegalArgumentException("User with ID ${newUser.id} already exists!"))
    }

    // add the new User
    users[newUser.id] = newUser
    return Result.success(Unit)
  }

  override suspend fun updateUser(userId: String, newUser: User): Result<Unit> {
    // First, check if the user to update even exists
    if (!users.containsKey(userId)) {
      return Result.failure(
          NoSuchElementException("Cannot update. User not found with ID: $userId"))
    }

    // Check if the object's ID matches the parameter ID
    if (userId != newUser.id) {
      return Result.failure(
          IllegalArgumentException(
              "User ID mismatch. Parameter was $userId but object ID was ${newUser.id}"))
    }

    // Perform the update
    users[userId] = newUser
    return Result.success(Unit)
  }

  override suspend fun deleteUser(userId: String): Result<Unit> {

    // remove user
    val removedUser = users.remove(userId)

    // check if user exists and return failure/success
    return if (removedUser == null) {
      Result.failure(NoSuchElementException("User not found with ID: $userId"))
    } else {
      Result.success(Unit)
    }
  }
}
