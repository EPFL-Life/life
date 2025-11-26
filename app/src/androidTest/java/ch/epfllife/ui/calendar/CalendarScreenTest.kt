package ch.epfllife.ui.calendar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.event.EventRepositoryLocal
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.home.HomeViewModel
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.theme.Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {

  private fun setUpCalendarScreen(
      allEvents: List<ch.epfllife.model.event.Event> = emptyList(),
      enrolledEvents: List<ch.epfllife.model.event.Event> = emptyList(),
      onEventClick: (String) -> Unit = {}
  ) {
    val repo = EventRepositoryLocal()
    repo.seedEvents(allEvents)
    composeTestRule.setContent {
      Theme {
        CalendarScreen(
            viewModel = viewModel { HomeViewModel(repo).also { it.setMyEvents(enrolledEvents) } },
            onEventClick = onEventClick)
      }
    }
    composeTestRule.waitForIdle()
  }

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun calendarScreen_DisplaysCorrectly() {
    setUpCalendarScreen(
        allEvents = listOf(ExampleEvents.event1), enrolledEvents = listOf(ExampleEvents.event1))

    composeTestRule.waitForIdle()

    // Check if the main screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.CALENDAR_SCREEN).assertIsDisplayed()

    // Check if the event is displayed
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
  }

  @Test
  fun calendarScreen_EventCardClickTriggersCallback() {

    var clickedEventId: String? = null

    setUpCalendarScreen(
        allEvents = listOf(ExampleEvents.event1),
        enrolledEvents = listOf(ExampleEvents.event1),
        onEventClick = { eventId -> clickedEventId = eventId })

    // Find and click on the event card
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performClick()

    // Verify that the callback was triggered with the correct event ID
    assertEquals(ExampleEvents.event1.id, clickedEventId)
  }

  @Test
  fun calendarScreen_FilterSwitchingWorks() {
    var clickedEventId: String? = null
    setUpCalendarScreen(
        allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2),
        enrolledEvents = listOf(ExampleEvents.event1),
        onEventClick = { eventId -> clickedEventId = eventId })

    composeTestRule.waitForIdle()
    Thread.sleep(1000)

    // Initially should show subscribed events
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()

    // Click on "All Events" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    Thread.sleep(1000)
    composeTestRule.waitForIdle()

    // Now should show all events
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).performScrollTo().assertIsDisplayed()
    // Click back on "Subscribed" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()

    Thread.sleep(1000)
    composeTestRule.waitForIdle()

    // Should show only subscribed events again
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()
  }

  @Test
  fun calendarScreen_EmptyStateShowsMessage() {
    setUpCalendarScreen(allEvents = emptyList(), enrolledEvents = emptyList())

    composeTestRule.waitForIdle()

    // Check that the screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.CALENDAR_SCREEN).assertIsDisplayed()

    // Check that no events are displayed
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertDoesNotExist()
  }

  @Test
  fun calendarScreen_DisplaysMultipleEventsFromDifferentMonths() {
    setUpCalendarScreen(
        allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2, ExampleEvents.event3),
        enrolledEvents = listOf(ExampleEvents.event1, ExampleEvents.event2, ExampleEvents.event3))

    composeTestRule.waitForIdle()

    // Check that all events are displayed
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event3.title).assertIsDisplayed()

    // Check that month headers are displayed (events are from different months)
    // Since events span multiple months, there should be multiple month headers
    val monthHeaders = composeTestRule.onAllNodesWithTag(CalendarTestTags.MONTH_HEADER)
    monthHeaders.onFirst().assertIsDisplayed()
    org.junit.Assert.assertTrue(
        "Should have multiple month headers for events from different months",
        monthHeaders.fetchSemanticsNodes().size >= 2)
  }

  @Test
  fun calendarScreen_handlesEventsWithInvalidDateFormat() {
    val invalidDateEvent = ExampleEvents.event1.copy(time = "invalid-date-format")

    setUpCalendarScreen(
        allEvents = listOf(invalidDateEvent), enrolledEvents = listOf(invalidDateEvent))

    composeTestRule.waitForIdle()
    // Should not crash with invalid date format
    composeTestRule.onNodeWithTag(NavigationTestTags.CALENDAR_SCREEN).assertIsDisplayed()
  }

  @Test
  fun calendarScreen_handlesEventsSpanningMultipleMonths() {
    val multiMonthEvents =
        listOf(
            ExampleEvents.event1.copy(time = "2025-01-15T09:00:00/2025-01-20T18:00:00"),
            ExampleEvents.event2.copy(time = "2025-02-01T10:00:00/2025-02-28T17:00:00"),
            ExampleEvents.event3.copy(time = "2025-03-10T14:00:00/2025-03-12T16:00:00"))

    setUpCalendarScreen(allEvents = multiMonthEvents, enrolledEvents = multiMonthEvents)

    composeTestRule.waitForIdle()
    // Should group events by month correctly
    val monthHeaders = composeTestRule.onAllNodesWithTag(CalendarTestTags.MONTH_HEADER)
    assertEquals(
        "Should have exactly 3 month headers for 3 different months",
        3,
        monthHeaders.fetchSemanticsNodes().size)
  }

  @Test
  fun calendarScreen_handlesEmptyEventListsInBothFilters() {
    setUpCalendarScreen(allEvents = emptyList(), enrolledEvents = emptyList())

    composeTestRule.waitForIdle()
    // Should display empty state for both filters
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()
    composeTestRule.waitForIdle()
  }
}
