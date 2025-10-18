package ch.epfllife.model.association

import ch.epfllife.model.event.Event

class AssociationRepositoryLocal : AssociationRepository {

  override suspend fun getAssociation(associationId: String): Association? {
    TODO("Not yet implemented")
  }

  override suspend fun getAllAssociations(): List<Association> {
    TODO("Not yet implemented")
  }

  override suspend fun createAssociation(association: Association) {
    TODO("Not yet implemented")
  }

  override suspend fun updateAssociation(associationId: String, newAssociation: Association) {
    TODO("Not yet implemented")
  }

  override suspend fun getEventsForAssociation(associationId: String): List<Event> {
    TODO("Not yet implemented")
  }
}
