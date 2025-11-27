package ch.epfllife.ui.eventdetails

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfllife.R
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.ui.eventDetails.EventDetailsUIState
import ch.epfllife.ui.eventDetails.EventDetailsViewModel
import ch.epfllife.utils.FirestoreLifeTest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Test suite for EventDetailsViewModel error handling with context.getString.
 *
 * Each test targets a specific error scenario that uses context.getString to retrieve localized
 * error messages.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventDetailsViewModelTest : FirestoreLifeTest() {

  private val context: Context by lazy {
    InstrumentationRegistry.getInstrumentation().targetContext
  }

  @Test
  fun loadEvent_eventNotFound_returnsErrorWithCorrectMessage() = runTest {
    // Arrange: No event in the database
    assertEquals(0, getEventCount())

    val authUid = Firebase.auth.currentUser!!.uid
    val user = ExampleUsers.user1.copy(id = authUid)
    userRepository.createUser(user)

    val viewModel = EventDetailsViewModel(repo = eventRepository, userRepo = userRepository)

    // Act: Try to load a non-existent event
    viewModel.loadEvent("nonexistent-event-id", context)

    // Assert: Hits line 48: context.getString(R.string.error_event_not_found)
    val state = viewModel.uiState.first { it !is EventDetailsUIState.Loading }
    assertTrue(state is EventDetailsUIState.Error)
    val errorState = state as EventDetailsUIState.Error
    assertEquals(context.getString(R.string.error_loading_event), errorState.message)
  }

  @Test
  fun loadEvent_repositoryThrowsException_returnsErrorWithCorrectMessage() = runTest {
    // Arrange: Create event and user, then delete event to trigger exception path
    val event = ExampleEvents.event1
    eventRepository.createEvent(event)

    val authUid = Firebase.auth.currentUser!!.uid
    val user = ExampleUsers.user1.copy(id = authUid)
    userRepository.createUser(user)

    val viewModel = EventDetailsViewModel(repo = eventRepository, userRepo = userRepository)

    eventRepository.deleteEvent(event.id)

    // Act: Load deleted event to trigger catch block
    viewModel.loadEvent(event.id, context)

    // Assert: Hits line 51: context.getString(R.string.error_loading_event)
    val state = viewModel.uiState.first { it !is EventDetailsUIState.Loading }
    assertTrue(state is EventDetailsUIState.Error)
    val errorState = state as EventDetailsUIState.Error
    assertEquals(context.getString(R.string.error_loading_event), errorState.message)
  }

  @Test
  fun enrollInEvent_subscriptionFails_returnsErrorWithCorrectMessage() = runTest {
    // Arrange: Create event
    val event = ExampleEvents.event1
    eventRepository.createEvent(event)

    val viewModel = EventDetailsViewModel(repo = eventRepository, userRepo = userRepository)

    // Arrange: Sign out to cause subscribeToEvent to fail
    Firebase.auth.signOut()

    // Act: Try to enroll without authentication
    viewModel.enrollInEvent(event, context)

    // Assert: Hits line 70: context.getString(R.string.error_failed_to_enroll_event)
    val state = viewModel.uiState.first { it !is EventDetailsUIState.Loading }
    assertTrue(state is EventDetailsUIState.Error)
    val errorState = state as EventDetailsUIState.Error
    assertEquals(context.getString(R.string.error_enroll_failed), errorState.message)
  }

  @Test
  fun enrollInEvent_exceptionDuringEnrollment_returnsErrorWithCorrectMessage() = runTest {
    // Arrange: Create event but no user document
    val event = ExampleEvents.event1
    eventRepository.createEvent(event)

    val viewModel = EventDetailsViewModel(repo = eventRepository, userRepo = userRepository)

    // Note: Authenticated but no user document exists, causing subscribeToEvent to fail

    // Act: Try to enroll
    viewModel.enrollInEvent(event, context)

    // Assert: Hits line 74: context.getString(R.string.error_failed_to_enroll_event)
    val state = viewModel.uiState.first { it !is EventDetailsUIState.Loading }
    assertTrue(state is EventDetailsUIState.Error)
    val errorState = state as EventDetailsUIState.Error
    assertEquals(context.getString(R.string.error_enroll_failed), errorState.message)
  }
}
