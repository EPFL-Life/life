package ch.epfllife.model.user

import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.utils.FirestoreLifeTest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Very extensive and maybe a bit repetitive tests to ensure passing of roles works as expected. */
class UserRepositoryFirestoreRoleTest : FirestoreLifeTest() {

  @Test
  fun createUser_withAdminRole_persistsRole() = runTest {
    // Arrange
    val authUid = Firebase.auth.currentUser!!.uid
    val adminUser = ExampleUsers.adminUser.copy(id = authUid)

    // Act
    val createResult = db.userRepo.createUser(adminUser)

    // Assert
    assertTrue(createResult.isSuccess)
    val retrievedUser = db.userRepo.getUser(authUid)
    assertNotNull(retrievedUser)
    assertEquals(UserRole.ADMIN, retrievedUser?.role)
    // ExampleUsers.adminUser doesn't have managed associations by default, so we check what's in
    // the example
    assertEquals(ExampleUsers.adminUser.managedAssociationIds, retrievedUser?.managedAssociationIds)
  }

  @Test
  fun createUser_withAssociationAdminRole_persistsRole() = runTest {
    // Arrange
    val authUid = Firebase.auth.currentUser!!.uid
    val associationAdminUser = ExampleUsers.associationAdminUser.copy(id = authUid)

    // Act
    val createResult = db.userRepo.createUser(associationAdminUser)

    // Assert
    assertTrue(createResult.isSuccess)
    val retrievedUser = db.userRepo.getUser(authUid)
    assertNotNull(retrievedUser)
    assertEquals(UserRole.ASSOCIATION_ADMIN, retrievedUser?.role)
    assertEquals(
        ExampleUsers.associationAdminUser.managedAssociationIds,
        retrievedUser?.managedAssociationIds)
  }

  @Test
  fun createUser_withDefaultRole_persistsUserRole() = runTest {
    // Arrange
    val authUid = Firebase.auth.currentUser!!.uid
    val regularUser = ExampleUsers.user1.copy(id = authUid)

    // Act
    val createResult = db.userRepo.createUser(regularUser)

    // Assert
    assertTrue(createResult.isSuccess)
    val retrievedUser = db.userRepo.getUser(authUid)
    assertNotNull(retrievedUser)
    assertEquals(UserRole.USER, retrievedUser?.role)
    assertTrue(retrievedUser?.managedAssociationIds?.isEmpty() == true)
  }
}
