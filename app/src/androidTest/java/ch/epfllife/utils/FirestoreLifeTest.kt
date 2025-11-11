package ch.epfllife.utils

import ch.epfllife.model.association.AssociationRepositoryFirestore
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.model.firestore.FirestoreCollections
import ch.epfllife.model.user.UserRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before

/**
 * Base class for all tests that interact with the Firebase Emulator regarding Firestore. This class
 * handles emulator setup, auth, and data cleanup. We need this class for all Firestore related
 * testing
 */
open class FirestoreLifeTest {

  // Repositories pointing to the emulator
  protected val assocRepository = AssociationRepositoryFirestore(FirebaseEmulator.firestore)
  protected val eventRepository = EventRepositoryFirestore(FirebaseEmulator.firestore)
  protected val userRepository = UserRepositoryFirestore(FirebaseEmulator.firestore)

  // Use a fake auth instance for testing
  protected val auth = Auth(FakeCredentialManager.withDefaultTestUser)

  @Before
  fun setUp() {
    // Ensure emulator is running and sign in a test user
    setUpEmulatorAuth(auth, "FirestoreLifeTest")

    // Clear all data before each test
    runTest { clearFirestore() }
  }

  @After
  fun tearDown() {
    // Clear data and sign out after each test
    runTest { clearFirestore() }
    Firebase.auth.signOut()
  }

  /** Helper to clear all collections used in the app. */
  suspend fun clearFirestore() {
    // see FirestoreCollections enum for collection names
    clearCollection(FirestoreCollections.ASSOCIATIONS)
    clearCollection(FirestoreCollections.EVENTS)
    clearCollection(FirestoreCollections.USERS)
  }

  /** Sub-Helper to clear all documents in a collection. */
  private suspend fun clearCollection(collectionPath: String) {
    val snapshot = FirebaseEmulator.firestore.collection(collectionPath).get().await()

    val batch = FirebaseEmulator.firestore.batch()
    snapshot.documents.forEach { batch.delete(it.reference) }
    batch.commit().await()
  }

  /**
   * Helper to get the number of documents in the associations collection. Used in
   * AssociationRepositoryFirestoreTest.kt
   */
  suspend fun getAssociationCount(): Int {
    return FirebaseEmulator.firestore
        .collection(FirestoreCollections.ASSOCIATIONS)
        .get()
        .await()
        .size()
  }

  /**
   * Helper to directly add an event to the emulator for testing. This bypasses the
   * EventRepositoryFirestore.createEvent method to allow independent testing
   */
  suspend fun seedEvent(event: Event) {
    FirebaseEmulator.firestore
        .collection(FirestoreCollections.EVENTS)
        .document(event.id)
        .set(event)
        .await()
  }
}
