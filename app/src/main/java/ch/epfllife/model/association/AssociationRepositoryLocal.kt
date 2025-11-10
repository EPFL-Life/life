package ch.epfllife.model.association

import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepository

// We pass the eventRepository because the getEventsForAssociation function retrieves events from it
class AssociationRepositoryLocal(private val eventRepository: EventRepository) :
    AssociationRepository {

  // In-memory storage for associations (use this only for testing)
  private val associations = mutableListOf<Association>()
  private var counter = 0

  override fun getNewUid(): String {
    return counter++.toString()
  }

  override suspend fun getAssociation(associationId: String): Association? {
    return associations.find { it.id == associationId }
  }

  override suspend fun getAllAssociations(): List<Association> {
    return associations.toList()
  }

  override suspend fun createAssociation(association: Association): Result<Unit> {

    // check if association with associationId already exists
    if (associations.any { it.id == association.id }) {
      return Result.failure(
          IllegalArgumentException("Association with id ${association.id} already exists!"))
    }

    // add new association
    associations.add(association)
    return Result.success(Unit)
  }

  override suspend fun deleteAssociation(associationId: String): Result<Unit> {
    // remove association
    val removedAssociation = associations.removeIf { it.id == associationId }

    // check if association exists and return failure/success
    return if (removedAssociation) {
      Result.success(Unit)
    } else {
      Result.failure(NoSuchElementException("Association not found with ID: $associationId"))
    }
  }

  override suspend fun updateAssociation(
      associationId: String,
      newAssociation: Association
  ): Result<Unit> {

    // 1. Find the index of the association to update
    val existingIndex = associations.indexOfFirst { it.id == associationId }

    // 2. Check if the association to update even exists
    if (existingIndex == -1) {
      return Result.failure(
          NoSuchElementException("Cannot update. Association not found with ID: $associationId"))
    }

    // 3. Check if the object's ID matches the parameter ID
    if (associationId != newAssociation.id) {
      return Result.failure(
          IllegalArgumentException(
              "Association ID mismatch. Parameter was $associationId but object ID was ${newAssociation.id}"))
    }

    // 4. Perform the update by replacing the item at its index
    associations[existingIndex] = newAssociation
    return Result.success(Unit)
  }

  override suspend fun getEventsForAssociation(associationId: String): List<Event> {

    if (!associations.any() { it.id == associationId }) {
      // I dont want to throw a exception here but i had no better idea of implementing this because
      // we dont return a Result<T> where we could pass an Exception
      throw NoSuchElementException(
          "Association with id $associationId not found! Can NOT return list of events!")
    }

    // return list of events
    return eventRepository.getAllEvents().filter() { it.association.id == associationId }
  }
}
