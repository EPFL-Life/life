package ch.epfllife.model.association

import ch.epfllife.model.event.Event

/** Represents a repository that manages Association data. */
interface AssociationRepository {

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
  suspend fun createAssociation(association: Association)

  /**
   * Updates an existing association's data.
   *
   * @param associationId The ID of the association to update.
   * @param newAssociation The [Association] object with the updated information.
   */
  suspend fun updateAssociation(associationId: String, newAssociation: Association)

  /**
   * Retrieves all events organized by a specific association.
   *
   * @param associationId The unique ID of the association.
   * @return A list of [Event] objects associated with the given ID.
   */
  suspend fun getEventsForAssociation(associationId: String): List<Event>
}
