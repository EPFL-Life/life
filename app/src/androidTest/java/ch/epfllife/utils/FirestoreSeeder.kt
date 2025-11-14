package ch.epfllife.utils

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.Event
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * !!! THIS IS AN INSTRUMENTED TEST, NOT A UNIT TEST !!!
 *
 * !!! THIS EDITS THE ACTUAL PROD DATABASE AND IS TO BE EXECUTED ONLY AND ONLY MANUALLY IN
 * DEVELOPMENT !!! DON'T EXECUTE UNLESS YOU KNOW WHAT YOU'RE DOING (ask daniel if not sure)
 *
 * HOW TO USE:
 * 1. Place this file in your `app/src/androidTest/java/ch/epfllife/utils/` directory.
 * 2. Make sure your official `google-services.json` is in your `app/` directory.
 * 3. Open this file in Android Studio.
 * 4. Right-click the class name `DatabaseSeederTest` or the method `seedOfficialDatabase`.
 * 5. Select "Run 'DatabaseSeederTest'".
 * 6. This will build and run the test on a connected device or emulator.
 * 7. Watch the "Run" tool window (or Logcat with filter "DatabaseSeeder") for output.
 *
 * This test writes to your LIVE Firestore database. It is designed to be run manually only when you
 * need to populate or reset your development data.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseSeederTest {

  private val SEED_TAG = "DatabaseSeeder"
  private lateinit var db: FirebaseFirestore

  @Before
  fun setup() {
    // Initialize Firebase
    val context = ApplicationProvider.getApplicationContext<Context>()
    FirebaseApp.initializeApp(context)
    db = FirebaseFirestore.getInstance()
  }

  /** Converts an Association data class into a Map for Firestore. */
  private fun associationToMap(assoc: Association): Map<String, Any?> {
    return mapOf(
        "name" to assoc.name,
        "description" to assoc.description,
        "eventCategory" to assoc.eventCategory.name, // Store enum as string
        "pictureUrl" to assoc.pictureUrl,
        "about" to assoc.about,
        "socialLinks" to assoc.socialLinks)
  }

  /**
   * Converts an Event data class into a Map for Firestore, replacing the full Association object
   * with a DocumentReference.
   */
  private fun eventToMap(event: Event, assocRef: DocumentReference): Map<String, Any?> {
    return mapOf(
        "title" to event.title,
        "description" to event.description,
        "location" to
            mapOf( // Store location as a nested map
                "name" to event.location.name,
                "latitude" to event.location.latitude,
                "longitude" to event.location.longitude),
        "time" to event.time,
        "association" to assocRef, // Use the DocumentReference
        "tags" to event.tags,
        "price" to event.price.toLong(), // Store UInt as Long
        "pictureUrl" to event.pictureUrl)
  }

  // IF YOU EVER WANT TO EXECUTE THIS YOU HAVE TO COMMENT OUT @Ignore EVEN IF EXECUTING THE TEST
  // DIRECTLY
  @Ignore("THIS TEST EDITS THE PROD DATABASE, DO NOT EXECUTE")
  @Test
  fun seedOfficialDatabase() {
    runBlocking {
      Log.d(SEED_TAG, "--- STARTING OFFICIAL DATABASE SEED ---")

      try {
        val batch = db.batch()
        val assocRefMap = mutableMapOf<String, DocumentReference>()

        // --- 1. Seed Associations ---
        Log.d(SEED_TAG, "Preparing associations...")
        for (assoc in ExampleAssociations.allAssociations) {
          val assocRef = db.collection("associations").document(assoc.id)
          val assocData = associationToMap(assoc)
          batch.set(assocRef, assocData)
          assocRefMap[assoc.id] = assocRef // Save ref for later
          Log.d(SEED_TAG, "Prepared: ${assoc.id}")
        }

        // --- 2. Seed Events ---
        Log.d(SEED_TAG, "Preparing events...")
        for (event in ExampleEvents.allEvents) {
          val eventRef = db.collection("events").document(event.id)
          // Find the correct association reference from the map
          val assocRef = assocRefMap[event.association.id]
          if (assocRef == null) {
            Log.w(
                SEED_TAG,
                "Skipping event ${event.id}: Could not find assoc ref for ${event.association.id}")
            continue
          }

          val eventData = eventToMap(event, assocRef)
          batch.set(eventRef, eventData)
          Log.d(SEED_TAG, "Prepared: ${event.id}")
        }

        // --- 3. Commit Batch ---
        Log.d(SEED_TAG, "Committing batch to Firestore...")
        batch.commit().await()
        Log.d(SEED_TAG, "--- DATABASE SEEDING SUCCESSFUL ---")
      } catch (e: Exception) {
        Log.e(SEED_TAG, "--- ERROR SEEDING DATABASE ---", e)
        // Re-throw to fail the test
        throw e
      }
    }
  }
}
