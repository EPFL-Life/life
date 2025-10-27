package ch.epfllife.model.association

import android.util.Log
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.firestore.FirestoreCollections
import ch.epfllife.model.map.Location
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
    return documentToAssociation(document) ?: throw Exception("Association not found")
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
    // check if id exists
    if (db.collection(FirestoreCollections.ASSOCIATIONS)
        .document(newAssociation.id)
        .get()
        .await() == null) {
      throw NoSuchElementException("Association with id ${newAssociation.id} not found")
    }

    // update association based on uid of passed object
    db.collection(FirestoreCollections.ASSOCIATIONS)
        .document(newAssociation.id)
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

    return snapshot.mapNotNull { documentToEvent(it) }
  }

  // TODO: Should the parsing functions maybe be moved to the model class or keep it here (which
  // would maybe result some code duplication)?
  // helper function for parsing
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
      val eventCategory = EventCategory.valueOf(eventCategoryString)

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

  // helper function for parsing
  private fun documentToEvent(document: DocumentSnapshot): Event? {
    return try {
      val id = document.id
      val title = document.getString("title") ?: return null
      val description = document.getString("description") ?: return null
      val time = document.getString("time") ?: return null
      val associationId = document.getString("associationId") ?: return null
      val imageUrl = document.getString("imageUrl")

      // Handle nested Location object
      val locationData = document.get("location") as? Map<*, *> ?: return null
      val location =
          Location(
              latitude = locationData["latitude"] as? Double ?: 0.0,
              longitude = locationData["longitude"] as? Double ?: 0.0,
              name = locationData["name"] as? String ?: "")

      // Handle Set<String> for tags (Firestore stores as List)
      val tagsList = document.get("tags") as? List<String> ?: emptyList()
      val tags = tagsList.toSet()

      // Handle UInt for price (Firestore stores as Long)
      val priceLong = document.getLong("price") ?: 0L
      val price = priceLong.toUInt()

      Event(
          id = id,
          title = title,
          description = description,
          location = location,
          time = time,
          associationId = associationId,
          tags = tags,
          price = price,
          imageUrl = imageUrl)
    } catch (e: Exception) {
      Log.e("AssociationRepository", "Error converting document to Event", e)
      null
    }
  }
}
