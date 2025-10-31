package ch.epfllife.ui.eventdetails

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.Price
import ch.epfllife.ui.eventDetails.EventDetailsContent
import ch.epfllife.ui.eventDetails.EventDetailsTestTags
import ch.epfllife.ui.eventDetails.EventDetailsUIState
import ch.epfllife.ui.eventDetails.EventDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class EventDetailsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleEvent =
      Event(
          id = "event1",
          title = "Drone Workshop",
          description = "The Drone Workshop is a multi-evening workshop organized by AéroPoly...",
          location = Location(46.5191, 6.5668, "Centre Sport et Santé"),
          time = "2025-10-12 18:00",
          associationId = "AeroPoly",
          tags = setOf("workshop"),
          price = Price(10u),
          imageUrl =
              "https://www.shutterstock.com/image-photo/engineer-working-on-racing-fpv-600nw-2278353271.jpg")

  // ============ ViewModel Tests ============

  @Test
  fun viewModel_InitialStateIsLoading() = runTest {
    val viewModel = EventDetailsViewModel()
    val initialState = viewModel.uiState.value
    assertTrue("Initial state should be Loading", initialState is EventDetailsUIState.Loading)
  }

  @Test
  fun viewModel_LoadEventSuccessfully() = runTest {
    val viewModel = EventDetailsViewModel()
    viewModel.loadEvent("event1")

    // Give the coroutine time to complete
    withContext(Dispatchers.Main) { Thread.sleep(100) }

    val state = viewModel.uiState.value
    assertTrue("State should be Success after loading", state is EventDetailsUIState.Success)
    if (state is EventDetailsUIState.Success) {
      assertNotNull("Event should not be null", state.event)
      assertEquals("Event title should match", "Drone Workshop", state.event.title)
      assertFalse("Initially should not be enrolled", state.isEnrolled)
    }
  }

  @Test
  fun viewModel_EnrollInEventUpdatesState() = runTest {
    val viewModel = EventDetailsViewModel()
    viewModel.loadEvent("event1")

    withContext(Dispatchers.Main) { Thread.sleep(100) }

    val initialState = viewModel.uiState.value
    assertTrue("Initial state should be Success", initialState is EventDetailsUIState.Success)

    if (initialState is EventDetailsUIState.Success) {
      viewModel.enrollInEvent(initialState.event)
      withContext(Dispatchers.Main) { Thread.sleep(100) }

      val enrolledState = viewModel.uiState.value
      assertTrue("State should still be Success", enrolledState is EventDetailsUIState.Success)
      if (enrolledState is EventDetailsUIState.Success) {
        assertTrue("Should be enrolled after enrolling", enrolledState.isEnrolled)
      }
    }
  }

  @Test
  fun viewModel_IsEnrolledReturnsFalse() {
    val viewModel = EventDetailsViewModel()
    val isEnrolled = viewModel.isEnrolled(sampleEvent)
    assertFalse("isEnrolled should return false (not implemented yet)", isEnrolled)
  }

  @Test
  fun viewModel_StateFlowEmitsUpdates() = runBlocking {
    val viewModel = EventDetailsViewModel()
    val stateFlow = viewModel.uiState

    // Check initial state
    val initialState = stateFlow.value
    assertTrue("Initial state should be Loading", initialState is EventDetailsUIState.Loading)

    // Load event
    viewModel.loadEvent("event1")
    withContext(Dispatchers.Main) { Thread.sleep(100) }

    // Check updated state
    val updatedState = stateFlow.value
    assertTrue("Updated state should be Success", updatedState is EventDetailsUIState.Success)
  }

  // ============ EventDetailsContent Tests ============

  @Test
  fun content_DisplaysEventTitle() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Drone Workshop").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEventAssociation() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_ASSOCIATION).assertIsDisplayed()
    composeTestRule.onNodeWithText("AeroPoly").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEventPrice() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_PRICE).assertIsDisplayed()
    composeTestRule.onNodeWithText("CHF 10").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEventLocation() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithText("Centre Sport et Santé").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEventDescription() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_DESCRIPTION).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(
            "The Drone Workshop is a multi-evening workshop organized by AéroPoly...",
            substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun content_DisplaysBackButton() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEnrollButton() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("Enrol in event").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysViewLocationButton() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("View Location on Map").assertIsDisplayed()
  }

  @Test
  fun content_BackButtonTriggersCallback() {
    var backClicked = false
    composeTestRule.setContent {
      EventDetailsContent(
          event = sampleEvent, onGoBack = { backClicked = true }, viewModel = viewModel())
    }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).performClick()
    assertTrue("Back button should trigger onGoBack callback", backClicked)
  }

  @Test
  fun content_EnrollButtonIsClickable() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).assertHasClickAction()
  }

  @Test
  fun content_ViewLocationButtonIsClickable() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON).assertHasClickAction()
  }

  // ============ Different Event Data Tests ============

  @Test
  fun content_DisplaysFreeEvent() {
    val freeEvent = sampleEvent.copy(price = Price(0u))
    composeTestRule.setContent { EventDetailsContent(event = freeEvent, viewModel = viewModel()) }
    // Price should be empty string for 0u price based on the code
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_PRICE).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysExpensiveEvent() {
    val expensiveEvent = sampleEvent.copy(price = Price(999u))
    composeTestRule.setContent {
      EventDetailsContent(event = expensiveEvent, viewModel = viewModel())
    }
    composeTestRule.onNodeWithText("CHF 999").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysLongTitle() {
    val longTitleEvent =
        sampleEvent.copy(
            title = "This is a very long event title that should still display properly")
    composeTestRule.setContent {
      EventDetailsContent(event = longTitleEvent, viewModel = viewModel())
    }
    composeTestRule
        .onNodeWithText("This is a very long event title that should still display properly")
        .assertIsDisplayed()
  }

  @Test
  fun content_DisplaysLongDescription() {
    val longDescription =
        "This is a very long description that contains a lot of information about the event. " +
            "It should wrap properly and display all the content to the user. " +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
    val longDescEvent = sampleEvent.copy(description = longDescription)
    composeTestRule.setContent {
      EventDetailsContent(event = longDescEvent, viewModel = viewModel())
    }
    composeTestRule.onNodeWithText(longDescription, substring = true).assertIsDisplayed()
  }

  @Test
  fun content_HandlesEventWithoutImageUrl() {
    val eventWithoutImage = sampleEvent.copy(imageUrl = null)
    composeTestRule.setContent {
      EventDetailsContent(event = eventWithoutImage, viewModel = viewModel())
    }
    // Image should still display with default URL
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_IMAGE).assertExists()
  }

  @Test
  fun content_HandlesEventWithEmptyTags() {
    val eventWithNoTags = sampleEvent.copy(tags = emptySet())
    composeTestRule.setContent {
      EventDetailsContent(event = eventWithNoTags, viewModel = viewModel())
    }
    // Should still display without crashing
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TITLE).assertIsDisplayed()
  }

  @Test
  fun content_HandlesEventWithMultipleTags() {
    val eventWithTags = sampleEvent.copy(tags = setOf("workshop", "tech", "drone", "engineering"))
    composeTestRule.setContent {
      EventDetailsContent(event = eventWithTags, viewModel = viewModel())
    }
    // Should display without crashing (tags might not be shown in UI yet)
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TITLE).assertIsDisplayed()
  }

  // ============ UI State Tests ============

  @Test
  fun uiState_SuccessContainsCorrectEvent() {
    val event = sampleEvent
    val state = EventDetailsUIState.Success(event, false)
    assertEquals("Success state should contain the event", event, state.event)
    assertFalse("Success state should have correct enrollment status", state.isEnrolled)
  }

  @Test
  fun uiState_SuccessCanBeEnrolled() {
    val event = sampleEvent
    val state = EventDetailsUIState.Success(event, true)
    assertTrue("Success state should reflect enrollment", state.isEnrolled)
  }

  @Test
  fun uiState_ErrorContainsMessage() {
    val errorMessage = "Failed to load event"
    val state = EventDetailsUIState.Error(errorMessage)
    assertEquals("Error state should contain message", errorMessage, state.message)
  }

  @Test
  fun uiState_LoadingIsObject() {
    val state: EventDetailsUIState = EventDetailsUIState.Loading
    assertNotNull("Loading state should not be null", state)
    assertEquals("Loading should be singleton", EventDetailsUIState.Loading, state)
  }

  // ============ Integration Tests ============

  @Test
  fun integration_AllContentDisplayedTogether() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }

    // Verify all major components are present
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_IMAGE).assertExists()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_ASSOCIATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_PRICE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_DESCRIPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun integration_MultipleClicksOnEnrollButton() {
    var clickCount = 0
    val testViewModel = EventDetailsViewModel()

    composeTestRule.setContent {
      EventDetailsContent(
          event = sampleEvent, viewModel = testViewModel, onGoBack = { clickCount++ })
    }

    // Click enroll button multiple times
    repeat(3) {
      composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).performClick()
      composeTestRule.waitForIdle()
    }

    // Should not crash
    composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).assertIsDisplayed()
  }

  @Test
  fun integration_MultipleClicksOnBackButton() {
    var backClickCount = 0
    composeTestRule.setContent {
      EventDetailsContent(
          event = sampleEvent, onGoBack = { backClickCount++ }, viewModel = viewModel())
    }

    // Click back button multiple times
    repeat(5) {
      composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).performClick()
      composeTestRule.waitForIdle()
    }

    assertEquals("Back button should be clicked 5 times", 5, backClickCount)
  }

  @Test
  fun integration_NavigationBetweenStates() {
    var goBackCalled = false
    composeTestRule.setContent {
      EventDetailsContent(
          event = sampleEvent, onGoBack = { goBackCalled = true }, viewModel = viewModel())
    }

    // Verify content is displayed
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TITLE).assertIsDisplayed()

    // Click back button
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).performClick()

    assertTrue("Navigation callback should be triggered", goBackCalled)
  }

  @Test
  fun content_AllButtonsAreAccessible() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }

    // All interactive elements should be present and accessible
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON).assertHasClickAction()
  }

  @Test
  fun content_VerifyTextStyling() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }

    // Verify that all text elements exist (styling is applied correctly)
    composeTestRule.onNodeWithText("Drone Workshop").assertExists()
    composeTestRule.onNodeWithText("AeroPoly").assertExists()
    composeTestRule.onNodeWithText("CHF 10").assertExists()
    composeTestRule.onNodeWithText("Centre Sport et Santé").assertExists()
    composeTestRule.onNodeWithText("Description").assertExists()
  }

  @Test
  fun content_DescriptionLabelDisplayed() {
    composeTestRule.setContent { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
    composeTestRule.onNodeWithText("Description").assertIsDisplayed()
  }

  @Test
  fun viewModel_MultipleLoadEventCalls() = runTest {
    val viewModel = EventDetailsViewModel()

    // Load event multiple times
    repeat(3) {
      viewModel.loadEvent("event$it")
      withContext(Dispatchers.Main) { Thread.sleep(50) }
    }

    val state = viewModel.uiState.value
    assertTrue("Final state should be Success", state is EventDetailsUIState.Success)
  }
}
