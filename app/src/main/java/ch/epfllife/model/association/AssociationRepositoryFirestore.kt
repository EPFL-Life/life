package ch.epfllife.model.association

import android.util.Log
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.model.firestore.FirestoreCollections
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AssociationRepositoryFirestore(private val db: FirebaseFirestore) : AssociationRepository {

  override fun getNewUid(): String {
    return db.collection(FirestoreCollections.ASSOCIATIONS).document().id
  }

  override suspend fun getAssociation(associationId: String): Association? {
    val document =
        db.collection(FirestoreCollections.ASSOCIATIONS).document(associationId).get().await()
    return documentToAssociation(document)
  }

  override suspend fun getAllAssociations(): List<Association> {
    val snapshot = db.collection(FirestoreCollections.ASSOCIATIONS).get().await()
    return snapshot.mapNotNull { documentToAssociation(it) }
  }

  override suspend fun createAssociation(association: Association) {
    db.collection(FirestoreCollections.ASSOCIATIONS)
        .document(association.id)
        .set(association)
        .await()
  }

  override suspend fun updateAssociation(newAssociation: Association) {

    // extract doc reference
    val docRef = db.collection(FirestoreCollections.ASSOCIATIONS).document(newAssociation.id)

    // check if doc does NOT exist
    if (!docRef.get().await().exists()) {
      // Throw an error because we can't update a non-existent document
      throw NoSuchElementException(
          "Association with id ${newAssociation.id} not found! Cannot update.")
    }

    // if it exists update it
    docRef.set(newAssociation).await()
  }

  //
  override suspend fun getEventsForAssociation(associationId: String): List<Event> {
    val snapshot =
        db.collection(FirestoreCollections.EVENTS)
            .whereEqualTo("associationId", associationId)
            .get()
            .await()

    return snapshot.mapNotNull { EventRepositoryFirestore.documentToEvent(it) }
  }

  // helper function for parsing

  companion object {
    /**
     * Safely converts a Firestore [DocumentSnapshot] into an [Association] data class.
     *
     * This function performs strict type checking. If any *required* field (e.g., `name`,
     * `description`, `eventCategory`) is missing, malformed, or of the wrong type, the function
     * will log an error and return `null`.
     *
     * Optional fields like `pictureUrl` will be set to `null` if not present.
     *
     * @param document The Firestore [DocumentSnapshot] to parse.
     * @return A parsed [Association] object, or `null` if conversion fails.
     */
    fun documentToAssociation(document: DocumentSnapshot): Association? {
      return try {
        // 1. Get the document ID
        val id = document.id

        // 2. Get required String fields (return null if missing)
        val name = document.getString("name")!!
        val description = document.getString("description")!!

        // 3. Get the nullable String field (no check needed, defaults to null)
        val pictureUrl = document.getString("pictureUrl")

        // 4. Handle the Enum (EventCategory)
        // Get the category name as a String from Firestore
        val eventCategoryString = document.getString("eventCategory")!!
        // Convert the String to the EventCategory enum value
        val eventCategory = EventCategory.valueOf(eventCategoryString.uppercase())

        // 5. Construct the Association object
        Association(
            id = id,
            name = name,
            description = description,
            pictureUrl = pictureUrl,
            eventCategory = eventCategory)
      } catch (e: NullPointerException) {
        // this can happen when one of the required fields is not present
        Log.e("AssociationRepository", "Error converting document to Association", e)
        null
      } catch (e: IllegalArgumentException) {
        // this can happen when parsing the eventCategory convert to non-existent enum value
        Log.e("AssociationRepository", "Error converting EventCategory to enum", e)
        null
      }
    }
  }
}
