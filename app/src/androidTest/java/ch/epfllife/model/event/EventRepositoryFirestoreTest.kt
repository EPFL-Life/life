// kotlin
package ch.epfllife.model.event

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class EventRepositoryFirestoreTest {

  @Test
  fun documentToEventReturnsEventWhenDocumentIsValid() = runTest {
    // Mock main document
    val doc = mock(DocumentSnapshot::class.java)
    `when`(doc.id).thenReturn("event123")
    `when`(doc.getString("title")).thenReturn("Kotlin Meetup")
    `when`(doc.getString("description")).thenReturn("A meetup about Kotlin")
    `when`(doc.getString("time")).thenReturn("2025-11-10T18:00:00Z")
    `when`(doc.getString("pictureUrl")).thenReturn("https://example.com/pic.png")

    // Location map
    val locMap = mapOf("name" to "Main Hall", "latitude" to 46.5191, "longitude" to 6.5668)
    `when`(doc.get("location")).thenReturn(locMap)

    // Tags list
    `when`(doc.get("tags")).thenReturn(listOf("kotlin", "meetup"))

    // Price as Long (Firestore numeric)
    `when`(doc.getLong("price")).thenReturn(150L)

    // Mock association reference stored in main document
    val assocRef = mock(DocumentReference::class.java)
    `when`(doc.get("association")).thenReturn(assocRef)

    // Mock association snapshot returned by assocRef.get()
    val assocSnap = mock(DocumentSnapshot::class.java)
    `when`(assocSnap.id).thenReturn("assoc1")
    `when`(assocSnap.getString("name")).thenReturn("EPFL Life")
    `when`(assocSnap.getString("description")).thenReturn("Student association")
    `when`(assocSnap.getString("pictureUrl")).thenReturn("https://example.com/assoc.png")
    `when`(assocSnap.getString("eventCategory")).thenReturn(EventCategory.SPORTS.name)

    // Return a completed Task for assocRef.get()
    `when`(assocRef.get()).thenReturn(Tasks.forResult(assocSnap))

    // Call the function under test
    val event = EventRepositoryFirestore.documentToEvent(doc)

    // Assertions
    assertNotNull(event)
    event!!
    assertEquals("event123", event.id)
    assertEquals("Kotlin Meetup", event.title)
    assertEquals("A meetup about Kotlin", event.description)
    assertEquals("2025-11-10T18:00:00Z", event.time)
    assertEquals("https://example.com/pic.png", event.pictureUrl)
    assertEquals(150u, event.price)
    assertEquals("Main Hall", event.location.name)
    assertEquals(46.5191, event.location.latitude, 1e-6)
    assertEquals(6.5668, event.location.longitude, 1e-6)
    assertEquals(setOf("kotlin", "meetup"), event.tags)
    assertNotNull(event.association)
    assertEquals("assoc1", event.association.id)
  }

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
