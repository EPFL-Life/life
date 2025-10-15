package ch.epfllife.model.event

import com.google.firebase.firestore.FirebaseFirestore

class EventRepositoryFirestore(private val db: FirebaseFirestore) : EventRepository {

  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun getAllEvents(): List<Event> {
    TODO("Not yet implemented")
  }

  override suspend fun getEvent(eventId: String): Event {
    TODO("Not yet implemented")
  }

  override suspend fun addEvent(event: Event) {
    TODO("Not yet implemented")
  }

  override suspend fun updateEvent(eventId: String, newEvent: Event) {
    TODO("Not yet implemented")
  }

  override suspend fun deleteEvent(eventId: String) {
    TODO("Not yet implemented")
  }
}
