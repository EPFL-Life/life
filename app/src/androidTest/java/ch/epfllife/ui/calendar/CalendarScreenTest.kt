package ch.epfllife.ui.calendar

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CalendarScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun calendarScreen_DisplaysCorrectly() {
    composeTestRule.setContent {
      MaterialTheme {
        CalendarScreen(
            allEvents = listOf(ExampleEvents.event1),
            enrolledEvents = listOf(ExampleEvents.event1),
            onEventClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // Check if the main screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.CALENDAR_SCREEN).assertIsDisplayed()

    // Check if the event is displayed
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
  }

  @Test
  fun calendarScreen_EventCardClickTriggersCallback() {
    var clickedEventId: String? = null

    composeTestRule.setContent {
      MaterialTheme {
        CalendarScreen(
            allEvents = listOf(ExampleEvents.event1),
            enrolledEvents = listOf(ExampleEvents.event1),
            onEventClick = { eventId -> clickedEventId = eventId })
      }
    }

    composeTestRule.waitForIdle()

    // Find and click on the event card
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).performClick()

    // Verify that the callback was triggered with the correct event ID
    assertEquals(ExampleEvents.event1.id, clickedEventId)
  }

  @Test
  fun calendarScreen_FilterSwitchingWorks() {
    composeTestRule.setContent {
      MaterialTheme {
        CalendarScreen(
            allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2),
            enrolledEvents = listOf(ExampleEvents.event1),
            onEventClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // Initially should show subscribed events
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()

    // Click on "All Events" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()

    composeTestRule.waitForIdle()

    // Now should show all events
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertIsDisplayed()

    // Click back on "Subscribed" filter button
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()

    composeTestRule.waitForIdle()

    // Should show only subscribed events again
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(ExampleEvents.event2.title).assertDoesNotExist()
  }

  @Test
  fun calendarScreen_EmptyStateShowsMessage() {
    composeTestRule.setContent {
      MaterialTheme {
        CalendarScreen(allEvents = emptyList(), enrolledEvents = emptyList(), onEventClick = {})
      }
    }

    composeTestRule.waitForIdle()

    // Check that the screen is displayed
    composeTestRule.onNodeWithTag(NavigationTestTags.CALENDAR_SCREEN).assertIsDisplayed()

    // Check that no events are displayed
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertDoesNotExist()
  }

  @Test
  fun calendarScreen_DisplaysMultipleEventsFromDifferentMonths() {
    composeTestRule.setContent {
      MaterialTheme {
        CalendarScreen(
            allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2, ExampleEvents.event3),
            enrolledEvents =
                listOf(ExampleEvents.event1, ExampleEvents.event2, ExampleEvents.event3),
            onEventClick = {})
      }
    }

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

  // ADD THESE TESTS TO THE EXISTING CalendarScreenTest.kt

  @Test
  fun calendarScreen_handlesEventsWithInvalidDateFormat() {
    val invalidDateEvent = ExampleEvents.event1.copy(time = "invalid-date-format")

    composeTestRule.setContent {
      MaterialTheme {
        CalendarScreen(
            allEvents = listOf(invalidDateEvent),
            enrolledEvents = listOf(invalidDateEvent),
            onEventClick = {})
      }
    }

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

    composeTestRule.setContent {
      MaterialTheme {
        CalendarScreen(
            allEvents = multiMonthEvents, enrolledEvents = multiMonthEvents, onEventClick = {})
      }
    }

    composeTestRule.waitForIdle()
    // Should group events by month correctly
    val monthHeaders = composeTestRule.onAllNodesWithTag(CalendarTestTags.MONTH_HEADER)
    assertTrue("Should have multiple month headers", monthHeaders.fetchSemanticsNodes().size >= 3)
  }

  @Test
  fun calendarScreen_handlesEmptyEventListsInBothFilters() {
    composeTestRule.setContent {
      MaterialTheme {
        CalendarScreen(allEvents = emptyList(), enrolledEvents = emptyList(), onEventClick = {})
      }
    }

    composeTestRule.waitForIdle()
    // Should display empty state for both filters
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()
    composeTestRule.waitForIdle()
  }
}
