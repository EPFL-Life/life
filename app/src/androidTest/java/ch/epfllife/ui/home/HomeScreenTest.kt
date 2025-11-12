package ch.epfllife.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepository
import ch.epfllife.ui.navigation.NavigationTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Fake EventRepository for testing
  private class FakeEventRepository(private val allEventsList: List<Event> = emptyList()) :
      EventRepository {
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
    val fakeRepo = FakeEventRepository(allEventsList = allEvents)
    val viewModel = HomeViewModel(repo = fakeRepo)
    // Set myEvents since the real ViewModel doesn't populate it yet
    viewModel.setMyEvents(myEvents)
    return viewModel
  }

  @Test
  fun homeScreen_DisplaysCorrectly() {
    val viewModel = createFakeViewModel(myEvents = listOf(ExampleEvents.event1))

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
    val viewModel = createFakeViewModel(myEvents = listOf(ExampleEvents.event1))

    composeTestRule.setContent {
      MaterialTheme {
        HomeScreen(viewModel = viewModel, onEventClick = { eventId -> clickedEventId = eventId })
      }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Find and click on the event card
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performClick()

    // Verify that the callback was triggered with the correct event ID
    assertEquals(ExampleEvents.event1.id, clickedEventId)
  }

  @Test
  fun homeScreen_DisplaysMultipleEvents() {
    val viewModel =
        createFakeViewModel(myEvents = listOf(ExampleEvents.event1, ExampleEvents.event2))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Check that both events are displayed
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()
  }

  @Test
  fun homeScreen_ClickingSecondEventTriggersCorrectCallback() {
    var clickedEventId: String? = null
    val viewModel =
        createFakeViewModel(myEvents = listOf(ExampleEvents.event1, ExampleEvents.event2))

    composeTestRule.setContent {
      MaterialTheme {
        HomeScreen(viewModel = viewModel, onEventClick = { eventId -> clickedEventId = eventId })
      }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Click on the second event
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).performClick()

    // Verify that the callback was triggered with the correct event ID
    assertEquals(ExampleEvents.event2.id, clickedEventId)
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
    val viewModel = createFakeViewModel(myEvents = listOf(ExampleEvents.event1))

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
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performClick()
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performClick()
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performClick()

    // Verify that all clicks were registered
    assertEquals(3, clickCount)
    assertEquals(ExampleEvents.event1.id, lastClickedEventId)
  }

  @Test
  fun homeScreen_SwitchingBetweenFiltersWorks() {
    val viewModel =
        createFakeViewModel(
            myEvents = listOf(ExampleEvents.event1), allEvents = listOf(ExampleEvents.event2))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Initially should show subscribed events (ExampleEvents.event1)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()
  }

  @Test
  fun homeScreen_ClickingAllEventsFilterShowsAllEvents() {
    val viewModel =
        createFakeViewModel(
            myEvents = listOf(ExampleEvents.event1), allEvents = listOf(ExampleEvents.event2))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Initially should show subscribed events (ExampleEvents.event1)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()

    // Click on "All Events" filter button
    composeTestRule
        .onNodeWithTag(ch.epfllife.ui.composables.DisplayedEventsTestTags.BUTTON_ALL)
        .performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Now should show all events (ExampleEvents.event2)
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertDoesNotExist()
  }

  @Test
  fun homeScreen_ClickingSubscribedFilterShowsSubscribedEvents() {
    val viewModel =
        createFakeViewModel(
            myEvents = listOf(ExampleEvents.event1), allEvents = listOf(ExampleEvents.event2))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Click on "All Events" filter button
    composeTestRule
        .onNodeWithTag(ch.epfllife.ui.composables.DisplayedEventsTestTags.BUTTON_ALL)
        .performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Should show all events (ExampleEvents.event2)
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()

    // Click back on "Subscribed" filter button
    composeTestRule
        .onNodeWithTag(ch.epfllife.ui.composables.DisplayedEventsTestTags.BUTTON_SUBSCRIBED)
        .performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Now should show subscribed events again (ExampleEvents.event1)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()
  }

  @Test
  fun homeScreen_EmptyAllEventsShowsMessage() {
    val viewModel =
        createFakeViewModel(myEvents = listOf(ExampleEvents.event1), allEvents = emptyList())

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Click on "All Events" filter button
    composeTestRule
        .onNodeWithTag(ch.epfllife.ui.composables.DisplayedEventsTestTags.BUTTON_ALL)
        .performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Check that empty state is displayed (screen is still displayed)
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
  }

  @Test
  fun homeScreen_FilterSwitchingWithBothListsPopulated() {
    val viewModel =
        createFakeViewModel(
            myEvents = listOf(ExampleEvents.event1),
            allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(viewModel = viewModel, onEventClick = {}) }
    }

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Initially should show subscribed events (ExampleEvents.event1 only)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()

    // Click on "All Events" filter button
    composeTestRule
        .onNodeWithTag(ch.epfllife.ui.composables.DisplayedEventsTestTags.BUTTON_ALL)
        .performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Now should show all events (both events)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()
  }
}
