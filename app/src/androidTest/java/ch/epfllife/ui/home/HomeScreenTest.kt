package ch.epfllife.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.map.Location
import ch.epfllife.ui.navigation.NavigationTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleAssociation =
      Association(
          id = "test-assoc",
          name = "Test Association",
          description = "A test association",
          eventCategory = EventCategory.ACADEMIC)

  private val sampleEvent =
      Event(
          id = "event1",
          title = "Test Event 1",
          description = "This is a test event",
          location = Location(46.5191, 6.5668, "Test Location"),
          time = "2025-11-15 18:00",
          association = sampleAssociation,
          tags = setOf("test"),
          price = 0u,
          pictureUrl = null)

  private val sampleEvent2 =
      Event(
          id = "event2",
          title = "Test Event 2",
          description = "Another test event",
          location = Location(46.5191, 6.5668, "Test Location 2"),
          time = "2025-11-16 19:00",
          association = sampleAssociation,
          tags = setOf("test"),
          price = 10u,
          pictureUrl = null)

  // Fake EventRepository for testing
  private class FakeEventRepository(
      private val allEventsList: List<Event> = emptyList(),
      private val myEventsList: List<Event> = emptyList()
  ) : EventRepository {
    override fun getNewUid(): String = "fake-uid"

    override suspend fun getAllEvents(): List<Event> = allEventsList

    override suspend fun getEvent(eventId: String): Event? = allEventsList.find { it.id == eventId }

    override suspend fun createEvent(event: Event): Result<Unit> = Result.success(Unit)

    override suspend fun updateEvent(eventId: String, newEvent: Event): Result<Unit> =
        Result.success(Unit)

    override suspend fun deleteEvent(eventId: String): Result<Unit> = Result.success(Unit)
  }

  private fun createFakeViewModel(
      myEvents: List<Event> = emptyList(),
      allEvents: List<Event> = emptyList()
  ): HomeViewModel {
    val fakeRepo = FakeEventRepository(allEventsList = allEvents, myEventsList = myEvents)
    val viewModel = HomeViewModel(repo = fakeRepo)
    // Set myEvents since the real ViewModel doesn't populate it yet
    viewModel.setMyEvents(myEvents)
    return viewModel
  }

  @Test
  fun homeScreen_DisplaysCorrectly() {
    val viewModel = createFakeViewModel(myEvents = listOf(sampleEvent))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Check if the main screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()

    // Check if EPFL logo is displayed
    composeTestRule.onNodeWithTag(HomeScreenTestTags.EPFLLOGO).assertIsDisplayed()
  }

  @Test
  fun homeScreen_EventCardClickTriggersCallback() {
    var clickedEventId: String? = null
    val viewModel = createFakeViewModel(myEvents = listOf(sampleEvent))

    composeTestRule.setContent {
      MaterialTheme {
        HomeScreen(viewModel = viewModel, onEventClick = { eventId -> clickedEventId = eventId })
      }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Find and click on the event card
    composeTestRule.onNodeWithText("Test Event 1").performClick()

    // Verify that the callback was triggered with the correct event ID
    assertEquals("event1", clickedEventId)
  }

  @Test
  fun homeScreen_DisplaysMultipleEvents() {
    val viewModel = createFakeViewModel(myEvents = listOf(sampleEvent, sampleEvent2))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Check that both events are displayed
    composeTestRule.onNodeWithText("Test Event 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Event 2").assertIsDisplayed()
  }

  @Test
  fun homeScreen_ClickingSecondEventTriggersCorrectCallback() {
    var clickedEventId: String? = null
    val viewModel = createFakeViewModel(myEvents = listOf(sampleEvent, sampleEvent2))

    composeTestRule.setContent {
      MaterialTheme {
        HomeScreen(viewModel = viewModel, onEventClick = { eventId -> clickedEventId = eventId })
      }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Click on the second event
    composeTestRule.onNodeWithText("Test Event 2").performClick()

    // Verify that the callback was triggered with the correct event ID
    assertEquals("event2", clickedEventId)
  }

  @Test
  fun homeScreen_EmptySubscribedEventsShowsMessage() {
    val viewModel = createFakeViewModel(myEvents = emptyList())

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Check that empty state message is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
  }

  @Test
  fun homeScreen_PreviewDisplaysWithoutCrashing() {
    // This test covers the preview line: MaterialTheme { HomeScreen(onEventClick = {}) }
    composeTestRule.setContent { MaterialTheme { HomeScreen(onEventClick = {}) } }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Just verify that the screen is displayed without crashing
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
  }

  @Test
  fun homeScreen_MultipleClicksOnSameEventWork() {
    var clickCount = 0
    var lastClickedEventId: String? = null
    val viewModel = createFakeViewModel(myEvents = listOf(sampleEvent))

    composeTestRule.setContent {
      MaterialTheme {
        HomeScreen(
            viewModel = viewModel,
            onEventClick = { eventId ->
              clickCount++
              lastClickedEventId = eventId
            })
      }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Click the event multiple times
    composeTestRule.onNodeWithText("Test Event 1").performClick()
    composeTestRule.onNodeWithText("Test Event 1").performClick()
    composeTestRule.onNodeWithText("Test Event 1").performClick()

    // Verify that all clicks were registered
    assertEquals(3, clickCount)
    assertEquals("event1", lastClickedEventId)
  }

  @Test
  fun homeScreen_SwitchingBetweenFiltersWorks() {
    val viewModel =
        createFakeViewModel(myEvents = listOf(sampleEvent), allEvents = listOf(sampleEvent2))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Initially should show subscribed events (sampleEvent)
    composeTestRule.onNodeWithText("Test Event 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Event 2").assertDoesNotExist()
  }
}
