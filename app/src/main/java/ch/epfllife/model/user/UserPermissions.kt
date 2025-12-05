package ch.epfllife.model.user

object UserPermissions {
  /** Checks if the user has permission to add a new association. */
  fun canAddAssociation(user: User): Boolean {
    return user.role == UserRole.ADMIN
  }

  /** Checks if the user has permission to edit the given association. */
  fun canEditAssociation(user: User, associationId: String): Boolean {
    return user.role == UserRole.ADMIN ||
        (user.role == UserRole.ASSOCIATION_ADMIN &&
            user.managedAssociationIds.contains(associationId))
  }
}
