// Kotlin
package ch.epfllife.model.event

import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.utils.FirestoreLifeTest
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class EventRepositoryFirestoreTest : FirestoreLifeTest() {

  @Test
  fun uploadAndRetrieveEventWorksCorrectly() = runTest {
    // Arrange: create an association first (required for event validity)
    val assoc = ExampleAssociations.association1
    db.assocRepo.createAssociation(assoc)

    // Arrange: prepare example event linked to that association
    val event = ExampleEvents.event1

    // Act: create event in Firestore emulator
    val createResult = db.eventRepo.createEvent(event)

    // Assert: creation succeeded
    assertTrue(createResult.isSuccess)
    assertEquals(1, getEventCount())

    // Act: retrieve event by ID
    val retrieved = db.eventRepo.getEvent(event.id)

    // Assert: event was retrieved and matches the original
    assertNotNull("Retrieved event should not be null", retrieved)
    assertEquals(event, retrieved)
  }

  @Test
  fun createEventReturnsSuccess() = runTest {
    val assoc1 = ExampleAssociations.association1
    // action: create association in database
    db.assocRepo.createAssociation(assoc1)

    val event = ExampleEvents.event1
    // action: create event
    val res = db.eventRepo.createEvent(event)

    // asserts: event has been created successfully
    assertTrue(res.isSuccess)
    assertEquals(1, getEventCount())
    assertEquals(event, db.eventRepo.getEvent(event.id))
  }

  @Test
  fun getEventReturnsException() = runTest {
    // action gets a nonexistent
    val eventId = "nonExistentId"
    try {
      db.eventRepo.getEvent(eventId)
      fail("Expected NoSuchElementException when document does not exist")
    } catch (e: NoSuchElementException) {
      // expected
    }
  }

  @Test
  fun getAllEventsReturnsListOfEvents() = runTest {
    val assoc1 = ExampleAssociations.association1
    val assoc2 = ExampleAssociations.association2
    val assoc3 = ExampleAssociations.association3

    // action: create association in database
    db.assocRepo.createAssociation(assoc1)
    db.assocRepo.createAssociation(assoc2)
    db.assocRepo.createAssociation(assoc3)

    val e1 = ExampleEvents.event1
    val e2 = ExampleEvents.event2
    val e3 = ExampleEvents.event3

    // action: create the 3 events
    db.eventRepo.createEvent(e1)
    db.eventRepo.createEvent(e2)
    db.eventRepo.createEvent(e3)

    // asserts: check that the event repository contains the 3 events added
    val all = db.eventRepo.getAllEvents()
    assertEquals(3, all.size)
    assert(all.contains(e1))
    assert(all.contains(e2))
    assert(all.contains(e3))
  }

  @Test
  fun updateEventReturnsSuccess() = runTest {
    val assoc1 = ExampleAssociations.association1
    val assoc2 = ExampleAssociations.association2

    // action: create association in database
    db.assocRepo.createAssociation(assoc1)
    db.assocRepo.createAssociation(assoc2)

    val original = ExampleEvents.event1
    // action: create event
    db.eventRepo.createEvent(original)
    // assert: check that the total number of events has increased
    assertEquals(1, getEventCount())

    // action: update event 1 with event 2
    val updated = ExampleEvents.event2.copy(id = original.id)
    val res = db.eventRepo.updateEvent(original.id, updated)

    // asserts: teh event has been successfully updated
    assertTrue(res.isSuccess)
    assertEquals(1, getEventCount())
    assertEquals(updated, db.eventRepo.getEvent(original.id))
  }

  @Test
  fun updateIdMismatchReturnsFailure() = runTest {
    val assoc1 = ExampleAssociations.association1
    val assoc2 = ExampleAssociations.association2

    // action: create association in database
    db.assocRepo.createAssociation(assoc1)
    db.assocRepo.createAssociation(assoc2)

    val original = ExampleEvents.event1
    // action: create event
    db.eventRepo.createEvent(original)
    // assert: check that the total numbers of events has increased
    assertEquals(1, getEventCount())

    // action: create a nonexistent event and updated to force failure
    val updated = ExampleEvents.event2.copy(id = "notExistentId")
    val res = db.eventRepo.updateEvent("notExistentId", updated)

    // assert: the event hasn't been updated
    assertTrue(res.isFailure)
    // assert: original stays unchanged
    assertEquals(original, db.eventRepo.getEvent(original.id))
    assertEquals(1, getEventCount())
  }

  @Test
  fun deleteEventReturnsSuccess() = runTest {
    val assoc1 = ExampleAssociations.association1
    // action: create association in database
    db.assocRepo.createAssociation(assoc1)

    val original = ExampleEvents.event1
    // action: create event
    db.eventRepo.createEvent(original)
    // assert: check that the total numbers of events has increased
    assertEquals(1, getEventCount())
    // action: delete the added event
    val res = db.eventRepo.deleteEvent(original.id)
    // asserts: the event has been successfully deleted and the total number of events decrease
    assertTrue(res.isSuccess)
    assertEquals(0, getEventCount())
  }

  @Test
  fun deleteNonExistentEventEventReturnsFailure() = runTest {
    val assoc1 = ExampleAssociations.association1
    // action: create association in database
    db.assocRepo.createAssociation(assoc1)

    val original = ExampleEvents.event1
    // action: create event
    db.eventRepo.createEvent(original)
    // assert: check that the total numbers of events has increased
    assertEquals(1, getEventCount())
    // action: try to delete an event with an nonexistent id
    val res = db.eventRepo.deleteEvent("nonExistentId")

    // assert: the event can't be deleted
    assertTrue(res.isFailure)
    // assert: database unchanged
    assertEquals(1, getEventCount())
  }

  @Mock private lateinit var mockDocument: DocumentSnapshot

  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
  }

  @Test
  fun documentToEventReturnsEventWhenDocumentIsValid() = runTest {
    // Expected event object
    val expected = ExampleEvents.event1
    // Mock main document using expected fields
    whenever(mockDocument.id).thenReturn(expected.id)
    whenever(mockDocument.getString("title")).thenReturn(expected.title)
    whenever(mockDocument.getString("description")).thenReturn(expected.description)
    whenever(mockDocument.getString("time")).thenReturn(expected.time)
    whenever(mockDocument.getString("pictureUrl")).thenReturn(expected.pictureUrl)

    // Location map
    val locMap =
        mapOf(
            "name" to expected.location.name,
            "latitude" to expected.location.latitude,
            "longitude" to expected.location.longitude)
    whenever(mockDocument.get("location")).thenReturn(locMap)

    // Tags list (Firestore stores lists)
    whenever(mockDocument.get("tags")).thenReturn(expected.tags.toList())

    // Price as Long (Firestore numeric)
    whenever(mockDocument.getLong("price")).thenReturn(expected.price.cents.toLong())

    // Mock association reference stored in main document
    val assocRef = mock(DocumentReference::class.java)
    whenever(mockDocument.get("association")).thenReturn(assocRef)

    // Mock association snapshot returned by assocRef.get(), using expected association
    val assocSnap = mock(DocumentSnapshot::class.java)
    whenever(assocSnap.id).thenReturn(expected.association.id)
    whenever(assocSnap.getString("name")).thenReturn(expected.association.name)
    // Also stub get("name") in case production code uses get(...) instead of getString(...)
    whenever(assocSnap.get("name")).thenReturn(expected.association.name)
    whenever(assocSnap.getString("description")).thenReturn(expected.association.description)
    whenever(assocSnap.getString("pictureUrl")).thenReturn(expected.association.pictureUrl)
    whenever(assocSnap.getString("logoUrl")).thenReturn(expected.association.logoUrl)
    whenever(assocSnap.getString("eventCategory"))
        .thenReturn(expected.association.eventCategory.name)
    whenever(assocSnap.getString("about")).thenReturn(expected.association.about)
    whenever(assocSnap.get("socialLinks")).thenReturn(expected.association.socialLinks)
    // Return a completed Task for assocRef.get()
    whenever(assocRef.get()).thenReturn(Tasks.forResult(assocSnap))

    // Call the function under test
    val event = EventRepositoryFirestore.documentToEvent(mockDocument)

    // Single structural equality assertion (data classes)
    assertEquals(expected, event)
  }

  @Test
  fun documentToEventReturnsNullWhenRequiredFieldsMissing() = runTest {
    whenever(mockDocument.id).thenReturn("bad")
    // missing title -> should return null
    whenever(mockDocument.getString("title")).thenReturn(null)

    val result = EventRepositoryFirestore.documentToEvent(mockDocument)
    assertNull(result)
  }

  @Test
  fun listenToCreateEvent() = runTest {
    var eventsList = emptyList<Event>()
    db.eventRepo.listenAll { events -> eventsList = events }
    db.eventRepo.createEvent(ExampleEvents.event1)

    kotlinx.coroutines.delay(100)

    assertEquals(listOf(ExampleEvents.event1), eventsList)
  }

  @Test
  fun listenToUpdateEvent() = runTest {
    db.eventRepo.createEvent(ExampleEvents.event1)

    var eventsList = emptyList<Event>()
    db.eventRepo.listenAll { events -> eventsList = events }

    val updatedEvent = ExampleEvents.event1.copy(title = "Updated Title")
    db.eventRepo.updateEvent(ExampleEvents.event1.id, updatedEvent)

    kotlinx.coroutines.delay(100)

    assertEquals(listOf(updatedEvent), eventsList)
  }

  @Test
  fun listenToDeleteEvent() = runTest {
    var eventsList = emptyList<Event>()
    db.eventRepo.listenAll { events -> eventsList = events }
    db.eventRepo.createEvent(ExampleEvents.event1)
    assertEquals(listOf(ExampleEvents.event1), eventsList)

    db.eventRepo.deleteEvent(ExampleEvents.event1.id)

    kotlinx.coroutines.delay(100)

    assertEquals(emptyList<Event>(), eventsList)
  }
}
