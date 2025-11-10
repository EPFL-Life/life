package ch.epfllife.model.association

import ch.epfllife.model.event.Event

/** Represents a repository that manages Association data. */
interface AssociationRepository {

  /**
   * Generates and returns a new unique identifier for an association.
   *
   * @return A new unique identifier.
   */
  fun getNewUid(): String

  /**
   * Retrieves a specific association by its unique identifier.
   *
   * @param associationId The unique ID of the association to fetch.
   * @return The [Association] object, or null if not found.
   */
  suspend fun getAssociation(associationId: String): Association?

  /**
   * Retrieves a list of all associations.
   *
   * @return A list of all [Association] objects.
   */
  suspend fun getAllAssociations(): List<Association>

  /**
   * Creates a new association in the data source.
   *
   * @param association The [Association] object to create.
   */
  suspend fun createAssociation(association: Association): Result<Unit>

  /**
   * Updates an existing association's data. The ID is retrieved from the passed Association object
   *
   * @param newAssociation The [Association] object with the updated information.
   */
  suspend fun updateAssociation(associationId: String, newAssociation: Association): Result<Unit>

  /**
   * Deletes an association from the data source.
   *
   * @param associationId The unique ID of the association to delete.
   * @return [Result.success] if deletion is successful, [Result.failure] if the association was not
   *   found.
   */
  suspend fun deleteAssociation(associationId: String): Result<Unit>

  /**
   * Retrieves all events organized by a specific association.
   *
   * @param associationId The unique ID of the association.
   * @return A list of [Event] objects associated with the given ID.
   */
  suspend fun getEventsForAssociation(associationId: String): List<Event>
}
