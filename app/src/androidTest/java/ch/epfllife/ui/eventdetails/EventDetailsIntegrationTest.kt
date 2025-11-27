package ch.epfllife.ui.eventDetails

import androidx.test.platform.app.InstrumentationRegistry
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.user.User
import ch.epfllife.utils.FirestoreLifeTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventDetailsIntegrationTest : FirestoreLifeTest() {

  @Test
  fun enrollAndUnenroll_UpdatesFirestoreAndUI() = runTest {
    // 1. Setup Data
    val event = ExampleEvents.event1
    val association = event.association

    // Seed association first (required for event parsing)
    val assocResult = db.assocRepo.createAssociation(association)
    assertTrue("Failed to seed association", assocResult.isSuccess)

    val eventResult = db.eventRepo.createEvent(event)
    assertTrue("Failed to create event", eventResult.isSuccess)

    val currentUser = auth.auth.currentUser
    checkNotNull(currentUser) { "User must be signed in for integration test" }

    // Create user in Firestore who is NOT enrolled initially
    val user =
        User(id = currentUser.uid, name = "Integration Test User", enrolledEvents = emptyList())

    val result = db.userRepo.createUser(user)
    assertTrue("Failed to create user", result.isSuccess)

    // 2. Initialize ViewModel with real repositories (connected to emulator)
    val viewModel = EventDetailsViewModel(db = db)

    // 3. Load Event and Verify Initial State (Not Enrolled)
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    viewModel.loadEvent(event.id, context)

    var state =
        viewModel.uiState.first {
          if (it is EventDetailsUIState.Error) {
            throw AssertionError("Expected Success but got Error: ${it.message}")
          }
          it is EventDetailsUIState.Success
        }
    assertFalse(
        "User should NOT be enrolled initially", (state as EventDetailsUIState.Success).isEnrolled)

    // 4. Perform Action: Enroll
    viewModel.enrollInEvent(event, context)

    // 5. Verify UI State Update (Enrolled)
    state =
        viewModel.uiState.first {
          if (it is EventDetailsUIState.Error) {
            throw AssertionError("Expected Success but got Error: ${it.message}")
          }
          (it as? EventDetailsUIState.Success)?.isEnrolled == true
        }
    assertTrue("UI should show user as enrolled", (state as EventDetailsUIState.Success).isEnrolled)

    // 6. Verify Firestore Update (Enrolled)
    var updatedUser = db.userRepo.getUser(currentUser.uid)
    checkNotNull(updatedUser)
    assertTrue(
        "User should be added to enrolledEvents in Firestore",
        updatedUser.enrolledEvents.contains(event.id))

    // 7. Perform Action: Unenroll
    viewModel.unenrollFromEvent(event, context)

    // 8. Verify UI State Update (Unenrolled)
    state =
        viewModel.uiState.first {
          if (it is EventDetailsUIState.Error) {
            throw AssertionError("Expected Success but got Error: ${it.message}")
          }
          (it as? EventDetailsUIState.Success)?.isEnrolled == false
        }
    assertFalse(
        "UI should show user as unenrolled", (state as EventDetailsUIState.Success).isEnrolled)

    // 9. Verify Firestore Update (Unenrolled)
    updatedUser = db.userRepo.getUser(currentUser.uid)
    checkNotNull(updatedUser)
    assertFalse(
        "User should be removed from enrolledEvents in Firestore",
        updatedUser.enrolledEvents.contains(event.id))
  }
}
