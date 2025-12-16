package ch.epfllife.model.association

import android.net.Uri
import android.util.Log
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.model.firestore.FirestoreCollections
import ch.epfllife.model.firestore.createListenAll
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await

class AssociationRepositoryFirestore(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : AssociationRepository {

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

  override suspend fun createAssociation(association: Association): Result<Unit> {
    db.collection(FirestoreCollections.ASSOCIATIONS)
        .document(association.id)
        .set(association)
        .await()
    return Result.success(Unit)
  }

  override suspend fun updateAssociation(
      associationId: String,
      newAssociation: Association,
  ): Result<Unit> {

    // 1. Check if the object's ID matches the parameter ID
    if (associationId != newAssociation.id) {
      return Result.failure(
          IllegalArgumentException(
              "Association ID mismatch. Parameter was $associationId but object ID was ${newAssociation.id}"))
    }

    return try {
      // 2. Get the document reference
      val docRef = db.collection(FirestoreCollections.ASSOCIATIONS).document(associationId)

      // 3. Check if the association to update even exists
      if (!docRef.get().await().exists()) {
        return Result.failure(
            NoSuchElementException("Cannot update. Association not found with ID: $associationId"))
      }

      // 4. Perform the update
      docRef.set(newAssociation).await()
      Result.success(Unit)
    } catch (e: Exception) {
      // Handle any other Firestore or coroutine exceptions
      Result.failure(e)
    }
  }

  override suspend fun getEventsForAssociation(associationId: String): Result<List<Event>> {
    // Result.runCatching will automatically catch any exceptions
    // from the .await() call and return a Result.Failure.
    val associationRef: DocumentReference =
        db.collection(FirestoreCollections.ASSOCIATIONS).document(associationId)

    return Result.runCatching {
      val snapshot =
          db.collection(FirestoreCollections.EVENTS)
              .whereEqualTo("association", associationRef)
              .get()
              .await()

      snapshot.mapNotNull { EventRepositoryFirestore.documentToEvent(it) }
    }
  }

  override suspend fun deleteAssociation(associationId: String): Result<Unit> {
    return try {
      // 1. Get the document reference
      val docRef = db.collection(FirestoreCollections.ASSOCIATIONS).document(associationId)

      // 2. Check if the association to delete even exists
      if (!docRef.get().await().exists()) {
        return Result.failure(
            NoSuchElementException("Cannot delete. Association not found with ID: $associationId"))
      }

      // 3. Perform the deletion
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      // Handle any other Firestore or coroutine exceptions
      Result.failure(e)
    }
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

        val logoUrl = document.getString("logoUrl")

        // 4. Handle the Enum (EventCategory)
        // Get the category name as a String from Firestore
        val eventCategoryString = document.getString("eventCategory")!!
        // Convert the String to the EventCategory enum value
        val eventCategory = EventCategory.valueOf(eventCategoryString.uppercase())

        // 5. Get the optional about field
        val about = document.getString("about")

        // 6. Get the optional socialLinks field
        val socialLinks =
            (document.get("socialLinks") as? Map<*, *>)?.let { map ->
              map.entries
                  .filter { (k, v) -> k is String && v is String }
                  .associate { (k, v) -> k as String to v as String }
            }

        // 7. Construct the Association object
        Association(
            id = id,
            name = name,
            description = description,
            pictureUrl = pictureUrl,
            logoUrl = logoUrl,
            eventCategory = eventCategory,
            about = about,
            socialLinks = socialLinks,
        )
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

  override fun listenAll(scope: CoroutineScope, onChange: suspend (List<Association>) -> Unit) =
      createListenAll(
          scope,
          db.collection(FirestoreCollections.ASSOCIATIONS),
          ::documentToAssociation,
          onChange,
      )

  override suspend fun uploadAssociationImage(
      associationId: String,
      imageUri: Uri,
      imageType: AssociationImageType
  ): Result<String> {
    return try {
      val storageRef = storage.reference

      Log.d("AssociationRepo", "Storage Bucket: ${storage.app.options.storageBucket}")

      val imageRef =
          storageRef.child("associations/$associationId/${imageType.name.lowercase()}.jpg")

      Log.d("AssociationRepo", "Starting upload for $associationId to path: ${imageRef.path}")

      val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()

      // Upload with metadata
      imageRef.putFile(imageUri, metadata).await()

      Log.d("AssociationRepo", "PutFile completed. Fetching Download URL...")

      val downloadUrl = imageRef.downloadUrl.await()

      Log.d("AssociationRepo", "Download URL fetched: $downloadUrl")

      Result.success(downloadUrl.toString())
    } catch (e: Exception) {
      Log.e("AssociationRepo", "Error uploading image at step: ${e.stackTrace.firstOrNull()}", e)
      Result.failure(e)
    }
  }
}
