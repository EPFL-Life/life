package ch.epfllife.model.event

// Please note the explanation and examples in the EventRepository interface
class EventRepositoryLocal : EventRepository {

  // in-memory storage for events (use this only for testing)
  private val events = mutableListOf<Event>()

  // counter  to allow us to create new ids
  private var counter = 0

  override fun getNewUid(): String {
    return counter++.toString()
  }

  override suspend fun getAllEvents(): List<Event> {
    return events.toList()
  }

  override suspend fun getEvent(eventId: String): Event? {
    return events.find { it.id == eventId }
  }

  override suspend fun createEvent(event: Event): Result<Unit> {

    // ensure uid does NOT exist yet -> prevent duplicate ids
    if (events.any { it.id == event.id }) {
      return Result.failure(IllegalArgumentException("Event with id ${event.id} already exists!"))
    }

    // actually add event
    events.add(event)
    return Result.success(Unit)
  }

  override suspend fun updateEvent(eventId: String, newEvent: Event): Result<Unit> {
    val eventIndex = events.indexOfFirst { it.id == eventId }

    // we return a failed result if the event is not found other wise we update the event and return
    // success
    if (eventIndex == -1) {
      return Result.failure(NoSuchElementException("Event with id $eventId not found!"))
    }

    // update event
    events[eventIndex] = newEvent
    return Result.success(Unit)
  }

  override suspend fun deleteEvent(eventId: String): Result<Unit> {
    val removed = events.removeIf { it.id == eventId }

    return if (removed) {
      Result.success(Unit)
    } else {
      Result.failure(NoSuchElementException("Event with id $eventId not found!"))
    }
  }

  fun setEvents(newEvents: List<Event>) {
    events.clear()
    events.addAll(newEvents)
  }
}
