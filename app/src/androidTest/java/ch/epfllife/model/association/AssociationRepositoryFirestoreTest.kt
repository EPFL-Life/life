package ch.epfllife.model.association

import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.utils.FirestoreLifeTest
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class AssociationRepositoryFirestoreTest : FirestoreLifeTest() {

  /**
   * We are using the triple A Pattern: Arrange, Act, Assert
   * https://automationpanda.com/2020/07/07/arrange-act-assert-a-pattern-for-writing-good-tests/
   */

  //

  @Test
  fun createAssociation_validAssociation_returnsSuccess() = runTest {
    val assoc1 = ExampleAssociations.association1

    // Act: create association in database
    val createAssociationResult = assocRepository.createAssociation(assoc1)

    // Assert: check association got created successfully and can be retrieved
    assert(createAssociationResult.isSuccess)
    assertEquals(1, getAssociationCount())
    assertEquals(assoc1, assocRepository.getAssociation(assoc1.id))
  }

  @Test
  fun getAssociation_nonExistentAssociation_returnsNull() = runTest {
    val assoc1 = ExampleAssociations.association1

    // Act: get association that doesn't exist
    val getAssociationResult = assocRepository.getAssociation(assoc1.id)

    // Assert: check association doesn't exist
    assertEquals(null, getAssociationResult)
  }

  @Test
  fun getAllAssociation_validAssociations_returnsListOfAssociations() = runTest {
    val assoc1 = ExampleAssociations.association1
    val assoc2 = ExampleAssociations.association2
    val assoc3 = ExampleAssociations.association3

    // Act: create associations in database
    assocRepository.createAssociation(assoc1)
    assocRepository.createAssociation(assoc2)
    assocRepository.createAssociation(assoc3)

    // Assert: 3 associations got added to database
    assertEquals(3, assocRepository.getAllAssociations().size)

    // Act: retrieve all associations
    val allAssociations = assocRepository.getAllAssociations()

    // Assert: retrieved associations are the same as the ones added (in order)
    // we are not checking the order here as its not relevant
    assertEquals(3, allAssociations.size)
    assert(allAssociations.contains(assoc1))
    assert(allAssociations.contains(assoc2))
    assert(allAssociations.contains(assoc3))
  }

  @Test
  fun updateAssociation_validAssociation_returnsSuccess() = runTest {
    val assoc1 = ExampleAssociations.association1

    // Act: add association to database
    assocRepository.createAssociation(assoc1)

    // Assert: added correctly
    assertEquals(1, getAssociationCount())

    // Act: update association
    // new event but change id so we can update assoc1 with assoc2
    val updatedAssociation = ExampleAssociations.association2.copy(id = assoc1.id)

    // Act: update association in database
    val updateAssociationResult = assocRepository.updateAssociation(assoc1.id, updatedAssociation)

    // Assert: check association got updated successfully and can be retrieved
    assert(updateAssociationResult.isSuccess)
    assertEquals(1, getAssociationCount())
    assertEquals(updatedAssociation, assocRepository.getAssociation(assoc1.id))
  }

  @Test
  fun updateAssociation_nonExistentAssociation_returnsFailure() = runTest {
    val assoc1 = ExampleAssociations.association1

    // Act: add association to database
    assocRepository.createAssociation(assoc1)

    // Assert: added correctly
    assertEquals(1, getAssociationCount())

    // Act: try to update association with non-existent ID
    val updatedAssociation = ExampleAssociations.association2.copy(id = "notExistentId")
    val updateAssociationResult =
        assocRepository.updateAssociation("notExistentId", updatedAssociation)

    // Assert: update failed and association was not affected
    assert(updateAssociationResult.isFailure)
    assertEquals(assoc1, assocRepository.getAssociation(assoc1.id))
    assertEquals(1, getAssociationCount())
  }

  @Test
  fun updateAssociation_idMismatch_returnsFailure() = runTest {
    val assoc1 = ExampleAssociations.association1

    // Act: add association to database
    assocRepository.createAssociation(assoc1)

    // Assert: added correctly
    assertEquals(1, getAssociationCount())

    // Act: try to update event but with mismatched id to update (newEvent.id != given id)
    val updatedAssociation = ExampleAssociations.association2.copy(id = "notExistentId")
    val updateAssociationResult = assocRepository.updateAssociation(assoc1.id, updatedAssociation)

    // Assert: update failed and association was not affected
    assert(updateAssociationResult.isFailure)
    assertEquals(assoc1, assocRepository.getAssociation(assoc1.id))
    assertEquals(1, getAssociationCount())
  }

  @Test
  fun deleteAssociation_validAssociation_returnsSuccess() = runTest {
    val assoc1 = ExampleAssociations.association1

    // Act: add association to database
    assocRepository.createAssociation(assoc1)

    // Assert: added correctly
    assertEquals(1, getAssociationCount())

    // Act: delete association from database
    val deleteAssociationResult = assocRepository.deleteAssociation(assoc1.id)

    // Assert: check association got deleted successfully
    assert(deleteAssociationResult.isSuccess)
    assertEquals(0, getAssociationCount())
  }

  @Test
  fun deleteAssociation_nonExistentAssociation_returnsFailure() = runTest {
    val assoc1 = ExampleAssociations.association1

    // Act: add association to database to check database works
    assocRepository.createAssociation(assoc1)

    // Assert: added correctly
    assertEquals(1, getAssociationCount())

    // Act: delete association that doesn't exist
    val deleteAssociationResult = assocRepository.deleteAssociation("nonExistentId")

    // Assert: check association doesn't exist
    assert(deleteAssociationResult.isFailure)
  }

  //  /**
  //   * This test is a bit more complex. We first create the Associations and then link the events
  // to
  //   * these association After that we upload both to the database.
  //   */
  //  @Test
  //  fun getEventsForAssociation_validAssociation_returnsListOfEvents() = runTest {
  //
  //    // Arrange: Link event1,2 to assoc1 and event3 to assoc3
  //
  //    // create associations
  //    val assoc1 = ExampleAssociations.association1
  //    val assoc2 = ExampleAssociations.association2
  //
  //    // event1,event2 belong to assoc1
  //    val event1 = ExampleEvents.event1.copy(association = assoc1)
  //    val event2 = ExampleEvents.event2.copy(association = assoc1)
  //
  //    // event3 belong to assoc2
  //    val event3 = ExampleEvents.event3.copy(association = assoc2)
  //
  //    // Act: add assoc and events to database
  //    assocRepository.createAssociation(assoc1)
  //    assocRepository.createAssociation(assoc2)
  //    eventRepository.createEvent(event1)
  //    eventRepository.createEvent(event2)
  //    eventRepository.createEvent(event3)
  //
  //    // Assert: 2 associations, 3 events got added to database
  //    assertEquals(2, assocRepository.getAllAssociations().size)
  //    assertEquals(3, eventRepository.getAllEvents().size)
  //
  //    // Act: get events for associations
  //    val eventsForAssoc1 = assocRepository.getEventsForAssociation(assoc1.id).getOrThrow()
  //    val eventsForAssoc2 = assocRepository.getEventsForAssociation(assoc2.id).getOrThrow()
  //
  //    // Assert: check events got retrieved correctly
  //    assertEquals(2, eventsForAssoc1.size)
  //    assertEquals(1, eventsForAssoc2.size)
  //    assert(eventsForAssoc1.contains(event1))
  //    assert(eventsForAssoc1.contains(event2))
  //    assert(eventsForAssoc2.contains(event3))
  //  }

  // --------parsing tests with mock----------

  // mock for the firebase class
  @Mock private lateinit var mockDocument: DocumentSnapshot

  // setup mock before tests
  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
  }

  // parsing tests
  @Test
  fun documentToAssociation_validDocument_returnsAssociation() {

    // arrange use example association from ExampleAssociation object
    val association = ExampleAssociations.association1

    // arrange: mock the document with all valid fields
    whenever(mockDocument.id).thenReturn(association.id)
    whenever(mockDocument.getString("name")).thenReturn(association.name)
    whenever(mockDocument.getString("description")).thenReturn(association.description)
    whenever(mockDocument.getString("pictureUrl")).thenReturn(association.pictureUrl)
    whenever(mockDocument.getString("eventCategory")).thenReturn(association.eventCategory.name)
    whenever(mockDocument.getString("about")).thenReturn(association.about)
    whenever(mockDocument.get("socialLinks")).thenReturn(association.socialLinks)

    // action: call the function
    val resultParsed = AssociationRepositoryFirestore.documentToAssociation(mockDocument)

    // assert: the parsed association is the same as the original association
    assertEquals(resultParsed, association)
  }

  @Test
  fun documentToAssociation_invalidDocument_returnsNull() {

    // arrange use example association from ExampleAssociation object
    val association = ExampleAssociations.association1

    // arrange: mock the document with invalid name field
    whenever(mockDocument.id).thenReturn(association.id)
    whenever(mockDocument.getString("name")).thenReturn(null)
    whenever(mockDocument.getString("description")).thenReturn(association.description)
    whenever(mockDocument.getString("pictureUrl")).thenReturn(association.pictureUrl)
    whenever(mockDocument.getString("eventCategory")).thenReturn(association.eventCategory.name)
    whenever(mockDocument.getString("about")).thenReturn(association.about)
    whenever(mockDocument.get("socialLinks")).thenReturn(association.socialLinks)

    // action: call the function
    val resultParsed = AssociationRepositoryFirestore.documentToAssociation(mockDocument)

    // assert: the parsed association is null
    assertEquals(resultParsed, null)
  }
}
