package ch.epfllife.model.event

import android.util.Log
import ch.epfllife.model.association.Association
import ch.epfllife.model.association.AssociationRepositoryFirestore
import ch.epfllife.model.firestore.FirestoreCollections
import ch.epfllife.model.map.Location
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class EventRepositoryFirestore(private val db: FirebaseFirestore) : EventRepository {

  override fun getNewUid(): String {
    return db.collection(FirestoreCollections.EVENTS).document().id
  }

  override suspend fun getAllEvents(): List<Event> = coroutineScope {
    val task = db.collection(FirestoreCollections.EVENTS).get().await()
    task.documents.map { doc -> async { documentToEvent(doc) } }.mapNotNull { it.await() }
  }

  override suspend fun getEvent(eventId: String): Event {
    return try {
      val doc = db.collection(FirestoreCollections.EVENTS).document(eventId).get().await()

      if (!doc.exists()) {
        throw NoSuchElementException("Event with id $eventId not found")
      }

      documentToEvent(doc) ?: throw IllegalStateException("Failed to parse event with id $eventId")
    } catch (e: Exception) {
      Log.e("EventRepoFirestore", "Error getting event $eventId", e)
      throw e
    }
  }

  override suspend fun createEvent(event: Event): Result<Unit> {
    val eventMap = eventToFirestoreMap(event, db)

    db.collection(FirestoreCollections.EVENTS).document(event.id).set(eventMap).await()
    return Result.success(Unit)
  }

  override suspend fun updateEvent(eventId: String, newEvent: Event): Result<Unit> {
    // case 1: ids of the events are different
    if (eventId != newEvent.id) {
      return Result.failure(IllegalArgumentException("Provided eventId does not match newEvent.id"))
    }
    return try {
      // case 2: the event doesn't exist
      val docRef = db.collection(FirestoreCollections.EVENTS).document(eventId)
      if (!docRef.get().await().exists()) {
        return Result.failure(
            NoSuchElementException("Cannot update. Event not found with ID: $eventId"))
      }

      // case 3: the event exists and can be updated
      val eventMap = eventToFirestoreMap(newEvent, db)

      docRef.set(eventMap).await()
      Result.success(Unit)
    } catch (e: Exception) {
      // Handle any other Firestore or coroutine exceptions
      Result.failure(e)
    }
  }

  override suspend fun deleteEvent(eventId: String): Result<Unit> {
    return try {
      val docRef = db.collection(FirestoreCollections.EVENTS).document(eventId)

      // case 1: The event to delete doesn't delete exists
      if (!docRef.get().await().exists()) {
        return Result.failure(
            NoSuchElementException("Cannot delete. Event not found with ID: $eventId"))
      }

      // case 2: the event can be deleted
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      // Handle any other Firestore or coroutine exceptions
      Result.failure(e)
    }
  }

  companion object {
    /**
     * Converts an [Event] object into a [Map] suitable for Firestore insertion, replacing the full
     * [Association] object with its [DocumentReference].
     */
    private fun eventToFirestoreMap(event: Event, db: FirebaseFirestore): Map<String, Any?> {
      val associationRef =
          db.collection(FirestoreCollections.ASSOCIATIONS).document(event.association.id)

      return mapOf(
          "id" to event.id,
          "title" to event.title,
          "description" to event.description,
          "location" to
              mapOf(
                  "name" to event.location.name,
                  "latitude" to event.location.latitude,
                  "longitude" to event.location.longitude),
          "time" to event.time,
          "association" to associationRef,
          "tags" to event.tags,
          "price" to event.price.toLong(),
          "pictureUrl" to event.pictureUrl,
      )
    }

    suspend fun getAssociation(document: DocumentSnapshot): Association? {
      val assocRef = document.get("association") as? DocumentReference ?: return null
      val assocSnap = assocRef.get().await()

      return AssociationRepositoryFirestore.documentToAssociation(assocSnap)
    }

    /**
     * Safely converts a Firestore [DocumentSnapshot] into an [Event] data class.
     *
     * This function performs strict type checking. If any *required* field (e.g., `title`,
     * `description`, `location`, `price`) is missing, malformed, or of the wrong type, the function
     * will log an error and return `null`.
     *
     * Optional fields like `imageUrl` will be set to `null` if not present. `tags` will default to
     * an empty list if not present.
     *
     * @param document The Firestore [DocumentSnapshot] to parse.
     * @return A parsed [Event] object, or `null` if conversion fails.
     */
    suspend fun documentToEvent(document: DocumentSnapshot): Event? {
      return try {
        // 1. Get the document's unique ID
        val id = document.id

        // 2. Get required String fields
        // If any are missing (null), the Elvis operator (?:) will return null
        // from the entire function.
        val title = document.getString("title")!!
        val description = document.getString("description")!!
        val time = document.getString("time")!!
        val association = getAssociation(document)

        // 3. Get optional String field
        // If 'imageUrl' is missing, getString() returns null, which is valid.
        val pictureUrl = document.getString("pictureUrl")

        // 4. Handle nested Location object
        // The 'location' field itself must exist and be a Map.

        val locMap = document.get("location") as? Map<*, *>

        val location =
            Location(
                name = locMap?.get("name") as String,
                latitude = locMap["latitude"] as Double,
                longitude = locMap["longitude"] as Double)

        // 5. Handle optional List of Strings
        val tags: List<String> =
            (document["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

        // 6. Handle numeric conversion for price (required)
        // Firestore stores all numbers as Long. Fail if 'price' is missing.
        val priceLong = document.getLong("price") ?: return null
        val price = priceLong.toUInt() // Convert to Kotlin's unsigned int

        // 7. Construct and return the final Event object
        Event(
            id = id,
            title = title,
            description = description,
            location = location,
            time = time,
            association = association!!,
            tags = tags,
            price = price,
            pictureUrl = pictureUrl)
      } catch (e: Exception) {
        // Catch any other errors (e.g., bad casts, toUInt() failure)
        Log.e("FirestoreMapper", "Error converting document ${document.id} to Event", e)
        null // Return null on any failure
      }
    }
  }
}
