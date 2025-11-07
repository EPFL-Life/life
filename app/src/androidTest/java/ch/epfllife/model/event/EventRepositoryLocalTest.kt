package ch.epfllife.model.event

import ch.epfllife.example_data.ExampleEvents
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class EventRepositoryLocalTest {

  // we need this to declare the property for the whole class
  private lateinit var repositoryEvent: EventRepositoryLocal

  @Before
  fun setup() {
    repositoryEvent = EventRepositoryLocal()
  }

  @Test
  fun addEvent_getEvent() = runTest {

    // add example event
    repositoryEvent.createEvent(ExampleEvents.event1)

    // get the event
    val retrievedEvent = repositoryEvent.getEvent(ExampleEvents.event1.id)

    // assert: check event was saved and we retrived the event
    assertEquals(retrievedEvent, ExampleEvents.event1)
    assertEquals(1, repositoryEvent.getAllEvents().size)
  }

  @Test
  fun getAllEvents_returnsAllEvents() = runTest {

    // add 3 example events
    repositoryEvent.createEvent(ExampleEvents.event1)
    repositoryEvent.createEvent(ExampleEvents.event2)
    repositoryEvent.createEvent(ExampleEvents.event3)

    // get all events
    val allEvents = repositoryEvent.getAllEvents()

    // assert: check all events were saved and we can retrive the events
    assertEquals(3, allEvents.size)
    assertEquals(ExampleEvents.event1, allEvents[0])
    assertEquals(ExampleEvents.event2, allEvents[1])
    assertEquals(ExampleEvents.event3, allEvents[2])
  }

  @Test
  fun newUid_generatesUniqueIds() = runTest {

    // create uid for events
    val uid1 = repositoryEvent.getNewUid()
    val uid2 = repositoryEvent.getNewUid()

    // asser: uids are unique
    assert(uid1 != uid2)

    // create events
    val event1 = ExampleEvents.event1.copy(id = uid1)
    val event2 = ExampleEvents.event2.copy(id = uid2)

    // add to repository
    repositoryEvent.createEvent(event1)
    repositoryEvent.createEvent(event2)

    // assert: correctly saved and uid as expected
    assertEquals(2, repositoryEvent.getAllEvents().size)
    assertEquals(event1, repositoryEvent.getEvent(uid1))
    assertEquals(event2, repositoryEvent.getEvent(uid2))
  }

  @Test
  fun updateEvent_updatesEvent() = runTest {

    // add event to repository
    repositoryEvent.createEvent(ExampleEvents.event1)

    // update event
    val updatedEvent = ExampleEvents.event1.copy(title = "Updated Title")

    // update event in repo
    repositoryEvent.updateEvent(ExampleEvents.event1.id, updatedEvent)

    // assert: event was updated
    assertEquals(updatedEvent, repositoryEvent.getEvent(ExampleEvents.event1.id))
  }

  @Test
  fun deleteEvent_removesEvent() = runTest {

    // add event to repository
    repositoryEvent.createEvent(ExampleEvents.event1)

    // assert event was added
    assertEquals(1, repositoryEvent.getAllEvents().size)

    // delete event
    repositoryEvent.deleteEvent(ExampleEvents.event1.id)

    // assert event was deleted
    assertEquals(0, repositoryEvent.getAllEvents().size)
  }

  @Test
  fun deleteEvent_returnsFailureForNonExistentEvent() = runTest {

    // assert no event in present
    assertEquals(0, repositoryEvent.getAllEvents().size)

    // attempt to delete event
    val result = repositoryEvent.deleteEvent("nonExistentEventId")

    // assert failure
    assert(!result.isSuccess)
    assert(result.isFailure) // redundant but just to showcase

    // assert no event was deleted
    assertEquals(0, repositoryEvent.getAllEvents().size)
  }

  @Test
  fun updateEvent_returnsFailureForNonExistentEvent() = runTest {

    // add event to repository
    repositoryEvent.createEvent(ExampleEvents.event1)

    // assert event was added
    assertEquals(1, repositoryEvent.getAllEvents().size)

    // attempt to update event
    val result = repositoryEvent.updateEvent("nonExistentEventId", ExampleEvents.event2)

    // assert failure
    assert(!result.isSuccess)

    // assert event was not updated
    assertEquals(1, repositoryEvent.getAllEvents().size)
  }

  @Test
  fun createEvent_withExistingId_returnsFailure() = runTest {
    // Arrange: Add an event to the repository first
    val originalEvent = ExampleEvents.event1
    repositoryEvent.createEvent(originalEvent)

    // Ensure the initial state is correct
    assertEquals(1, repositoryEvent.getAllEvents().size)

    // Act: Attempt to create another event with the SAME ID
    val duplicateEvent = ExampleEvents.event2.copy(id = originalEvent.id)
    val result = repositoryEvent.createEvent(duplicateEvent)

    // Assert: The operation should fail
    assert(result.isFailure) { "Creating an event with a duplicate ID should fail." }

    // Assert: The repository should NOT have been modified
    val eventsInRepo = repositoryEvent.getAllEvents()
    assertEquals(1, eventsInRepo.size) // The repository size should not change on a failed creation
    assertEquals(originalEvent, eventsInRepo[0]) // The original event should remain unchanged.
  }
}
