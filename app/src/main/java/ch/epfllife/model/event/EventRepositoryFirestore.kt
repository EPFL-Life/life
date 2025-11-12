package ch.epfllife.model.event

import android.util.Log
import ch.epfllife.model.association.Association
import ch.epfllife.model.firestore.FirestoreCollections
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.Price
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.get
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class EventRepositoryFirestore(private val db: FirebaseFirestore) : EventRepository {

  override fun getNewUid(): String {
    TODO("Not yet implemented")
  }

  override suspend fun getAllEvents(): List<Event> = coroutineScope {
    val task = db.collection(FirestoreCollections.EVENTS).get().await()
    task.documents.map { doc -> async { documentToEvent(doc) } }.mapNotNull { it.await() }
  }

  override suspend fun getEvent(eventId: String): Event {
    TODO("Not yet implemented")
  }

  override suspend fun createEvent(event: Event): Result<Unit> {
    return runCatching {
      db.collection(FirestoreCollections.EVENTS).document(event.id).set(event).await()
    }
  }

  override suspend fun updateEvent(eventId: String, newEvent: Event): Result<Unit> {
    // add a check if the eventId is same as newEvent.Id
    TODO("Not yet implemented")
  }

  override suspend fun deleteEvent(eventId: String): Result<Unit> {
    TODO("Not yet implemented")
  }

  companion object {

    suspend fun getAssociation(document: DocumentSnapshot): Association? {
      val assocRef = document.get("association") as? DocumentReference ?: return null
      val assocSnap = assocRef.get().await()

      return Association(
          id = assocSnap.id,
          name = assocSnap.get("name").toString(),
          description = assocSnap.getString("description")!!,
          pictureUrl = assocSnap.getString("pictureUrl"),
          eventCategory = EventCategory.valueOf(assocSnap.getString("eventCategory")!!))
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
        val price = Price(priceLong.toUInt()) // Convert to Kotlin's unsigned int and then price

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
