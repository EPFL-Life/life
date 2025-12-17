package ch.epfllife.model.user

import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.Association
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.event.Event
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.FirestoreLifeTest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRepositoryFirebaseTest : FirestoreLifeTest() {

  // ---Tests for getCurrentUser---

  fun setUpSimple(): Triple<User, Association, Event> {
    return runBlocking {
      // Arrange: see that for association and event I don't add asserts (they are covered in the
      // other tests )
      val assoc1 = ExampleAssociations.association1
      db.assocRepo.createAssociation(assoc1)
      val event = ExampleEvents.event1
      db.eventRepo.createEvent(event)

      val authUid = signInTestUserUsingAuth()

      // to avoid problems, we assign the sample user the UID of the
      // authenticated user, thus simulating that they are the same.
      val user1 = ExampleUsers.user1.copy(id = authUid)
      db.userRepo.createUser(user1)
      Triple(user1, assoc1, event)
    }
  }

  @Test
  fun getCurrentUser_userAuthenticated_and_existsInDb_returnsUser() = runTest {
    // Arrange: Get the authenticated user's ID from the auth emulator.
    // The base class (FirestoreLifeTest) signs in a default test user in its @Before.
    val authenticatedUser = Firebase.auth.currentUser
    assertNotNull("User should be authenticated by FirestoreLifeTest.setUp", authenticatedUser)
    val authUid = authenticatedUser!!.uid

    // Arrange: Create a user document in Firestore that matches the auth UID.
    // The name "Test User" matches the name in the fake token from FakeCredentialManager
    // We use user1 as base user if any changes het made in the future User class
    val userToCreateInDb = ExampleUsers.user1.copy(id = authUid)

    val createResult = db.userRepo.createUser(userToCreateInDb)
    assertTrue("Failed to create user for test", createResult.isSuccess)
    assertEquals(1, getUserCount())

    // Act
    val currentUser = db.userRepo.getCurrentUser()

    // Assert NN and equality
    assertNotNull(currentUser)
    assertEquals(userToCreateInDb, currentUser)
    assertEquals(authUid, currentUser?.id)
  }

  @Test
  fun getCurrentUser_userAuthenticated_but_notInDb_returnsNull() = runTest {
    // Arrange
    // The base class signs us in, and @Before clears the DB.
    // So, we are authenticated, but no user doc exists.
    assertEquals(0, getUserCount())

    // Act
    val currentUser = db.userRepo.getCurrentUser()

    // Assert user does not exist in DB
    assertNull(currentUser)
  }

  @Test
  fun getCurrentUser_userNotAuthenticated_returnsNull() = runTest {
    // Arrange
    // The base class @Before signs us in. We must sign out for this test.
    Firebase.auth.signOut()

    // Act
    val currentUser = db.userRepo.getCurrentUser()

    // Assert
    assertNull(currentUser)
  }

  // ---Tests for createUser---

  @Test
  fun createUser_validUser_returnsSuccess() = runTest {
    // Arrange: import a valid user
    val authUid = Firebase.auth.currentUser!!.uid
    val user1 = ExampleUsers.user1.copy(id = authUid)

    // Act: create user in database
    val createUserResult = db.userRepo.createUser(user1)

    // Assert: check user got created successfully and is correctly retrived
    assert(createUserResult.isSuccess)
    assertEquals(1, getUserCount())
    assertEquals(user1, db.userRepo.getUser(user1.id))
  }

  // ---Tests for getAllUsers()---

  @Test
  fun getAllUsers_validUsers_returnsListOfUsers() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    val user2 = ExampleUsers.user2
    val user3 = ExampleUsers.user3

    // Act: create users in database
    db.userRepo.createUser(user1)
    db.userRepo.createUser(user2)
    db.userRepo.createUser(user3)

    // Assert: 3 users got added to database
    assertEquals(3, getUserCount())

    // Act: retrieve all users
    val allUsers = db.userRepo.getAllUsers()

    // Assert: retrieved users are the same as the ones added
    assertEquals(3, allUsers.size)
    assert(allUsers.contains(user1))
    assert(allUsers.contains(user2))
    assert(allUsers.contains(user3))
  }

  @Test
  fun getAllUsers_emptyDatabase_returnsEmptyList() = runTest {
    // Arrange
    assertEquals(0, getUserCount())

    // Act
    val allUsers = db.userRepo.getAllUsers()

    // Assert
    assertTrue(allUsers.isEmpty())
  }

  // --- Tests for getUser ---

  @Test
  fun getUser_nonExistentUser_returnsNull() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1

    // Act: get user that doesn't exist
    val getUserResult = db.userRepo.getUser(user1.id)

    // Assert: check user doesn't exist
    assertEquals(null, getUserResult)
  }

  // --- Tests for updateUser ---

  @Test
  fun updateUser_validUser_returnsSuccess() = runTest {
    // Arrange: add basic user to db
    val user1 = ExampleUsers.user1
    db.userRepo.createUser(user1)
    assertEquals(1, getUserCount())

    // Arrange: create an updated version of the user (change name+subscriptions)
    val updatedUser = user1.copy(name = "Alex Updated", subscriptions = listOf("asso-cs"))

    // Act: update user in database
    val updateUserResult = db.userRepo.updateUser(user1.id, updatedUser)

    // Assert: check user got updated successfully and can be retrieved
    assert(updateUserResult.isSuccess)
    assertEquals(1, getUserCount())

    val retrievedUser = db.userRepo.getUser(user1.id)
    assertEquals(updatedUser, retrievedUser)
    assertEquals(updatedUser.name, retrievedUser?.name)
    assertEquals(updatedUser.subscriptions, retrievedUser?.subscriptions)
  }

  @Test
  fun updateUser_nonExistentUser_returnsFailure() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    db.userRepo.createUser(user1)
    assertEquals(1, getUserCount())

    // Act: try to update user with non-existent ID
    // This tests the 'docRef.get().await().exists()' check in your repo
    val updatedUser = ExampleUsers.user2.copy(id = "notExistentId")
    val updateUserResult = db.userRepo.updateUser("notExistentId", updatedUser)

    // Assert: update failed and original user was not affected
    assert(updateUserResult.isFailure)
    assertTrue(updateUserResult.exceptionOrNull() is NoSuchElementException)
    // original user still same:
    assertEquals(user1, db.userRepo.getUser(user1.id))
    assertEquals(1, getUserCount())
  }

  @Test
  fun updateUser_idMismatch_returnsFailure() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    db.userRepo.createUser(user1)
    assertEquals(1, getUserCount())

    // Act: try to update user but with mismatched id in the object
    val updatedUser = ExampleUsers.user2.copy(id = "mismatchedId")
    val updateUserResult = db.userRepo.updateUser(user1.id, updatedUser)

    // Assert: update failed and association was not affected
    assert(updateUserResult.isFailure)
    assertTrue(updateUserResult.exceptionOrNull() is IllegalArgumentException)
    assertEquals(user1, db.userRepo.getUser(user1.id))
    assertEquals(1, getUserCount())
  }

  // ---DeleteUser()---
  @Test
  fun deleteUser_validUser_returnsSuccess() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    db.userRepo.createUser(user1)
    assertEquals(1, getUserCount())

    // Act
    val deleteUserResult = db.userRepo.deleteUser(user1.id)

    // Assert
    assert(deleteUserResult.isSuccess)
    assertEquals(0, getUserCount())
  }

  @Test
  fun deleteUser_nonExistentUser_returnsFailure() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    db.userRepo.createUser(user1)
    assertEquals(1, getUserCount())

    // Act
    val deleteUserResult = db.userRepo.deleteUser("nonExistentId")

    // Assert
    assert(deleteUserResult.isFailure)
    assertTrue(deleteUserResult.exceptionOrNull() is NoSuchElementException)
    assertEquals(1, getUserCount())
  }

  /** Auxiliary method for sign in in the firebase as an authenticated user */
  suspend fun signInTestUserUsingAuth(): String {
    // we are going to ensure fresh session first
    val authInstance = Auth(FakeCredentialManager.withDefaultTestUser)
    authInstance.signOut()
    // fake credentials and sign in
    val credential = FakeCredentialManager.defaultUserCredentials
    val result = authInstance.signInWithCredential(credential)

    // return the id of the auth user.
    return when (result) {
      is SignInResult.Success -> result.user.uid
      SignInResult.Cancelled -> error("Sign-in was cancelled in test")
      SignInResult.Failure -> error("Failed to sign in test user")
    }
  }

  @Test
  fun subscribe_success() = runTest {
    val (user, _, event) = setUpSimple()
    // Assert
    assertEquals("User count must be 1", 1, getUserCount())

    // action : subscribe
    val result = db.userRepo.subscribeToEvent(event.id)

    // assert
    assertTrue("Event subscription must succeed", result.isSuccess)
    val updated = db.userRepo.getUser(user.id)
    assertTrue(
        "Enrolled events must be updated",
        updated?.enrolledEvents?.contains(event.id) ?: false,
    )
  }

  @Test
  fun subscribe_fails_when_logged_out() = runTest {
    val (_, _, event) = setUpSimple()

    // we sign out, now there is no user logged
    Firebase.auth.signOut()

    // action: subscribe to event without having a current authenticated user
    val result = db.userRepo.subscribeToEvent(event.id)
    // assert
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribe_fails_when_event_missing() = runTest {
    setUpSimple()

    // act: try to subscribe to non-existent event
    val result = db.userRepo.subscribeToEvent("nonExistentEventId")

    // assert
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribe_fails_when_subscribed_before() = runTest {
    val (_, _, event) = setUpSimple()

    // act: first subscribe
    val first = db.userRepo.subscribeToEvent(event.id)
    assertTrue(first.isSuccess)

    // act: second subscribe should fail
    val second = db.userRepo.subscribeToEvent(event.id)
    // assert
    assertTrue(second.isFailure)
  }

  @Test
  fun unsubscribe_success() = runTest {
    val (user, _, event) = setUpSimple()

    val sub = db.userRepo.subscribeToEvent(event.id)
    assertTrue(sub.isSuccess)

    // act: unsubscribe
    val result = db.userRepo.unsubscribeFromEvent(event.id)

    // assert
    assertTrue(result.isSuccess)
    val updated = db.userRepo.getUser(user.id)
    assertTrue(!(updated?.enrolledEvents?.contains(event.id) ?: false))
  }

  @Test
  fun unsubscribe_fails_when_logged_out() = runTest {
    val (_, _, event) = setUpSimple()

    // action: sign out
    Firebase.auth.signOut()
    // act
    val result = db.userRepo.unsubscribeFromEvent(event.id)
    // assert
    assertTrue(result.isFailure)
  }

  @Test
  fun unsubscribe_fails_when_event_missing() = runTest {
    setUpSimple()

    // act: try to unsubscribe from non-existent event
    val result = db.userRepo.unsubscribeFromEvent("nonExistentEventId")

    // assert
    assertTrue(result.isFailure)
  }

  @Test
  fun unsubscribe_fails_when_not_subscribed() = runTest {
    val (_, _, event) = setUpSimple()

    // act
    val result = db.userRepo.unsubscribeFromEvent(event.id)

    // assert
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToAssociation_success() = runTest {
    val (user, association, _) = setUpSimple()

    // action: subscribe to event
    val result = db.userRepo.subscribeToAssociation(association.id)

    // assert
    assertTrue(result.isSuccess)

    // ensure that the subscription is updated
    val updatedUser = db.userRepo.getUser(user.id)
    assertTrue(updatedUser?.subscriptions?.contains(association.id) ?: false)
  }

  @Test
  fun subscribeToAssociation_returnsFailure_whenLoggedOut() = runTest {
    val (_, association, _) = setUpSimple()

    // action: subscribe to a association without being logged in
    Firebase.auth.signOut()
    val result = db.userRepo.subscribeToAssociation(association.id)
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToAssociation_returnsFailure_whenAssociationDoesNotExist() = runTest {
    val (_, _, _) = setUpSimple()

    // action: subscribe to a non-existent association
    val result = db.userRepo.subscribeToAssociation("nonExistentEventId")
    // assert
    assertTrue(result.isFailure)
  }

  @Test
  fun subscribeToAssociation_returnsFailure_whenAlreadySubscribed() = runTest {
    val (_, association, _) = setUpSimple()

    // action: enroll to association
    val firstAssociation = db.userRepo.subscribeToAssociation(association.id)
    assertTrue(firstAssociation.isSuccess)

    // action: enroll to association that user had already subscribed
    val secondAssociation = db.userRepo.subscribeToAssociation(association.id)
    assertTrue(secondAssociation.isFailure)
  }

  @Test
  fun unsubscribeFromAssociation_success() = runTest {
    val (user, association, _) = setUpSimple()
    val sub = db.userRepo.subscribeToAssociation(association.id)
    assertTrue(sub.isSuccess)

    // action: unsubscribe
    val result = db.userRepo.unsubscribeFromAssociation(association.id)

    // assert
    assertTrue(result.isSuccess)
    val updated = db.userRepo.getUser(user.id)
    assertTrue(!(updated?.subscriptions?.contains(association.id) ?: false))
  }

  @Test
  fun unsubscribeFromAssociation_returnsFailure_whenLoggedOut() = runTest {
    val (_, association, _) = setUpSimple()

    // action: try to unsubscribe from event while logged out
    Firebase.auth.signOut()
    val result = db.userRepo.unsubscribeFromAssociation(association.id)
    // assert: the action must fail
    assertTrue(result.isFailure)
  }

  @Test
  fun unsubscribeFromAssociation_returnsFailure_whenEventDoesNotExist() = runTest {
    val (_, _, _) = setUpSimple()

    // action: try to unsubscribe from a non-existent event
    val result = db.userRepo.unsubscribeFromAssociation("nonExistentEventId")

    // assert: the action must fail
    assertTrue(result.isFailure)
  }

  @Test
  fun unsubscribeFromAssociation_returnsFailure_whenNotSubscribed() = runTest {
    val (_, _, _) = setUpSimple()

    // (arrange: User is NOT subscribed to this association)
    // action: try to unsubscribe from an event they are not subscribed to
    val result = db.userRepo.unsubscribeFromAssociation(ExampleAssociations.association2.id)

    // assert: the action must fail
    assertTrue(result.isFailure)
  }

  @Test
  fun followAndUnfollow() = runTest {
    val authUid = signInTestUserUsingAuth()
    val user1 = ExampleUsers.user1.copy(id = authUid)
    db.userRepo.createUser(user1)
    val user2 = ExampleUsers.user2
    db.userRepo.createUser(user2)
    db.userRepo.followUser(user2.id)
    var updatedUser1 = db.userRepo.getUser(user1.id)
    assertTrue(updatedUser1!!.following.contains(user2.id))
    db.userRepo.unfollowUser(user2.id)
    updatedUser1 = db.userRepo.getUser(user1.id)
    assertTrue(updatedUser1!!.following.isEmpty())
  }

  @Test
  fun cannotFollowSelf() = runTest {
    val authUid = signInTestUserUsingAuth()
    val user1 = ExampleUsers.user1.copy(id = authUid)
    db.userRepo.createUser(user1)
    val result = db.userRepo.followUser(user1.id)
    assertTrue(result.isFailure)
    val updatedUser1 = db.userRepo.getUser(user1.id)
    assertTrue(updatedUser1!!.following.isEmpty())
  }

  @Test
  fun cannotFollowWithoutLogin() = runTest {
    val result = db.userRepo.followUser("any")
    assertTrue(result.isFailure)
  }
}
