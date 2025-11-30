package ch.epfllife.model.user

import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.AssociationRepositoryLocal
import ch.epfllife.model.event.EventRepositoryLocal
import java.lang.IllegalStateException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UserRepositoryLocalTest {

  // we need this to declare the property for the whole class
  private lateinit var repositoryUser: UserRepositoryLocal
  private lateinit var repositoryEventLocal: EventRepositoryLocal
  private lateinit var repositoryAssociationLocal: AssociationRepositoryLocal

  @Before
  fun setup() {
    repositoryEventLocal = EventRepositoryLocal()
    repositoryAssociationLocal = AssociationRepositoryLocal(repositoryEventLocal)
    repositoryUser = UserRepositoryLocal(repositoryEventLocal, repositoryAssociationLocal)
  }

  @Test
  fun getAllUsers_returnsAllUsers() = runTest {
    // arrange: add 3 example users
    repositoryUser.createUser(ExampleUsers.user1)
    repositoryUser.createUser(ExampleUsers.user2)
    repositoryUser.createUser(ExampleUsers.user3)

    // prepare: get all users
    val allUsers = repositoryUser.getAllUsers()

    // assert: check all users were saved
    assertEquals(3, allUsers.size)

    // assert: users saved correctly
    val expectedUsers = listOf(ExampleUsers.user1, ExampleUsers.user2, ExampleUsers.user3)
    assertTrue(allUsers.containsAll(expectedUsers)) // containAll bcs order NOT guaranteed
  }

  @Test
  fun updateUser_updatesUser() = runTest {
    // arrange: add user to repository
    repositoryUser.createUser(ExampleUsers.user1)

    // action: create an updated version
    val updatedUser = ExampleUsers.user1.copy(name = "Updated Name")

    // action: update user in repo
    val result = repositoryUser.updateUser(ExampleUsers.user1.id, updatedUser)

    // assert: operation was successful and user updated
    assertTrue(result.isSuccess)
    assertEquals(updatedUser, repositoryUser.getUser(ExampleUsers.user1.id))
  }

  @Test
  fun deleteUser_removesUser() = runTest {

    // arrange: add user to repository
    repositoryUser.createUser(ExampleUsers.user1)

    // action: try to delete non-existent user
    val result = repositoryUser.deleteUser(ExampleUsers.user1.id)

    // assert: operation was successful and user was removed
    assertTrue(result.isSuccess)
    assertEquals(0, repositoryUser.getAllUsers().size)
    assertEquals(null, repositoryUser.getUser(ExampleUsers.user1.id))
  }

  @Test
  fun deleteUser_returnsFailureForNonExistentUser() = runTest {

    // arrange: add user which should not be deleted
    repositoryUser.createUser(ExampleUsers.user1)

    // action: try to delete non-existent user
    val response = repositoryUser.deleteUser("nonExistentUserId")

    // assert: action was a failure and nothing was removed
    assertTrue(response.isFailure)
    assertEquals(1, repositoryUser.getAllUsers().size)
  }

  @Test
  fun updateUser_returnsFailureForNonExistentUser() = runTest {

    // arrange: add user which should not be updated
    repositoryUser.createUser(ExampleUsers.user1)

    // assert: user was added to repository
    assertEquals(1, repositoryUser.getAllUsers().size)

    // action: try to update non existent user
    val response = repositoryUser.updateUser("nonExistentUserId", ExampleUsers.user1)

    // assert: action was a failure (also check type) and nothing was updated
    assertTrue(response.isFailure)
    assertEquals(1, repositoryUser.getAllUsers().size)
    assertTrue(response.exceptionOrNull() is NoSuchElementException)
  }

  @Test
  fun updateUser_returnFailureForIdMismatch() = runTest {

    // arrange: add user which should not be updated
    repositoryUser.createUser(ExampleUsers.user1)

    // assert: user was added to repository
    assertEquals(1, repositoryUser.getAllUsers().size)

    // action: try to update non existent user
    val response = repositoryUser.updateUser(ExampleUsers.user1.id, ExampleUsers.user2)

    // assert: action was a failure (also check type) and nothing was updated
    assertTrue(response.isFailure)
    assertEquals(1, repositoryUser.getAllUsers().size)
    assertTrue(response.exceptionOrNull() is IllegalArgumentException)
  }

  @Test
  fun createUser_withExistingId_returnsFailure() = runTest {
    // arrange: Add a user to the repository
    val originalUser = ExampleUsers.user1
    repositoryUser.createUser(originalUser)

    // assert: user saved successfully
    assertEquals(1, repositoryUser.getAllUsers().size)

    // action: Attempt to create another user with the SAME ID
    val duplicateUser = ExampleUsers.user2.copy(id = originalUser.id)
    val result = repositoryUser.createUser(duplicateUser)

    // Assert: The operation should fail
    assertTrue(result.isFailure) // Creating a user with a duplicate ID should fail
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)

    // Assert: The repository should NOT have been modified
    val usersInRepo = repositoryUser.getAllUsers()
    assertEquals(1, usersInRepo.size)
    assertEquals(originalUser, usersInRepo[0])
  }

  @Test
  fun getCurrentUser_returnsCorrectUserWhenLoggedIn() = runTest {
    // arrange: add some users
    repositoryUser.createUser(ExampleUsers.user1)
    repositoryUser.createUser(ExampleUsers.user2)

    // action: simulate login
    repositoryUser.simulateLogin(ExampleUsers.user1.id)
    val currentUser = repositoryUser.getCurrentUser()

    // assert: check current user is correct
    assertEquals(ExampleUsers.user1, currentUser)
  }

  @Test
  fun getCurrentUser_returnsNullWhenLoggedOut() = runTest {
    // Add a user, but don't log in
    repositoryUser.createUser(ExampleUsers.user1)

    // Ensure no one is logged in by default
    val currentUser = repositoryUser.getCurrentUser()
    assertEquals(null, currentUser)
  }

  @Test
  fun subscribeToEvent_success() = runTest {
    // arrange: add an user
    repositoryUser.createUser(ExampleUsers.user3)
    // arrange: add an event
    val event = ExampleEvents.event3
    repositoryEventLocal.createEvent(event)
    // action: simulate login
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action: subscribe to event
    val result = repositoryUser.subscribeToEvent(event.id)

    // assert
    assertTrue(result.isSuccess)

    // ensure that the subscription is updated
    val updatedUser = repositoryUser.getUser(ExampleUsers.user3.id)
    assertTrue(updatedUser?.enrolledEvents?.contains(event.id) ?: false)
  }

  @Test
  fun subscribeToEvent_returnsFailure_whenLoggedOut() = runTest {
    // arrange: add an user
    repositoryUser.createUser(ExampleUsers.user3)
    // arrange: add an event
    val event = ExampleEvents.event3
    repositoryEventLocal.createEvent(event)
    // action: subscribe to a event without being logged in
    val result = repositoryUser.subscribeToEvent(ExampleEvents.event1.id)
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToEvent_returnsFailure_whenEventRepoNotInitialized() = runTest {
    // arrange: initialize a null event repository
    repositoryUser = UserRepositoryLocal(null)
    // action: create an user + login
    repositoryUser.createUser(ExampleUsers.user3)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action:subscribe to a event with a null event repository
    val result = repositoryUser.subscribeToEvent(ExampleEvents.event1.id)
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToEvent_returnsFailure_whenEventDoesNotExist() = runTest {
    // arrange: create user + login
    repositoryUser.createUser(ExampleUsers.user3)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action: subscribe to a non-existent event
    val result = repositoryUser.subscribeToEvent("nonExistentEventId")
    // assert
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToEvent_returnsFailure_whenAlreadySubscribed() = runTest {
    // arrange: create an event
    repositoryUser.createUser(ExampleUsers.user3)
    val event = ExampleEvents.event1
    repositoryEventLocal.createEvent(event)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action: enroll to event
    val firstEvent = repositoryUser.subscribeToEvent(event.id)
    assertTrue(firstEvent.isSuccess)

    // action: enroll to event that user had already subscribed
    val secondEvent = repositoryUser.subscribeToEvent(event.id)
    assertTrue(secondEvent.isFailure)
  }

  @Test
  fun unsubscribeFromEvent_success() = runTest {
    // arrange: create user + event, subscribe first
    repositoryUser.createUser(ExampleUsers.user3)
    val event = ExampleEvents.event1
    repositoryEventLocal.createEvent(event)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)
    val sub = repositoryUser.subscribeToEvent(event.id)
    assertTrue(sub.isSuccess)

    // action: unsubscribe
    val result = repositoryUser.unsubscribeFromEvent(event.id)

    // assert
    assertTrue(result.isSuccess)
    val updated = repositoryUser.getUser(ExampleUsers.user3.id)
    assertTrue(!(updated?.subscriptions?.contains(event.id) ?: false))
  }

  @Test
  fun unsubscribeFromEvent_returnsFailure_whenLoggedOut() = runTest {
    // arrange: add an user
    repositoryUser.createUser(ExampleUsers.user3)
    // arrange: add an event
    repositoryEventLocal.createEvent(ExampleEvents.event1)
    // (arrange: No user is logged in by default)

    // action: try to unsubscribe from event while logged out
    val result = repositoryUser.unsubscribeFromEvent(ExampleEvents.event1.id)
    // assert: the action must fail
    assertTrue(result.isFailure)
  }

  @Test
  fun unsubscribeFromEvent_returnsFailure_whenEventRepoNotInitialized() = runTest {
    // arrange: initialize the repository with a null event repository
    repositoryUser = UserRepositoryLocal(null)
    // arrange: create an user + login
    repositoryUser.createUser(ExampleUsers.user3)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action: try to unsubscribe from event with a null event repository
    val result = repositoryUser.unsubscribeFromEvent(ExampleEvents.event1.id)
    // assert: the action must fail and check the type of failure
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  @Test
  fun unsubscribeFromEvent_returnsFailure_whenEventDoesNotExist() = runTest {
    // arrange: create user + login
    repositoryUser.createUser(ExampleUsers.user3)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)
    // (arrange: Event does not exist in repositoryEventLocal)

    // action: try to unsubscribe from a non-existent event
    val result = repositoryUser.unsubscribeFromEvent("nonExistentEventId")

    // assert: the action must fail
    assertTrue(result.isFailure)
  }

  @Test
  fun unsubscribeFromEvent_returnsFailure_whenNotSubscribed() = runTest {
    // arrange: create user + event
    repositoryUser.createUser(ExampleUsers.user3)
    val event = ExampleEvents.event1
    repositoryEventLocal.createEvent(event)
    // arrange: login user
    repositoryUser.simulateLogin(ExampleUsers.user3.id)
    // (arrange: User is NOT subscribed to this event)

    // action: try to unsubscribe from an event they are not subscribed to
    val result = repositoryUser.unsubscribeFromEvent(event.id)

    // assert: the action must fail
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToAssociation_success() = runTest {
    // arrange: add an user
    repositoryUser.createUser(ExampleUsers.user3)
    // arrange: add an association
    val association = ExampleAssociations.association3
    repositoryAssociationLocal.createAssociation(association)
    // action: simulate login
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action: subscribe to event
    val result = repositoryUser.subscribeToAssociation(association.id)

    // assert
    assertTrue(result.isSuccess)

    // ensure that the subscription is updated
    val updatedUser = repositoryUser.getUser(ExampleUsers.user3.id)
    assertTrue(updatedUser?.subscriptions?.contains(association.id) ?: false)
  }

  @Test
  fun subscribeToAssociation_returnsFailure_whenLoggedOut() = runTest {
    // arrange: add an user
    repositoryUser.createUser(ExampleUsers.user3)
    // arrange: add an association
    val association = ExampleAssociations.association3
    repositoryAssociationLocal.createAssociation(association)
    // action: subscribe to a association without being logged in
    val result = repositoryUser.subscribeToAssociation(association.id)
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToAssociation_returnsFailure_whenAssociationRepoNotInitialized() = runTest {
    // arrange: initialize a null association repository
    repositoryUser = UserRepositoryLocal(null, null)
    // action: create an user + login
    repositoryUser.createUser(ExampleUsers.user3)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action:subscribe to an association with a null event repository
    val result = repositoryUser.subscribeToAssociation(ExampleAssociations.association3.id)
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToAssociation_returnsFailure_whenAssociationDoesNotExist() = runTest {
    // arrange: create user + login
    repositoryUser.createUser(ExampleUsers.user3)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action: subscribe to a non-existent association
    val result = repositoryUser.subscribeToAssociation("nonExistentEventId")
    // assert
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToAssociation_returnsFailure_whenAlreadySubscribed() = runTest {
    // arrange: create an association
    repositoryUser.createUser(ExampleUsers.user3)
    val association = ExampleAssociations.association3
    repositoryAssociationLocal.createAssociation(association)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action: enroll to association
    val firstAssociation = repositoryUser.subscribeToAssociation(association.id)
    assertTrue(firstAssociation.isSuccess)

    // action: enroll to association that user had already subscribed
    val secondAssociation = repositoryUser.subscribeToAssociation(association.id)
    assertTrue(secondAssociation.isFailure)
  }

  @Test
  fun unsubscribeFromAssociation_success() = runTest {
    // arrange: create user + association, subscribe first
    repositoryUser.createUser(ExampleUsers.user3)
    val association = ExampleAssociations.association3
    repositoryAssociationLocal.createAssociation(association)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)
    val sub = repositoryUser.subscribeToAssociation(association.id)
    assertTrue(sub.isSuccess)

    // action: unsubscribe
    val result = repositoryUser.unsubscribeFromAssociation(association.id)

    // assert
    assertTrue(result.isSuccess)
    val updated = repositoryUser.getUser(ExampleUsers.user3.id)
    assertTrue(!(updated?.subscriptions?.contains(association.id) ?: false))
  }

  @Test
  fun unsubscribeFromAssociation_returnsFailure_whenLoggedOut() = runTest {
    // arrange: add an user
    repositoryUser.createUser(ExampleUsers.user3)
    // arrange: add an event
    repositoryAssociationLocal.createAssociation(ExampleAssociations.association3)
    // (arrange: No user is logged in by default)

    // action: try to unsubscribe from event while logged out
    val result = repositoryUser.unsubscribeFromAssociation(ExampleAssociations.association3.id)
    // assert: the action must fail
    assertTrue(result.isFailure)
  }

  @Test
  fun unsubscribeFromAssociation_returnsFailure_whenAssociationRepoNotInitialized() = runTest {
    // arrange: initialize the repository with a null event repository
    repositoryUser = UserRepositoryLocal(null, null)
    // arrange: create an user + login
    repositoryUser.createUser(ExampleUsers.user3)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)

    // action: try to unsubscribe from event with a null event repository
    val result = repositoryUser.unsubscribeFromAssociation(ExampleAssociations.association3.id)
    // assert: the action must fail and check the type of failure
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  @Test
  fun unsubscribeFromAssociation_returnsFailure_whenEventDoesNotExist() = runTest {
    // arrange: create user + login
    repositoryUser.createUser(ExampleUsers.user3)
    repositoryUser.simulateLogin(ExampleUsers.user3.id)
    // (arrange: Event does not exist in repositoryEventLocal)

    // action: try to unsubscribe from a non-existent event
    val result = repositoryUser.unsubscribeFromAssociation("nonExistentEventId")

    // assert: the action must fail
    assertTrue(result.isFailure)
  }

  @Test
  fun unsubscribeFromAssociation_returnsFailure_whenNotSubscribed() = runTest {
    // arrange: create user + association
    repositoryUser.createUser(ExampleUsers.user3)
    val association = ExampleAssociations.association3
    repositoryAssociationLocal.createAssociation(association)
    // arrange: login user
    repositoryUser.simulateLogin(ExampleUsers.user3.id)
    // (arrange: User is NOT subscribed to this association)

    // action: try to unsubscribe from an event they are not subscribed to
    val result = repositoryUser.unsubscribeFromAssociation(association.id)

    // assert: the action must fail
    assertTrue(result.isFailure)
  }
}
