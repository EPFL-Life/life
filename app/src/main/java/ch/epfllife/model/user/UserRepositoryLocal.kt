package ch.epfllife.model.user

import ch.epfllife.model.event.EventRepositoryLocal

class UserRepositoryLocal(private var eventRepositoryLocal: EventRepositoryLocal? = null) : UserRepository {

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

  override suspend fun subscribeToEvent(eventId: String): Result<Unit> {
    val currentUser = getCurrentUser()

    // case 1: getCurrentUser() returns a null object
    if (currentUser == null) {
      return Result.failure(
        NoSuchElementException("No user is currently logged in"))
    }

    // case 2: check that the event repository is initialized
    val eventRepo = eventRepositoryLocal
      ?: return Result.failure(
        IllegalStateException("EventRepository not initialized in UserRepositoryLocal."))

    // case 3: when user tries to subscribe to an invalid event
    if(eventRepo.getEvent(eventId) == null){
      return Result.failure(
        NoSuchElementException("Event with ID $eventId does not exist in the repository.")
      )
    }

    // case 4: the user is already enrolled to event
    if(currentUser.subscriptions.contains(eventId)){
      return Result.failure(
        IllegalArgumentException("User is already subscribed to event with ID: $eventId")
      )
    }

    // case 5: user can enroll to event
    val updatedUser = currentUser.copy(subscriptions = currentUser.subscriptions + eventId)
    // reused updateUser() method
    return updateUser(currentUser.id, updatedUser)
  }


  override suspend fun unsubscribeFromEvent(eventId: String): Result<Unit> {
    val currentUser = getCurrentUser()

    // case 1: getCurrentUser() returns a null object
    if (currentUser == null) {
      return Result.failure(
        NoSuchElementException("No user is currently logged in"))
    }

    // case 2: check that the event repository is initialized
    val eventRepo = eventRepositoryLocal
      ?: return Result.failure(
        IllegalStateException("EventRepository not initialized in UserRepositoryLocal."))

    // case 3: when user tries to unsubscribe to an invalid event
    if(eventRepo.getEvent(eventId) == null){
      return Result.failure(
        NoSuchElementException("Event with ID $eventId does not exist in the repository.")
      )
    }

    // case 4: the user is trying to unsubscribe from an event they are not subscribed to
    if(!currentUser.subscriptions.contains(eventId)){
      return Result.failure(
        IllegalArgumentException("User is not subscribed to event with ID: $eventId")
      )
    }

    // case 5: user can unsubscribe to event
    val updatedUser = currentUser.copy(subscriptions = currentUser.subscriptions - eventId)
    // reused updateUser() method
    return updateUser(currentUser.id, updatedUser)



  }
}
