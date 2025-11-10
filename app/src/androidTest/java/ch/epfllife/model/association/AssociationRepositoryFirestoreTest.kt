package ch.epfllife.model.association

import ch.epfllife.example_data.ExampleAssociations
import com.google.firebase.firestore.DocumentSnapshot
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class AssociationRepositoryFirestoreTest {

  // mock for the firebase class
  @Mock private lateinit var mockDocument: DocumentSnapshot

  // setup mock before tests
  @Before
  fun setup() {
    MockitoAnnotations.openMocks(this)
  }

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

    // action: call the function
    val resultParsed = AssociationRepositoryFirestore.documentToAssociation(mockDocument)

    // assert: the parsed association is null
    assertEquals(resultParsed, null)
  }
}
