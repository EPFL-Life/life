package ch.epfllife.model.association

import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.event.EventRepositoryLocal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AssociationRepositoryLocalTest {

  // --- Test Data ---

  // Create sample data that matches your Association data class
  private val assoc1 =
      Association(
          id = "assoc-1",
          name = "Art Club",
          description = "Club for artists",
          pictureUrl = "http://example.com/art.png",
          eventCategory = EventCategory.ACADEMIC)

  private val assoc2 =
      Association(
          id = "assoc-2",
          name = "Music Society",
          description = "For music lovers",
          pictureUrl = null,
          eventCategory = EventCategory.TECH)

  private lateinit var repositoryAssociation: AssociationRepositoryLocal
  private lateinit var repositoryEvent: EventRepositoryLocal

  @Before
  fun setup() {
    repositoryEvent = EventRepositoryLocal()
    repositoryAssociation = AssociationRepositoryLocal(repositoryEvent)
  }

  // --- Test Cases ---

  @Test
  fun createAssociation_savesAssociation() = runTest {
    assertTrue(repositoryAssociation.getAllAssociations().isEmpty())

    repositoryAssociation.createAssociation(assoc1)

    val associations = repositoryAssociation.getAllAssociations()
    assertEquals(associations.size, 1)
    assertEquals(associations[0], assoc1)
  }

  @Test
  fun createAssociation_generatesUniqueId() = runTest {
    assertTrue(repositoryAssociation.getAllAssociations().isEmpty())

    repositoryAssociation.createAssociation(assoc1)
    repositoryAssociation.createAssociation(assoc2)

    val associations = repositoryAssociation.getAllAssociations()
    assertNotEquals(associations[0].id, associations[1].id)
  }

  @Test
  fun updateAssociation_updatesAssociation() = runTest {
    assertTrue(repositoryAssociation.getAllAssociations().isEmpty())

    repositoryAssociation.createAssociation(assoc1)
    val updatedAssociation = assoc1.copy(name = "Updated Art Club")

    assertEquals(assoc1, repositoryAssociation.getAssociation(assoc1.id))

    repositoryAssociation.updateAssociation(updatedAssociation)

    assertEquals(updatedAssociation, repositoryAssociation.getAssociation(assoc1.id))
    assertEquals(updatedAssociation.name, repositoryAssociation.getAssociation(assoc1.id)?.name)
  }

  @Test
  fun getEventsForAssociation_returnsEventsForAssociation() =
      runTest {
        // TODO: implement (currently logic for EventRepository is missing)
      }

  @Test
  fun getEventsForAssociation_returnsEmptyListForNonExistentAssociation() =
      runTest {
        // TODO: implement (currently logic for EventRepository is missing)
        //
        //    val events = repositoryAssociation.getEventsForAssociation("non-existent-id")
        //    assertNotNull(events)
        //    assertTrue(events.isEmpty())
      }
}
