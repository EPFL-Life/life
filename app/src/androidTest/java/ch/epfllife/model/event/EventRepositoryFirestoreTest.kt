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
  fun createEventReturnsSuccess() = runTest {
    val assoc1 = ExampleAssociations.association1
    // action: create association in database
    assocRepository.createAssociation(assoc1)

    val event = ExampleEvents.event1
    // action: create event
    val res = eventRepository.createEvent(event)

    // asserts: event has been created successfully
    assertTrue(res.isSuccess)
    assertEquals(1, getEventCount())
    assertEquals(event, eventRepository.getEvent(event.id))
  }

  @Test
  fun getEventReturnsException() = runTest {
    // action gets a nonexistent
    val eventId = "nonExistentId"
    try {
      eventRepository.getEvent(eventId)
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
    assocRepository.createAssociation(assoc1)
    assocRepository.createAssociation(assoc2)
    assocRepository.createAssociation(assoc3)

    val e1 = ExampleEvents.event1
    val e2 = ExampleEvents.event2
    val e3 = ExampleEvents.event3

    // action: create the 3 events
    eventRepository.createEvent(e1)
    eventRepository.createEvent(e2)
    eventRepository.createEvent(e3)

    // asserts: check that the event repository contains the 3 events added
    val all = eventRepository.getAllEvents()
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
    assocRepository.createAssociation(assoc1)
    assocRepository.createAssociation(assoc2)

    val original = ExampleEvents.event1
    // action: create event
    eventRepository.createEvent(original)
    // assert: check that the total number of events has increased
    assertEquals(1, getEventCount())

    // action: update event 1 with event 2
    val updated = ExampleEvents.event2.copy(id = original.id)
    val res = eventRepository.updateEvent(original.id, updated)

    // asserts: teh event has been successfully updated
    assertTrue(res.isSuccess)
    assertEquals(1, getEventCount())
    assertEquals(updated, eventRepository.getEvent(original.id))
  }

  @Test
  fun updateIdMismatchReturnsFailure() = runTest {
    val assoc1 = ExampleAssociations.association1
    val assoc2 = ExampleAssociations.association2

    // action: create association in database
    assocRepository.createAssociation(assoc1)
    assocRepository.createAssociation(assoc2)

    val original = ExampleEvents.event1
    // action: create event
    eventRepository.createEvent(original)
    // assert: check that the total numbers of events has increased
    assertEquals(1, getEventCount())

    // action: create a nonexistent event and updated to force failure
    val updated = ExampleEvents.event2.copy(id = "notExistentId")
    val res = eventRepository.updateEvent("notExistentId", updated)

    // assert: the event hasn't been updated
    assertTrue(res.isFailure)
    // assert: original stays unchanged
    assertEquals(original, eventRepository.getEvent(original.id))
    assertEquals(1, getEventCount())
  }

  @Test
  fun deleteEventReturnsSuccess() = runTest {
    val assoc1 = ExampleAssociations.association1
    // action: create association in database
    assocRepository.createAssociation(assoc1)

    val original = ExampleEvents.event1
    // action: create event
    eventRepository.createEvent(original)
    // assert: check that the total numbers of events has increased
    assertEquals(1, getEventCount())
    // action: delete the added event
    val res = eventRepository.deleteEvent(original.id)
    // asserts: the event has been successfully deleted and the total number of events decrease
    assertTrue(res.isSuccess)
    assertEquals(0, getEventCount())
  }

  @Test
  fun deleteNonExistentEventEventReturnsFailure() = runTest {
    val assoc1 = ExampleAssociations.association1
    // action: create association in database
    assocRepository.createAssociation(assoc1)

    val original = ExampleEvents.event1
    // action: create event
    eventRepository.createEvent(original)
    // assert: check that the total numbers of events has increased
    assertEquals(1, getEventCount())
    // action: try to delete an event with an nonexistent id
    val res = eventRepository.deleteEvent("nonExistentId")

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
    whenever(mockDocument.getLong("price")).thenReturn(expected.price.toLong())

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
  fun getEventReturnsEventWhenDocumentExistsAndValid() = runTest {
    val eventId = "event123"
    val expected =
        Event(
            id = eventId,
            title = "Kotlin Meetup",
            description = "A meetup about Kotlin",
            time = "2025-11-10T18:00:00Z",
            pictureUrl = "https://example.com/pic.png",
            price = 150u,
            location = Location(name = "Main Hall", latitude = 46.5191, longitude = 6.5668),
            tags = listOf("kotlin", "meetup"),
            association =
                Association(
                    id = "assoc1",
                    name = "EPFL Life",
                    description = "Student association",
                    pictureUrl = "https://example.com/assoc.png",
                    eventCategory = EventCategory.SPORTS))

    // Mock document snapshot with fields
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.exists()).thenReturn(true)
    `when`(doc.id).thenReturn(expected.id)
    `when`(doc.getString("title")).thenReturn(expected.title)
    `when`(doc.getString("description")).thenReturn(expected.description)
    `when`(doc.getString("time")).thenReturn(expected.time)
    `when`(doc.getString("pictureUrl")).thenReturn(expected.pictureUrl)

    val locMap =
        mapOf(
            "name" to expected.location.name,
            "latitude" to expected.location.latitude,
            "longitude" to expected.location.longitude)
    `when`(doc.get("location")).thenReturn(locMap)
    `when`(doc.get("tags")).thenReturn(expected.tags.toList())
    `when`(doc.getLong("price")).thenReturn(expected.price.toLong())

    // Association reference -> snapshot
    val assocRef = mock(DocumentReference::class.java)
    `when`(doc.get("association")).thenReturn(assocRef)
    val assocSnap = mock(DocumentSnapshot::class.java)
    `when`(assocSnap.id).thenReturn(expected.association.id)
    `when`(assocSnap.get("name")).thenReturn(expected.association.name)
    `when`(assocSnap.getString("description")).thenReturn(expected.association.description)
    `when`(assocSnap.getString("pictureUrl")).thenReturn(expected.association.pictureUrl)
    `when`(assocSnap.getString("eventCategory")).thenReturn(expected.association.eventCategory.name)
    `when`(assocRef.get()).thenReturn(Tasks.forResult(assocSnap))

    // Mock Firestore chain: db.collection(...).document(eventId).get()
    val db = mock(FirebaseFirestore::class.java)
    val col = mock(com.google.firebase.firestore.CollectionReference::class.java)
    val docRef = mock(DocumentReference::class.java)
    `when`(db.collection(FirestoreCollections.EVENTS)).thenReturn(col)
    `when`(col.document(eventId)).thenReturn(docRef)
    `when`(docRef.get()).thenReturn(Tasks.forResult(doc))

    val repo = EventRepositoryFirestore(db)
    val result = repo.getEvent(eventId)

    assertEquals(expected, result)
  }

  @Test
  fun getEventThrowsNoSuchElementWhenDocumentMissing() = runTest {
    val eventId = "missing"
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.exists()).thenReturn(false)

    val db = mock(FirebaseFirestore::class.java)
    val col = mock(com.google.firebase.firestore.CollectionReference::class.java)
    val docRef = mock(DocumentReference::class.java)
    `when`(db.collection(FirestoreCollections.EVENTS)).thenReturn(col)
    `when`(col.document(eventId)).thenReturn(docRef)
    `when`(docRef.get()).thenReturn(Tasks.forResult(doc))

    val repo = EventRepositoryFirestore(db)
    try {
      repo.getEvent(eventId)
      fail("Expected NoSuchElementException when document does not exist")
    } catch (e: NoSuchElementException) {
      // expected
    }
  }

  @Test
  fun getEventThrowsIllegalStateWhenParsingFails() = runTest {
    val eventId = "badparse"
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.exists()).thenReturn(true)
    // Missing required field 'title' -> documentToEvent will return null
    `when`(doc.getString("title")).thenReturn(null)

    val db = mock(FirebaseFirestore::class.java)
    val col = mock(com.google.firebase.firestore.CollectionReference::class.java)
    val docRef = mock(DocumentReference::class.java)
    `when`(db.collection(FirestoreCollections.EVENTS)).thenReturn(col)
    `when`(col.document(eventId)).thenReturn(docRef)
    `when`(docRef.get()).thenReturn(Tasks.forResult(doc))

    val repo = EventRepositoryFirestore(db)
    try {
      repo.getEvent(eventId)
      fail("Expected IllegalStateException when parsing fails")
    } catch (e: IllegalStateException) {
      // expected
    }
  }
}
