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
  suspend fun getEvent(eventId: String): Event

  /**
   * Adds a new Event item to the repository.
   *
   * @param event The Event item to add.
   */
  suspend fun addEvent(event: Event)

  /**
   * Edits an existing Event item in the repository.
   *
   * @param eventId The unique identifier of the Event item to edit.
   * @param newEvent The new value for the Event item.
   */
  suspend fun updateEvent(eventId: String, newEvent: Event)

  /**
   * Deletes an Event item from the repository.
   *
   * @param eventId The unique identifier of the Event item to delete.
   */
  suspend fun deleteEvent(eventId: String)
}
