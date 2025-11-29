package ch.epfllife.model.event

/** Represents a repository that manages Event items. */
interface EventRepository {

  /** Generates and returns a new unique identifier for an Event item. */
  fun getNewUid(): String

  /**
   * Retrieves all Event items from the repository.
   *
   * @return A list of all Event items.
   */
  suspend fun getAllEvents(): List<Event>

  /**
   * Retrieves a specific Event item by its unique identifier.
   *
   * @param eventId The unique identifier of the Event item to retrieve.
   * @return The Event item with the specified identifier.
   */
  suspend fun getEvent(eventId: String): Event?

  /**
   * Adds a new Event item to the repository.
   *
   * @param event The Event item to add.
   * @return A result indicating the success or failure of the operation.
   */
  suspend fun createEvent(event: Event): Result<Unit>

  /**
   * Edits an existing Event item in the repository.
   *
   * @param eventId The unique identifier of the Event item to edit.
   * @param newEvent The new value for the Event item.
   * @return A result indicating the success or failure of the operation.
   */
  suspend fun updateEvent(eventId: String, newEvent: Event): Result<Unit>

  /**
   * Deletes an Event item from the repository.
   *
   * @param eventId The unique identifier of the Event item to delete.
   * @return A result indicating the success or failure of the operation.
   *
   * // Example of calling deleteEvent suspend fun onDeleteEvent(eventId: String) {
   * eventRepository.deleteEvent(eventId).fold( onSuccess = { println("Event deleted!") }, onFailure
   * = { error -> println("Failed to delete: ${error.message}") } ) }
   */
  suspend fun deleteEvent(eventId: String): Result<Unit>

  fun listenAll(onChange: (List<Event>) -> Unit)
}
