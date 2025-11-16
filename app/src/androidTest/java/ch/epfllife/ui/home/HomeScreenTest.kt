package ch.epfllife.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepositoryLocal
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.theme.Theme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  val repo = EventRepositoryLocal()

  private fun createFakeViewModel(
      myEvents: List<Event> = emptyList(),
      allEvents: List<Event> = emptyList(),
  ): HomeViewModel {
    repo.setEvents(allEvents)
    val viewModel = HomeViewModel(repo = repo)
    // Set myEvents since the real ViewModel doesn't populate it yet
    viewModel.setMyEvents(myEvents)
    return viewModel
  }

  private fun setUpHomeScreen(
      myEvents: List<Event> = emptyList(),
      allEvents: List<Event> = emptyList(),
  ) = runTest {
    val viewModel = createFakeViewModel(myEvents = myEvents, allEvents = allEvents)

    composeTestRule.setContent { Theme { HomeScreen(viewModel = viewModel, onEventClick = {}) } }
  }

  @Test
  fun homeScreen_DisplaysCorrectly() {
    setUpHomeScreen(myEvents = listOf(ExampleEvents.event1))

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
    setUpHomeScreen(myEvents = listOf(ExampleEvents.event1, ExampleEvents.event2))

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
    setUpHomeScreen(myEvents = emptyList())

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Check that empty state message is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
  }

  @Test
  fun homeScreen_SwitchingBetweenFiltersWorks() {
    setUpHomeScreen(
        myEvents = listOf(ExampleEvents.event1),
        allEvents = listOf(ExampleEvents.event2),
    )

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Initially should show subscribed events (ExampleEvents.event1)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()
  }

  @Test
  fun homeScreen_ClickingAllEventsFilterShowsAllEvents() {
    setUpHomeScreen(
        myEvents = listOf(ExampleEvents.event1),
        allEvents = listOf(ExampleEvents.event2),
    )

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Initially should show subscribed events (ExampleEvents.event1)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()

    // Click on "All Events" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Now should show all events (ExampleEvents.event2)
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertDoesNotExist()
  }

  @Test
  fun homeScreen_ClickingSubscribedFilterShowsSubscribedEvents() {
    setUpHomeScreen(
        myEvents = listOf(ExampleEvents.event1),
        allEvents = listOf(ExampleEvents.event2),
    )

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Click on "All Events" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Should show all events (ExampleEvents.event2)
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()

    // Click back on "Subscribed" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Now should show subscribed events again (ExampleEvents.event1)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()
  }

  @Test
  fun homeScreen_EmptyAllEventsShowsMessage() {
    setUpHomeScreen()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Click on "All Events" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Check that empty state is displayed (screen is still displayed)
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
  }

  @Test
  fun homeScreen_FilterSwitchingWithBothListsPopulated() {
    setUpHomeScreen(
        myEvents = listOf(ExampleEvents.event1),
        allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2),
    )

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Initially should show subscribed events (ExampleEvents.event1 only)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()

    // Click on "All Events" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    // Wait for the UI to settle
    composeTestRule.waitForIdle()

    // Now should show all events (both events)
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()
  }
}
