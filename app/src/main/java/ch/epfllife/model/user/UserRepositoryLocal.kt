package ch.epfllife.model.user

import ch.epfllife.model.association.AssociationRepositoryLocal
import ch.epfllife.model.event.EventRepositoryLocal

class UserRepositoryLocal(private var eventRepositoryLocal: EventRepositoryLocal? = null, private var associationRepository: AssociationRepositoryLocal?=null) :
    UserRepository {

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
      return Result.failure(NoSuchElementException(ERROR_USER_NOT_LOGGED_IN))
    }

    // case 2: check that the event repository is initialized
    val eventRepo =
        eventRepositoryLocal
            ?: return Result.failure(
                IllegalStateException("EventRepository not initialized in UserRepositoryLocal."))

    // case 3: when user tries to subscribe to an invalid event
    if (eventRepo.getEvent(eventId) == null) {
      return Result.failure(
          NoSuchElementException("Event with ID $eventId does not exist in the repository."))
    }

    // case 4: the user is already enrolled to event
    if (currentUser.enrolledEvents.contains(eventId)) {
      return Result.failure(
          IllegalArgumentException("User is already subscribed to event with ID: $eventId"))
    }

    // case 5: user can enroll to event
    val updatedUser = currentUser.copy(enrolledEvents = currentUser.enrolledEvents + eventId)
    // reused updateUser() method
    return updateUser(currentUser.id, updatedUser)
  }

  override suspend fun unsubscribeFromEvent(eventId: String): Result<Unit> {
    val currentUser = getCurrentUser()

    // case 1: getCurrentUser() returns a null object
    if (currentUser == null) {
      return Result.failure(NoSuchElementException(ERROR_USER_NOT_LOGGED_IN))
    }

    // case 2: check that the event repository is initialized
    val eventRepo =
        eventRepositoryLocal
            ?: return Result.failure(
                IllegalStateException("EventRepository not initialized in UserRepositoryLocal."))

    // case 3: when user tries to unsubscribe to an invalid event
    if (eventRepo.getEvent(eventId) == null) {
      return Result.failure(
          NoSuchElementException("Event with ID $eventId does not exist in the repository."))
    }

    // case 4: the user is trying to unsubscribe from an event they are not subscribed to
    if (!currentUser.enrolledEvents.contains(eventId)) {
      return Result.failure(
          IllegalArgumentException("User is not subscribed to event with ID: $eventId"))
    }

    // case 5: user can unsubscribe to event
    val updatedUser = currentUser.copy(enrolledEvents = currentUser.enrolledEvents - eventId)
    // reused updateUser() method
    return updateUser(currentUser.id, updatedUser)
  }

    override suspend fun subscribeToAssociation(associationId: String): Result<Unit> {
        val currentUser = getCurrentUser()

        // case 1: getCurrentUser() returns a null object
        if (currentUser == null) {
            return Result.failure(NoSuchElementException(ERROR_USER_NOT_LOGGED_IN))
        }

        // case 2: check that the association repository is initialized
        val associationRepository =
            associationRepository
                ?: return Result.failure(
                    IllegalStateException(
                        "AssociationRepository not initialized in UserRepositoryLocal."))

        // case 3: when user tries to subscribe to an invalid association
        if (associationRepository.getAssociation(associationId) == null) {
            return Result.failure(
                NoSuchElementException(
                    "Association with ID $associationId does not exist in the repository."))
        }

        // case 4: the user is already subscribed to association
        if (currentUser.subscriptions.contains(associationId)) {
            return Result.failure(
                IllegalArgumentException(
                    "User is already subscribed to association with ID: $associationId"))
        }

        // case 5: user can subscribe to association
        val updatedUser = currentUser.copy(subscriptions = currentUser.subscriptions + associationId)
        // reused updateUser() method
        return updateUser(currentUser.id, updatedUser)
    }

    override suspend fun unsubscribeFromAssociation(associationId: String): Result<Unit> {
        val currentUser = getCurrentUser()

        // case 1: getCurrentUser() returns a null object
        if (currentUser == null) {
            return Result.failure(NoSuchElementException(ERROR_USER_NOT_LOGGED_IN))
        }

        // case 2: check that the association repository is initialized
        val associationRepository =
            associationRepository
                ?: return Result.failure(
                    IllegalStateException(
                        "AssociationRepository not initialized in UserRepositoryLocal."))

        // case 3: when user tries to unsubscribe to an invalid association
        if (associationRepository.getAssociation(associationId) == null) {
            return Result.failure(
                NoSuchElementException(
                    "Association with ID $associationId does not exist in the repository."))
        }

        // case 4: the user is trying to unsubscribe from an association they are not subscribed to
        if (!currentUser.subscriptions.contains(associationId)) {
            return Result.failure(
                IllegalArgumentException(
                    "User is already subscribed to association with ID: $associationId"))
        }

        // case 5: user can unsubscribe to association
        val updatedUser = currentUser.copy(subscriptions = currentUser.subscriptions - associationId)
        // reused updateUser() method
        return updateUser(currentUser.id, updatedUser)
    }
}
