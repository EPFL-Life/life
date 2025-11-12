package ch.epfllife.model.user

import android.util.Log
import ch.epfllife.model.firestore.FirestoreCollections
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  override suspend fun getCurrentUser(): User? {

    // get currently logged in user from firebase auth
    val currentFirebaseUser = Firebase.auth.currentUser ?: return null

    // get user from firestore based on his id
    return getUser(currentFirebaseUser.uid)
  }

  override suspend fun getAllUsers(): List<User> {
    return try {
      val snapshot = db.collection(FirestoreCollections.USERS).get().await()
      snapshot.mapNotNull { documentToUser(it) }
    } catch (e: Exception) {
      Log.e("UserRepository", "Error getting all users", e)
      emptyList()
    }
  }

  override suspend fun getUser(userId: String): User? {
    return try {
      db.collection(FirestoreCollections.USERS).document(userId).get().await().let {
        documentToUser(it)
      }
    } catch (e: Exception) {
      Log.e("UserRepository", "Error getting user with ID $userId", e)
      null
    }
  }

  override suspend fun createUser(newUser: User): Result<Unit> {
    return Result.runCatching {
      db.collection(FirestoreCollections.USERS).document(newUser.id).set(newUser).await()
    }
  }

  override suspend fun updateUser(userId: String, newUser: User): Result<Unit> {
    // 1. Check if the object's ID matches the parameter ID
    if (userId != newUser.id) {
      return Result.failure(
          IllegalArgumentException(
              "User ID mismatch. Parameter was $userId but object ID was ${newUser.id}"))
    }

    return try {
      val docRef = db.collection(FirestoreCollections.USERS).document(userId)

      // 2. Check if the user to update even exists
      if (!docRef.get().await().exists()) {
        return Result.failure(
            NoSuchElementException("Cannot update. User not found with ID: $userId"))
      }

      // 3. Perform the update
      docRef.set(newUser).await()
      Result.success(Unit)
    } catch (e: Exception) {
      // Handle any other Firestore or coroutine exceptions
      Result.failure(e)
    }
  }

  override suspend fun deleteUser(userId: String): Result<Unit> {
    return try {
      val docRef = db.collection(FirestoreCollections.USERS).document(userId)

      // 1. Check if the user to delete even exists
      if (!docRef.get().await().exists()) {
        return Result.failure(
            NoSuchElementException("Cannot delete. User not found with ID: $userId"))
      }

      // 2. Perform the deletion
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      // Handle any other Firestore or coroutine exceptions
      Result.failure(e)
    }
  }

  companion object {
    /**
     * Safely converts a Firestore [DocumentSnapshot] into a [User] data class.
     *
     * This function performs strict type checking. If any *required* field (e.g., `name`) is
     * missing, malformed, or of the wrong type, the function will log an error and return `null`.
     *
     * @param document The Firestore [DocumentSnapshot] to parse.
     * @return A parsed [User] object, or `null` if conversion fails.
     */
    fun documentToUser(document: DocumentSnapshot): User? {
      return try {
        // 1. Get the document ID
        val id = document.id

        // 2. Get required String fields (will throw NPE if missing, caught below)
        val name = document.getString("name")!!

        // 3. Get optional subscriptions list (defaults to empty)
        val subList =
            (document["subscriptions"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        val subscriptions = subList.toList()

        // 4. Get optional enrolledEvents list (defaults to empty)
        val enrolledEvents =
            (document["enrolledEvents"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

        // 5. Handle nested UserSettings data class (FIXED)
        // Get the userSettings map from Firestore
        val settingsMap = document.get("userSettings") as? Map<*, *>
        // Get "isDarkMode" from the map, defaulting to 'false' if it or the map doesn't exist
        val isDarkMode = settingsMap?.get("isDarkMode") as? Boolean ?: false
        val userSettings = UserSettings(isDarkMode = isDarkMode)

        // 5. Construct the User object
        User(
            id = id,
            name = name,
            subscriptions = subscriptions,
            enrolledEvents = enrolledEvents,
            userSettings = userSettings)
      } catch (e: NullPointerException) {
        // this can happen when one of the required fields is not present
        Log.e("UserRepository", "Error converting document to User", e)
        null
      }
    }
  }

  override suspend fun subscribeToEvent(eventId: String): Result<Unit> {
    TODO("Not yet implemented")
  }

  override suspend fun unsubscribeFromEvent(eventId: String): Result<Unit> {
    TODO("Not yet implemented")
  }
}
