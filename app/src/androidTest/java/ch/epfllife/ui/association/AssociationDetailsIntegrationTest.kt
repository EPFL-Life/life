package ch.epfllife.ui.association

import androidx.test.platform.app.InstrumentationRegistry
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.model.user.User
import ch.epfllife.utils.FirestoreLifeTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssociationDetailsIntegrationTest : FirestoreLifeTest() {

  @Test
  fun subscribeAndUnsubscribe_UpdatesFirestoreAndUI() = runTest {
    // 1. Setup Data
    val association = ExampleAssociations.association1

    // Seed association
    val assocResult = db.assocRepo.createAssociation(association)
    assertTrue("Failed to seed association", assocResult.isSuccess)

    val currentUser = auth.auth.currentUser
    checkNotNull(currentUser) { "User must be signed in for integration test" }

    // Create user in Firestore who is NOT subscribed initially
    val user =
        User(id = currentUser.uid, name = "Integration Test User", subscriptions = emptyList())

    val result = db.userRepo.createUser(user)
    assertTrue("Failed to create user", result.isSuccess)

    // 2. Initialize ViewModel with real repositories (connected to emulator)
    val viewModel = AssociationDetailsViewModel(db = db)

    // 3. Load Association and Verify Initial State (Not Subscribed)
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    viewModel.loadAssociation(association.id, context)

    var state =
        viewModel.uiState.first {
          if (it is AssociationDetailsUIState.Error) {
            throw AssertionError("Expected Success but got Error: ${it.message}")
          }
          it is AssociationDetailsUIState.Success
        }
    assertFalse(
        "User should NOT be subscribed initially",
        (state as AssociationDetailsUIState.Success).isSubscribed)

    // 4. Perform Action: Subscribe
    viewModel.subscribeToAssociation(association.id, context)

    // 5. Verify UI State Update (Subscribed)
    state =
        viewModel.uiState.first {
          if (it is AssociationDetailsUIState.Error) {
            throw AssertionError("Expected Success but got Error: ${it.message}")
          }
          (it as? AssociationDetailsUIState.Success)?.isSubscribed == true
        }
    assertTrue(
        "UI should show user as subscribed",
        (state as AssociationDetailsUIState.Success).isSubscribed)

    // 6. Verify Firestore Update (Subscribed)
    // Poll for the update as Firestore operations are asynchronous
    var updatedUser: User? = null
    var attempts = 0
    while (attempts < 10) {
      updatedUser = db.userRepo.getUser(currentUser.uid)
      if (updatedUser?.subscriptions?.contains(association.id) == true) {
        break
      }
      kotlinx.coroutines.delay(500)
      attempts++
    }

    checkNotNull(updatedUser)
    assertTrue(
        "User should be added to subscriptions in Firestore",
        updatedUser.subscriptions.contains(association.id))

    // 7. Perform Action: Unsubscribe
    viewModel.unsubscribeFromAssociation(association.id, context)

    // 8. Verify UI State Update (Unsubscribed)
    state =
        viewModel.uiState.first {
          if (it is AssociationDetailsUIState.Error) {
            throw AssertionError("Expected Success but got Error: ${it.message}")
          }
          (it as? AssociationDetailsUIState.Success)?.isSubscribed == false
        }
    assertFalse(
        "UI should show user as unsubscribed",
        (state as AssociationDetailsUIState.Success).isSubscribed)

    // 9. Verify Firestore Update (Unsubscribed)
    attempts = 0
    while (attempts < 10) {
      updatedUser = db.userRepo.getUser(currentUser.uid)
      if (updatedUser?.subscriptions?.contains(association.id) == false) {
        break
      }
      kotlinx.coroutines.delay(500)
      attempts++
    }

    checkNotNull(updatedUser)
    assertFalse(
        "User should be removed from subscriptions in Firestore",
        updatedUser.subscriptions.contains(association.id))
  }
}
