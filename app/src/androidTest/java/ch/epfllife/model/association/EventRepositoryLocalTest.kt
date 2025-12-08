package ch.epfllife.model.association

import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.event.EventRepositoryLocal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepositoryLocalTest {

  // testing class
  private lateinit var repositoryAssociation: AssociationRepositoryLocal

  // dependency needed for testing
  private lateinit var repositoryEvent: EventRepositoryLocal

  private fun getAssocList(scope: TestScope): List<Association> {
    val associationsList = mutableListOf<Association>()
    repositoryAssociation.listenAll(scope) { associations ->
      associationsList.clear()
      associationsList.addAll(associations)
    }
    return associationsList
  }

  private suspend fun TestScope.createAssoc(): Pair<Association, List<Association>> {
    val assoc = ExampleAssociations.association1
    val assocList = getAssocList(this)
    repositoryAssociation.createAssociation(assoc)
    advanceUntilIdle()
    assertEquals(listOf(assoc), assocList)
    return Pair(assoc, assocList)
  }

  @Before
  fun setup() {
    repositoryEvent = EventRepositoryLocal()
    repositoryAssociation = AssociationRepositoryLocal(eventRepository = repositoryEvent)
  }

  @Test
  fun addAssociation_getAssociation() = runTest {
    // Arrange: check repository is empty
    assertEquals(0, repositoryAssociation.getAllAssociations().size)

    // Action: add example association
    val response = repositoryAssociation.createAssociation(ExampleAssociations.association1)

    // Assert: Result is success
    assert(response.isSuccess)

    // Assert: check association was saved and correctly saved/retrived
    assertEquals(1, repositoryAssociation.getAllAssociations().size)
    assertEquals(
        ExampleAssociations.association1,
        repositoryAssociation.getAssociation(ExampleAssociations.association1.id),
    )
  }

  @Test
  fun getAllAssociations_returnsAllAssociations() = runTest {

    // Arrange: check repository is empty
    assertEquals(0, repositoryAssociation.getAllAssociations().size)

    // Action: add all 3 example Associatnions
    repositoryAssociation.createAssociation(ExampleAssociations.association1)
    repositoryAssociation.createAssociation(ExampleAssociations.association2)
    repositoryAssociation.createAssociation(ExampleAssociations.association3)

    // Assert: check all associations were saved and correctly saved/retrived
    assertEquals(3, repositoryAssociation.getAllAssociations().size)
    assertEquals(
        ExampleAssociations.association1,
        repositoryAssociation.getAssociation(ExampleAssociations.association1.id),
    )
    assertEquals(
        ExampleAssociations.association2,
        repositoryAssociation.getAssociation(ExampleAssociations.association2.id),
    )
    assertEquals(
        ExampleAssociations.association3,
        repositoryAssociation.getAssociation(ExampleAssociations.association3.id),
    )
  }

  @Test
  fun newUid_generatesUniqueIds() = runTest {

    // Action: generate new Uids
    val uid1 = repositoryAssociation.getNewUid()
    val uid2 = repositoryAssociation.getNewUid()

    // Assert: check uids are unique
    assert(uid1 != uid2)
  }

  @Test
  fun updateAssociation_updatesAssociation() = runTest {

    // Arrange: check repository is empty
    assertEquals(0, repositoryAssociation.getAllAssociations().size)

    // Action add example association
    repositoryAssociation.createAssociation(ExampleAssociations.association1)

    // Assert: check association was saved
    assertEquals(1, repositoryAssociation.getAllAssociations().size)
    assertEquals(
        ExampleAssociations.association1,
        repositoryAssociation.getAssociation(ExampleAssociations.association1.id),
    )

    // Arrange: change id of association 2 to same as association 1
    val newAssociation =
        ExampleAssociations.association2.copy(id = ExampleAssociations.association1.id)

    // Action: update association 1 with association 2 using associations 1 id
    val response =
        repositoryAssociation.updateAssociation(ExampleAssociations.association1.id, newAssociation)

    // Assert: successful operation
    assert(response.isSuccess)

    // Assert: check association was updated (we compare only the name bcs id is not the same)
    assertEquals(
        ExampleAssociations.association2.name,
        repositoryAssociation.getAssociation(ExampleAssociations.association1.id)?.name,
    )
  }

  @Test
  fun deleteAssociation_removesAssociation() = runTest {

    // Arrange: check repository is empty
    assertEquals(0, repositoryAssociation.getAllAssociations().size)

    // Action add example associations
    repositoryAssociation.createAssociation(ExampleAssociations.association1)
    repositoryAssociation.createAssociation(ExampleAssociations.association2)

    // Assert: check associations were saved
    assertEquals(2, repositoryAssociation.getAllAssociations().size)

    // Action: remove association 1
    repositoryAssociation.deleteAssociation(ExampleAssociations.association1.id)

    // Assert: check association was removed
    assertEquals(1, repositoryAssociation.getAllAssociations().size)
    assertEquals(
        ExampleAssociations.association2,
        repositoryAssociation.getAssociation(ExampleAssociations.association2.id),
    )
  }

  @Test
  fun getEventsForAssociation_returnsCorrectEvents() = runTest {
    // Arrange:
    // 1. Create associations
    val assoc1 = ExampleAssociations.association1
    val assoc2 = ExampleAssociations.association2
    repositoryAssociation.createAssociation(assoc1)
    repositoryAssociation.createAssociation(assoc2)

    // 2. Create events linked to those associations
    val event1ForA1 = ExampleEvents.event1.copy(association = assoc1)
    val event2ForA1 = ExampleEvents.event2.copy(association = assoc1)
    val event3ForA2 = ExampleEvents.event3.copy(association = assoc2)

    // 3. Add events to the *event repository* (the dependency)
    repositoryEvent.createEvent(event1ForA1)
    repositoryEvent.createEvent(event2ForA1)
    repositoryEvent.createEvent(event3ForA2)

    // Act: Get events for assoc1
    val eventsForA1 = repositoryAssociation.getEventsForAssociation(assoc1.id).getOrThrow()

    // Assert: Check only assoc1's events are returned
    assertEquals(2, eventsForA1.size)
    assert(eventsForA1.contains(event1ForA1))
    assert(eventsForA1.contains(event2ForA1))

    // Act: Get events for assoc2
    val eventsForA2 = repositoryAssociation.getEventsForAssociation(assoc2.id).getOrThrow()

    // Assert: Check only assoc2's events are returned
    assertEquals(1, eventsForA2.size)
    assertEquals(event3ForA2, eventsForA2[0])
  }

  @Test
  fun getEventsForAssociation_returnsEmptyListForAssociationWithNoEvents() = runTest {
    // Arrange:
    // 1. Create associations
    val assoc1 = ExampleAssociations.association1 // This assoc will have no events
    val assoc2 = ExampleAssociations.association2
    repositoryAssociation.createAssociation(assoc1)
    repositoryAssociation.createAssociation(assoc2)

    // 2. Create an event ONLY for assoc2
    val eventForA2 = ExampleEvents.event3.copy(association = assoc2)
    repositoryEvent.createEvent(eventForA2)

    // Act: Get events for assoc1 (which has no events)
    val eventsForA1 = repositoryAssociation.getEventsForAssociation(assoc1.id).getOrThrow()

    // Assert: Check that the list is empty
    assertEquals(0, eventsForA1.size)
  }

  @Test
  fun createAssociation_duplicateId_returnsFailure() = runTest {

    // Arrange: check repository is empty
    assertEquals(0, repositoryAssociation.getAllAssociations().size)

    // Action: add example association
    val response = repositoryAssociation.createAssociation(ExampleAssociations.association1)

    // Assert: Result is success
    assert(response.isSuccess)

    // Action: try to add association with same id
    val response2 = repositoryAssociation.createAssociation(ExampleAssociations.association1)

    // Assert: Result is failure
    assert(response2.isFailure)
  }

  @Test
  fun deleteAssociation_nonExistentAssociation_returnsFailure() = runTest {

    // Arrange: check repository is empty
    assertEquals(0, repositoryAssociation.getAllAssociations().size)

    // Action: try to delete association
    val response = repositoryAssociation.deleteAssociation("nonExistentAssociationId")

    // Assert: action was a failure
    assert(response.isFailure)
  }

  @Test
  fun updateAssociation_nonExistentAssociationWithId_returnsFailure() = runTest {
    // Arrange: check repository is empty
    assertEquals(0, repositoryAssociation.getAllAssociations().size)

    // Action: try to update association
    val response =
        repositoryAssociation.updateAssociation(
            "nonExistentAssociationId",
            ExampleAssociations.association1,
        )

    // Assert: action was a failure
    assert(response.isFailure)
  }

  @Test
  fun updateAssociation_associationIdMismatch_returnsFailure() = runTest {

    // Arrange: check repository is empty
    assertEquals(0, repositoryAssociation.getAllAssociations().size)

    // Action add example association
    repositoryAssociation.createAssociation(ExampleAssociations.association1)

    // Assert: check association was saved
    assertEquals(1, repositoryAssociation.getAllAssociations().size)

    // Action: try to update association with mismatched/different id
    val response =
        repositoryAssociation.updateAssociation(
            ExampleAssociations.association1.id,
            ExampleAssociations.association2,
        )

    // Assert: failure
    assert(response.isFailure)
  }

  @Test fun listenToCreateAssoc() = runTest { createAssoc() }

  @Test
  fun listenToUpdateAssoc() = runTest {
    val (assoc, assocList) = createAssoc()

    val updatedEvent = assoc.copy(name = "Updated Name")
    repositoryAssociation.updateAssociation(assoc.id, updatedEvent)

    advanceUntilIdle()
    assertEquals(listOf(updatedEvent), assocList)
  }

  @Test
  fun listenToDeleteAssoc() = runTest {
    val (assoc, assocList) = createAssoc()

    repositoryAssociation.deleteAssociation(assoc.id)

    advanceUntilIdle()
    assertEquals(emptyList<Association>(), assocList)
  }
}
