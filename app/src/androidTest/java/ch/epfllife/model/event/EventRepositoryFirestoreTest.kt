package ch.epfllife.model.event

import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class EventRepositoryFirestoreTest {

  // mock for the firebase class
  @Mock private lateinit var mockDocument: DocumentSnapshot

  // setup mock before tests
  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
  }

  /** Helper function to create the nested location map */
  private fun createLocationMap(lat: Double, lon: Double, name: String): Map<String, Any> {
    return mapOf("latitude" to lat, "longitude" to lon, "name" to name)
  }

  @Test
  fun documentToEvent_validDocument_returnsEvent() {

    // arrange: mock the document
    whenever(mockDocument.id).thenReturn("testId")
    whenever(mockDocument.getString("title")).thenReturn("Test Event")
    whenever(mockDocument.getString("description")).thenReturn("This is a test event")
    whenever(mockDocument.get("location")).thenReturn(createLocationMap(1.0, 2.0, "Test Location"))
    whenever(mockDocument.getString("time")).thenReturn("10:00")
    whenever(mockDocument.getString("associationId")).thenReturn("testAssociationId")
    whenever(mockDocument.get("tags")).thenReturn(listOf("tag1", "tag2"))
    whenever(mockDocument.getLong("price")).thenReturn(100L)
    whenever(mockDocument.getString("imageUrl")).thenReturn("testImageUrl")

    // action: call the function
    val event = EventRepositoryFirestore.documentToEvent(mockDocument)

    // assert:
    assert(event != null)
    assertEquals("testId", event?.id)
    assert("Test Event" == event?.title)
    assert("This is a test event" == event?.description)
    assert(1.0 == event?.location?.latitude)
    assert(2.0 == event?.location?.longitude)
    assert("Test Location" == event?.location?.name)
    assert("10:00" == event?.time)
    assert("testAssociationId" == event?.associationId)
    assert(setOf("tag1", "tag2") == event?.tags)
    assert(100u == event?.price)
    assert("testImageUrl" == event?.imageUrl)
  }

  @Test
  fun documentToEvent_missingRequiredField_returnsNull() {

    // arrange: mock the document
    whenever(mockDocument.id).thenReturn("testId")
    whenever(mockDocument.getString("title")).thenReturn("Test Event")
    whenever(mockDocument.getString("description")).thenReturn("This is a test event")
    whenever(mockDocument.get("location")).thenReturn(createLocationMap(3.0, 4.0, "Test Location2"))
    whenever(mockDocument.getString("time")).thenReturn("10:00")
    // associationId is "missing" therefore Mockito return null -> this will result in a parsing
    // error
    whenever(mockDocument.get("tags")).thenReturn(listOf("tag1", "tag2"))
    whenever(mockDocument.getLong("price")).thenReturn(100L)
    whenever(mockDocument.getString("imageUrl")).thenReturn("testImageUrl")

    // action: call the function
    val event = EventRepositoryFirestore.documentToEvent(mockDocument)

    // assert: required field was missing therefore event should be null
    assert(event == null)
  }

  @Test
  fun documentToEvent_malformedId_returnsNull() {

    // arrange: mock the document
    whenever(mockDocument.id).thenReturn(null) // this will result in a parsing error
    whenever(mockDocument.getString("title")).thenReturn("Test Event")
    whenever(mockDocument.getString("description")).thenReturn("This is a test event")
    whenever(mockDocument.getString("time")).thenReturn("10:00")
    whenever(mockDocument.get("location")).thenReturn(createLocationMap(5.0, 6.0, "Test Location3"))
    whenever(mockDocument.getString("associationId")).thenReturn("testAssociationId")
    whenever(mockDocument.get("tags")).thenReturn(listOf("tag1", "tag2"))
    whenever(mockDocument.getLong("price")).thenReturn(-100L)
    whenever(mockDocument.getString("imageUrl")).thenReturn("testImageUrl")

    // action: call the function
    val event = EventRepositoryFirestore.documentToEvent(mockDocument)

    // assert: event should be null
    assert(event == null)
  }
}
