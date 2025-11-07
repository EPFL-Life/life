// Kotlin
package ch.epfllife.model.event

import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EventRepositoryFirestoreTest {

  // TODO: we will use the test from Daniel (see draft PR #147)
  //  @Test
  //  fun documentToEventReturnsEventWhenDocumentIsValid() = runTest {
  //    // Expected event object
  //    val expected =
  //        Event(
  //            id = "event123",
  //            title = "Kotlin Meetup",
  //            description = "A meetup about Kotlin",
  //            time = "2025-11-10T18:00:00Z",
  //            pictureUrl = "https://example.com/pic.png",
  //            price = Price(150u),
  //            location = Location(name = "Main Hall", latitude = 46.5191, longitude = 6.5668),
  //            tags = setOf("kotlin", "meetup"),
  //            association =
  //                Association(
  //                    id = "assoc1",
  //                    name = "EPFL Life",
  //                    description = "Student association",
  //                    pictureUrl = "https://example.com/assoc.png",
  //                    eventCategory = EventCategory.SPORTS))
  //
  //    // Mock main document using expected fields
  //    val doc = mock(DocumentSnapshot::class.java)
  //    `when`(doc.id).thenReturn(expected.id)
  //    `when`(doc.getString("title")).thenReturn(expected.title)
  //    `when`(doc.getString("description")).thenReturn(expected.description)
  //    `when`(doc.getString("time")).thenReturn(expected.time)
  //    `when`(doc.getString("pictureUrl")).thenReturn(expected.pictureUrl)
  //
  //    // Location map
  //    val locMap =
  //        mapOf(
  //            "name" to expected.location.name,
  //            "latitude" to expected.location.latitude,
  //            "longitude" to expected.location.longitude)
  //    `when`(doc.get("location")).thenReturn(locMap)
  //
  //    // Tags list (Firestore stores lists)
  //    `when`(doc.get("tags")).thenReturn(expected.tags.toList())
  //
  //    // Price as Long (Firestore numeric)
  //    `when`(doc.get("price")).thenReturn(expected.price)
  //
  //    // Mock association reference stored in main document
  //    val assocRef = mock(DocumentReference::class.java)
  //    `when`(doc.get("association")).thenReturn(assocRef)
  //
  //    // Mock association snapshot returned by assocRef.get(), using expected association
  //    val assocSnap = mock(DocumentSnapshot::class.java)
  //    `when`(assocSnap.id).thenReturn(expected.association.id)
  //    `when`(assocSnap.getString("name")).thenReturn(expected.association.name)
  //    // Also stub get("name") in case production code uses get(...) instead of getString(...)
  //    `when`(assocSnap.get("name")).thenReturn(expected.association.name)
  //    `when`(assocSnap.getString("description")).thenReturn(expected.association.description)
  //    `when`(assocSnap.getString("pictureUrl")).thenReturn(expected.association.pictureUrl)
  //
  // `when`(assocSnap.getString("eventCategory")).thenReturn(expected.association.eventCategory.name)
  //
  //    // Return a completed Task for assocRef.get()
  //    `when`(assocRef.get()).thenReturn(Tasks.forResult(assocSnap))
  //
  //    // Call the function under test
  //    val event = EventRepositoryFirestore.documentToEvent(doc)
  //
  //    // Single structural equality assertion (data classes)
  //    assertEquals(expected, event)
  //  }

  @Test
  fun documentToEventReturnsNullWhenRequiredFieldsMissing() = runTest {
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("bad")
    // missing title -> should return null
    `when`(doc.getString("title")).thenReturn(null)

    val result = EventRepositoryFirestore.documentToEvent(doc)
    assertNull(result)
  }
}
