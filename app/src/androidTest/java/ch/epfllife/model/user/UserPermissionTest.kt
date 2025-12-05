package ch.epfllife.model.user

import ch.epfllife.example_data.ExampleUsers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserPermissionTest {

  @Test
  fun canAddAssociation_admin_returnsTrue() {
    assertTrue(UserPermissions.canAddAssociation(ExampleUsers.adminUser))
  }

  @Test
  fun canAddAssociation_associationAdmin_returnsFalse() {
    assertFalse(UserPermissions.canAddAssociation(ExampleUsers.associationAdminUser))
  }

  @Test
  fun canAddAssociation_user_returnsFalse() {
    assertFalse(UserPermissions.canAddAssociation(ExampleUsers.user1))
  }

  @Test
  fun canEditAssociation_admin_returnsTrue() {
    assertTrue(UserPermissions.canEditAssociation(ExampleUsers.adminUser, "anyId"))
  }

  @Test
  fun canEditAssociation_associationAdmin_managedAssociation_returnsTrue() {
    // associationAdminUser manages "assoc1"
    assertTrue(UserPermissions.canEditAssociation(ExampleUsers.associationAdminUser, "assoc1"))
  }

  @Test
  fun canEditAssociation_associationAdmin_unmanagedAssociation_returnsFalse() {
    // associationAdminUser manages "assoc1", not "assoc2"
    assertFalse(UserPermissions.canEditAssociation(ExampleUsers.associationAdminUser, "assoc2"))
  }

  @Test
  fun canEditAssociation_user_returnsFalse() {
    // user1 has no managed associations and is USER role
    assertFalse(UserPermissions.canEditAssociation(ExampleUsers.user1, "assoc1"))
  }
}
