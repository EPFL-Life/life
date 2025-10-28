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

    val newAssociationId = newAssociation.id

    // check if id exists
    if (db.collection(FirestoreCollections.ASSOCIATIONS)
        .document(newAssociationId)
        .get()
        .await()
        .exists()) {
      throw NoSuchElementException("Association with id ${newAssociation.id} not found")
    }

    // update association based on uid of passed object
    db.collection(FirestoreCollections.ASSOCIATIONS)
        .document(newAssociationId)
        .set(newAssociation)
        .await()
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

  /**
   * Safely converts a Firestore [DocumentSnapshot] into an [Association] data class.
   *
   * This function performs strict type checking. If any *required* field (e.g., `name`,
   * `description`, `eventCategory`) is missing, malformed, or of the wrong type, the function will
   * log an error and return `null`.
   *
   * Optional fields like `pictureUrl` will be set to `null` if not present.
   *
   * @param document The Firestore [DocumentSnapshot] to parse.
   * @return A parsed [Association] object, or `null` if conversion fails.
   */
  private fun documentToAssociation(document: DocumentSnapshot): Association? {
    return try {
      // 1. Get the document ID
      val id = document.id

      // 2. Get required String fields (return null if missing)
      val name = document.getString("name") ?: return null
      val description = document.getString("description") ?: return null

      // 3. Get the nullable String field (no check needed, defaults to null)
      val pictureUrl = document.getString("pictureUrl")

      // 4. Handle the Enum (EventCategory)
      // Get the category name as a String from Firestore
      val eventCategoryString = document.getString("eventCategory") ?: return null
      // Convert the String to the EventCategory enum value
      val eventCategory = EventCategory.valueOf(eventCategoryString.uppercase())

      // 5. Construct the Association object
      Association(
          id = id,
          name = name,
          description = description,
          pictureUrl = pictureUrl,
          eventCategory = eventCategory)
    } catch (e: Exception) {
      // Log any errors during conversion (e.g., valueOf fails for enum)
      Log.e("AssociationRepository", "Error converting document to Association", e)
      null
    }
  }
}
