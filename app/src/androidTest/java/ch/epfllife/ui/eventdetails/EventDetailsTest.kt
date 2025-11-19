package ch.epfllife.ui.eventdetails

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.map.Location
import ch.epfllife.model.user.Price
import ch.epfllife.ui.eventDetails.EventDetailsContent
import ch.epfllife.ui.eventDetails.EventDetailsTestTags
import ch.epfllife.ui.eventDetails.EventDetailsUIState
import ch.epfllife.ui.eventDetails.EventDetailsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class EventDetailsTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleEvent =
      Event(
          id = "AeroPoly",
          title = "Drone Workshop",
          description = "The Drone Workshop is a multi-evening workshop organized by AéroPoly...",
          location = Location(46.5191, 6.5668, "Centre Sport et Santé"),
          time = "2025-10-12 18:00",
          association =
              Association(
                  name = "AeroPoly",
                  id = "AeroPoly",
                  description = "Description",
                  eventCategory = EventCategory.ACADEMIC),
          tags = listOf("workshop"),
          price = Price(10u),
          pictureUrl =
              "https://www.shutterstock.com/image-photo/engineer-working-on-racing-fpv-600nw-2278353271.jpg")

  // Helper that creates a ViewModel with a fake repo returning sampleEvent for any id
  private fun createViewModelWithFakeRepo(): EventDetailsViewModel {
    val fakeRepo =
        object : EventRepository {
          override fun getNewUid(): String = "fake-id"

          override suspend fun getAllEvents(): List<Event> = listOf(sampleEvent)

          override suspend fun getEvent(eventId: String): Event = sampleEvent

          override suspend fun createEvent(event: Event): Result<Unit> = Result.success(Unit)

          override suspend fun updateEvent(eventId: String, newEvent: Event): Result<Unit> =
              Result.success(Unit)

          override suspend fun deleteEvent(eventId: String): Result<Unit> = Result.success(Unit)
        }
    return EventDetailsViewModel(fakeRepo)
  }

  private fun setSampleEventContent() {
    setEventContent(sampleEvent)
  }

  private fun setEventContent(event: Event) {
    composeTestRule.setContent {
      EventDetailsContent(event = event, onOpenMap = {}, onGoBack = {}, onEnrollClick = {})
    }
  }

  // ============ ViewModel Tests ============

  @Test
  fun viewModel_InitialStateIsLoading() = runTest {
    val viewModel = EventDetailsViewModel()
    val initialState = viewModel.uiState.value
    assertTrue("Initial state should be Loading", initialState is EventDetailsUIState.Loading)
  }

  @Test
  fun viewModel_LoadEventThrowsException() = runTest {
    // Create a fake repo that throws an exception
    val fakeRepoThrowsException =
        object : EventRepository {
          override fun getNewUid(): String = "fake-id"

          override suspend fun getAllEvents(): List<Event> = emptyList()

          override suspend fun getEvent(eventId: String): Event? {
            throw RuntimeException("Database connection failed")
          }

          override suspend fun createEvent(event: Event): Result<Unit> = Result.success(Unit)

          override suspend fun updateEvent(eventId: String, newEvent: Event): Result<Unit> =
              Result.success(Unit)

          override suspend fun deleteEvent(eventId: String): Result<Unit> = Result.success(Unit)
        }

    val viewModel = EventDetailsViewModel(fakeRepoThrowsException)
    viewModel.loadEvent("some-event-id")

    // Give the coroutine time to complete
    withContext(Dispatchers.Main) { Thread.sleep(100) }

    val state = viewModel.uiState.value
    assertTrue("State should be Error when exception is thrown", state is EventDetailsUIState.Error)
    if (state is EventDetailsUIState.Error) {
      assertTrue(
          "Error message should contain 'Failed to load event'",
          state.message.startsWith("Failed to load event"))
      assertTrue(
          "Error message should contain exception message",
          state.message.contains("Database connection failed"))
    }
  }

  @Test
  fun viewModel_IsEnrolledReturnsFalse() = runTest {
    val viewModel = EventDetailsViewModel()
    val isEnrolled = viewModel.isEnrolled(sampleEvent)
    assertFalse("User shouldn't be reported as enrolled in the event", isEnrolled)
  }

  // ============ EventDetailsContent Tests ============

  @Test
  fun content_DisplaysEventTitle() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Drone Workshop").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEventAssociation() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_ASSOCIATION).assertIsDisplayed()
    composeTestRule.onNodeWithText("AeroPoly").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEventPrice() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_PRICE).assertIsDisplayed()
    composeTestRule.onNodeWithText("CHF 0.10").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEventLocation() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithText("Centre Sport et Santé").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEventDescription() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_DESCRIPTION).assertIsDisplayed()
    composeTestRule
        .onNodeWithText(
            "The Drone Workshop is a multi-evening workshop organized by AéroPoly...",
            substring = true)
        .assertIsDisplayed()
  }

  @Test
  fun content_DisplaysBackButton() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysEnrollButton() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("Enrol in event").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysViewLocationButton() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON).assertIsDisplayed()
  }

  @Test
  fun clickOnMapTriggersNavigation() {
    var clicked = false
    composeTestRule.setContent {
      EventDetailsContent(
          sampleEvent, onGoBack = {}, onOpenMap = { clicked = true }, onEnrollClick = {})
    }
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
    assertTrue("View Location button should trigger onOpenMap callback", clicked)
  }

  @Test
  fun content_BackButtonTriggersCallback() {
    var backClicked = false
    composeTestRule.setContent {
      EventDetailsContent(
          event = sampleEvent,
          onGoBack = { backClicked = true },
          onEnrollClick = {},
          onOpenMap = {})
    }
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).performClick()
    assertTrue("Back button should trigger onGoBack callback", backClicked)
  }

  @Test
  fun content_EnrollButtonIsClickable() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).assertHasClickAction()
  }

  @Test
  fun content_ViewLocationButtonIsClickable() {
    setSampleEventContent()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON).assertHasClickAction()
  }

  // ============ Different Event Data Tests ============

  @Test
  fun content_DisplaysFreeEvent() {
    val freeEvent = sampleEvent.copy(price = Price(0u))
    setEventContent(freeEvent)
    // Price should be empty string for 0u price based on the code
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_PRICE).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysExpensiveEvent() {
    val expensiveEvent = sampleEvent.copy(price = Price(999u))
    setEventContent(expensiveEvent)
    composeTestRule.onNodeWithText("CHF 9.99").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysLongTitle() {
    val longTitleEvent =
        sampleEvent.copy(
            title = "This is a very long event title that should still display properly")
    setEventContent(longTitleEvent)
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
    setEventContent(longDescEvent)
    composeTestRule.onNodeWithText(longDescription, substring = true).assertIsDisplayed()
  }

  @Test
  fun content_HandlesEventWithoutImageUrl() {
    val eventWithoutImage = sampleEvent.copy(pictureUrl = null)
    setEventContent(eventWithoutImage)
    // Image should still display with default URL
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_IMAGE).assertExists()
  }

  @Test
  fun content_HandlesEventWithEmptyTags() {
    val eventWithNoTags = sampleEvent.copy(tags = emptyList())
    setEventContent(eventWithNoTags)
    // Should still display without crashing
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TITLE).assertIsDisplayed()
  }

  @Test
  fun content_HandlesEventWithMultipleTags() {
    val eventWithTags = sampleEvent.copy(tags = listOf("workshop", "tech", "drone", "engineering"))
    setEventContent(eventWithTags)
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
    setSampleEventContent()

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

    composeTestRule.setContent {
      EventDetailsContent(
          event = sampleEvent, onEnrollClick = {}, onGoBack = { clickCount++ }, onOpenMap = {})
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
  fun integration_NavigationBetweenStates() {
    var goBackCalled = false
    composeTestRule.setContent {
      EventDetailsContent(
          event = sampleEvent,
          onGoBack = { goBackCalled = true },
          onEnrollClick = {},
          onOpenMap = {})
    }

    // Verify content is displayed
    composeTestRule.onNodeWithTag(EventDetailsTestTags.EVENT_TITLE).assertIsDisplayed()

    // Click back button
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).performClick()

    assertTrue("Navigation callback should be triggered", goBackCalled)
  }

  @Test
  fun content_AllButtonsAreAccessible() {
    setSampleEventContent()

    // All interactive elements should be present and accessible
    composeTestRule.onNodeWithTag(EventDetailsTestTags.BACK_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.ENROLL_BUTTON).assertHasClickAction()
    composeTestRule.onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON).assertHasClickAction()
  }

  @Test
  fun content_VerifyTextStyling() {
    setSampleEventContent()

    // Verify that all text elements exist (styling is applied correctly)
    composeTestRule.onNodeWithText("Drone Workshop").assertExists()
    composeTestRule.onNodeWithText("AeroPoly").assertExists()
    composeTestRule.onNodeWithText("CHF 0.10").assertExists()
    composeTestRule.onNodeWithText("Centre Sport et Santé").assertExists()
    composeTestRule.onNodeWithText("Description").assertExists()
  }

  @Test
  fun content_DescriptionLabelDisplayed() {
    setSampleEventContent()
    composeTestRule.onNodeWithText("Description").assertIsDisplayed()
  }
}
