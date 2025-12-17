package ch.epfllife.model.event

import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Please note the explanation and examples in the EventRepository interface
class EventRepositoryLocal : EventRepository {

  // in-memory storage for events (use this only for testing)
  private val events = mutableListOf<Event>()

  // counter  to allow us to create new ids
  private var counter = 0

  private val eventsListeners = mutableListOf<((List<Event>) -> Unit)>()

  private fun notifyListeners() {
    eventsListeners.forEach { it(events.toList()) }
  }

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
    notifyListeners()
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
    notifyListeners()
    return Result.success(Unit)
  }

  override suspend fun deleteEvent(eventId: String): Result<Unit> {
    val removed = events.removeIf { it.id == eventId }

    return if (removed) {
      notifyListeners()
      Result.success(Unit)
    } else {
      Result.failure(NoSuchElementException("Event with id $eventId not found!"))
    }
  }

  override fun listenAll(scope: CoroutineScope, onChange: suspend (List<Event>) -> Unit) {
    eventsListeners.add { scope.launch { onChange(it) } }
    // send initial data
    scope.launch { onChange(events.toList()) }
  }

  fun seedEvents(newEvents: List<Event>) {
    events.clear()
    events.addAll(newEvents)
  }

  override suspend fun uploadEventImage(eventId: String, imageUri: Uri): Result<String> {
    // stub logic
    return Result.success("https://example.com/mock_event_image.jpg")
  }
}
