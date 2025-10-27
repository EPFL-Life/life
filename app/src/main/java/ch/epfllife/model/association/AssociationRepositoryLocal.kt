package ch.epfllife.model.association

import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepository

// TODO this works but is it a good approach?
// we pass the eventRepository because the getEventsForAssociation function retrieves events from it
class AssociationRepositoryLocal(private val eventRepository: EventRepository) :
    AssociationRepository {

  // In-memory storage for associations (use this only for testing)
  private val associations = mutableListOf<Association>()
  private var counter = 0

  override fun getNewUid(): String {
    return counter++.toString()
  }

  // TODO should associationId be String or Int
  override suspend fun getAssociation(associationId: String): Association? {
    return associations.find { it.id == associationId }
  }

  override suspend fun getAllAssociations(): List<Association> {
    return associations.toList()
  }

  override suspend fun createAssociation(association: Association) {
    // TODO should some check (Nullcheck, duplciateId Check) be added?
    associations.add(association)
  }

  override suspend fun updateAssociation(newAssociation: Association) {
    val toBeUpdatedId = newAssociation.id

    // check if id exists
    if (!associations.any() { it.id == toBeUpdatedId }) {
      throw NoSuchElementException(
          "Association with id $toBeUpdatedId not found! Can NOT be updated!")
    }

    // replace the old association with the new one (based on ID)
    associations.removeIf { it.id == toBeUpdatedId }
    associations.add(newAssociation)
  }

  // TODO: same here useString or Int for ID?
  override suspend fun getEventsForAssociation(associationId: String): List<Event> {

    if (!associations.any() { it.id == associationId }) {
      throw NoSuchElementException(
          "Association with id $associationId not found! Can NOT return list of events!")
    }

    // return list of events
    return eventRepository.getAllEvents().filter() { it.associationId == associationId }
  }
}
