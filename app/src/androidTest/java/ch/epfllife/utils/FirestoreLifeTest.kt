package ch.epfllife.utils

import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.model.firestore.FirestoreCollections
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mockito

/**
 * Base class for all tests that interact with the Firebase Emulator regarding Firestore. This class
 * handles emulator setup, auth, and data cleanup. We need this class for all Firestore related
 * testing
 */
open class FirestoreLifeTest {

  // Repositories pointing to the emulator
  protected val mockStorage = Mockito.mock(FirebaseStorage::class.java)
  protected val db = Db.forTest(mockStorage)

  // Use a fake auth instance for testing
  protected val auth = Auth(FakeCredentialManager.withDefaultTestUser)

  @Before
  fun setUp() {
    // We must use runTest here to ensure that both auth and cleanup
    // are complete before the actual @Test method runs.
    // There was an Issue with race condition (setUp/login was not done before other tests started)
    runTest {
      // Ensure emulator is running and sign in a test user
      // This MUST be inside runTest to ensure it completes before the test
      setUpEmulator(auth, "FirestoreLifeTest")

      // Clear all data before each test
      clearFirestore()

      // Sign IN a test user for all tests inheriting from this class
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      assertTrue(
          "Failed to sign in default test user in FirestoreLifeTest.setUp",
          signInResult is SignInResult.Success,
      )
    }
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

  /** Helper to get the number of documents in the users collection. */
  suspend fun getUserCount(): Int {
    return FirebaseEmulator.firestore.collection(FirestoreCollections.USERS).get().await().size()
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

  /** Helper to get the number of events. Used in EventRepositoryFirestoreTest.kt */
  suspend fun getEventCount(): Int {
    return FirebaseEmulator.firestore.collection(FirestoreCollections.EVENTS).get().await().size()
  }
}
