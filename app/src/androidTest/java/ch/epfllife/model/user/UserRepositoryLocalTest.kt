package ch.epfllife.model.user

import ch.epfllife.example_data.ExampleUsers
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UserRepositoryLocalTest {

  // we need this to declare the property for the whole class
  private lateinit var repositoryUser: UserRepositoryLocal

  @Before
  fun setup() {
    repositoryUser = UserRepositoryLocal()
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
}
