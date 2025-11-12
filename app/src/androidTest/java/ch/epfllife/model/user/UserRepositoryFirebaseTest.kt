package ch.epfllife.model.user

import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.utils.FirestoreLifeTest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Assert.assertEquals
import org.junit.Test

class UserRepositoryFirebaseTest : FirestoreLifeTest() {

  // ---Tests for getCurrentUser---

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

    val createResult = userRepository.createUser(userToCreateInDb)
    assertTrue("Failed to create user for test", createResult.isSuccess)
    assertEquals(1, getUserCount())

    // Act
    val currentUser = userRepository.getCurrentUser()

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
    val currentUser = userRepository.getCurrentUser()

    // Assert user does not exist in DB
    assertNull(currentUser)
  }

  @Test
  fun getCurrentUser_userNotAuthenticated_returnsNull() = runTest {
    // Arrange
    // The base class @Before signs us in. We must sign out for this test.
    Firebase.auth.signOut()

    // Act
    val currentUser = userRepository.getCurrentUser()

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
    val createUserResult = userRepository.createUser(user1)

    // Assert: check user got created successfully and is correctly retrived
    assert(createUserResult.isSuccess)
    assertEquals(1, getUserCount())
    assertEquals(user1, userRepository.getUser(user1.id))
  }

  // ---Tests for getAllUsers()---

  @Test
  fun getAllUsers_validUsers_returnsListOfUsers() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    val user2 = ExampleUsers.user2
    val user3 = ExampleUsers.user3

    // Act: create users in database
    userRepository.createUser(user1)
    userRepository.createUser(user2)
    userRepository.createUser(user3)

    // Assert: 3 users got added to database
    assertEquals(3, getUserCount())

    // Act: retrieve all users
    val allUsers = userRepository.getAllUsers()

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
    val allUsers = userRepository.getAllUsers()

    // Assert
    assertTrue(allUsers.isEmpty())
  }

  // --- Tests for getUser ---

  @Test
  fun getUser_nonExistentUser_returnsNull() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1

    // Act: get user that doesn't exist
    val getUserResult = userRepository.getUser(user1.id)

    // Assert: check user doesn't exist
    assertEquals(null, getUserResult)
  }

  // --- Tests for updateUser ---

  @Test
  fun updateUser_validUser_returnsSuccess() = runTest {
    // Arrange: add basic user to db
    val user1 = ExampleUsers.user1
    userRepository.createUser(user1)
    assertEquals(1, getUserCount())

    // Arrange: create an updated version of the user (change name+subscriptions)
    val updatedUser = user1.copy(name = "Alex Updated", subscriptions = listOf("asso-cs"))

    // Act: update user in database
    val updateUserResult = userRepository.updateUser(user1.id, updatedUser)

    // Assert: check user got updated successfully and can be retrieved
    assert(updateUserResult.isSuccess)
    assertEquals(1, getUserCount())

    val retrievedUser = userRepository.getUser(user1.id)
    assertEquals(updatedUser, retrievedUser)
    assertEquals(updatedUser.name, retrievedUser?.name)
    assertEquals(updatedUser.subscriptions, retrievedUser?.subscriptions)
  }

  @Test
  fun updateUser_nonExistentUser_returnsFailure() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    userRepository.createUser(user1)
    assertEquals(1, getUserCount())

    // Act: try to update user with non-existent ID
    // This tests the 'docRef.get().await().exists()' check in your repo
    val updatedUser = ExampleUsers.user2.copy(id = "notExistentId")
    val updateUserResult = userRepository.updateUser("notExistentId", updatedUser)

    // Assert: update failed and original user was not affected
    assert(updateUserResult.isFailure)
    assertTrue(updateUserResult.exceptionOrNull() is NoSuchElementException)
    // original user still same:
    assertEquals(user1, userRepository.getUser(user1.id))
    assertEquals(1, getUserCount())
  }

  @Test
  fun updateUser_idMismatch_returnsFailure() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    userRepository.createUser(user1)
    assertEquals(1, getUserCount())

    // Act: try to update user but with mismatched id in the object
    val updatedUser = ExampleUsers.user2.copy(id = "mismatchedId")
    val updateUserResult = userRepository.updateUser(user1.id, updatedUser)

    // Assert: update failed and association was not affected
    assert(updateUserResult.isFailure)
    assertTrue(updateUserResult.exceptionOrNull() is IllegalArgumentException)
    assertEquals(user1, userRepository.getUser(user1.id))
    assertEquals(1, getUserCount())
  }

  // ---DeleteUser()---
  @Test
  fun deleteUser_validUser_returnsSuccess() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    userRepository.createUser(user1)
    assertEquals(1, getUserCount())

    // Act
    val deleteUserResult = userRepository.deleteUser(user1.id)

    // Assert
    assert(deleteUserResult.isSuccess)
    assertEquals(0, getUserCount())
  }

  @Test
  fun deleteUser_nonExistentUser_returnsFailure() = runTest {
    // Arrange
    val user1 = ExampleUsers.user1
    userRepository.createUser(user1)
    assertEquals(1, getUserCount())

    // Act
    val deleteUserResult = userRepository.deleteUser("nonExistentId")

    // Assert
    assert(deleteUserResult.isFailure)
    assertTrue(deleteUserResult.exceptionOrNull() is NoSuchElementException)
    assertEquals(1, getUserCount())
  }
}
