package ch.epfllife.model.association

import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepository

// We pass the eventRepository because the getEventsForAssociation function retrieves events from it
class AssociationRepositoryLocal(private val eventRepository: EventRepository) :
    AssociationRepository {

  // In-memory storage for associations (use this only for testing)
  private val associations = mutableListOf<Association>()
  private var counter = 0

  private val associationsListeners = mutableListOf<((List<Association>) -> Unit)>()
  private val associationListeners = mutableMapOf<String, ((Association) -> Unit)>()

  private fun notifyListeners() {
    associationsListeners.forEach { it(associations.toList()) }
    associations.forEach { assoc ->
      associationListeners.forEach { id, listener -> if (assoc.id == id) listener(assoc) }
    }
  }

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
    notifyListeners()
    return Result.success(Unit)
  }

  override suspend fun deleteAssociation(associationId: String): Result<Unit> {
    // remove association
    val removedAssociation = associations.removeIf { it.id == associationId }

    // check if association exists and return failure/success
    return if (removedAssociation) {
      notifyListeners()
      Result.success(Unit)
    } else {
      Result.failure(NoSuchElementException("Association not found with ID: $associationId"))
    }
  }

  override suspend fun updateAssociation(
      associationId: String,
      newAssociation: Association,
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
    notifyListeners()
    return Result.success(Unit)
  }

  // In your Repository implementation
  override suspend fun getEventsForAssociation(associationId: String): Result<List<Event>> {
    return Result.runCatching {
      // check association exists
      if (!associations.any { it.id == associationId }) {
        throw NoSuchElementException("Association with id $associationId not found!")
      }

      // inefficient but works for testing
      eventRepository.getAllEvents().filter() { it.association.id == associationId }
    }
  }

  override fun listenAll(onChange: (List<Association>) -> Unit) {
    associationsListeners.add(onChange)
    // send initial data
    onChange(associations.toList())
  }

  override fun listen(associationId: String, onChange: (Association) -> Unit) {
    associationListeners.put(associationId, onChange)
    // send initial data
    associations.find { it.id == associationId }?.let(onChange)
  }
}
