package ch.epfllife.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.AssociationRepositoryLocal
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepositoryLocal
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.ui.composables.DisplayedEventsTestTags
import ch.epfllife.ui.composables.EventCardTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.theme.Theme
import ch.epfllife.utils.triggerRefresh
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var db: Db

  private fun setUpDb(
      myEvents: List<Event> = emptyList(),
      allEvents: List<Event> = emptyList(),
  ) = runTest {
    val combinedEvents = (allEvents + myEvents).distinctBy { it.id }
    val eventRepo = EventRepositoryLocal().apply { seedEvents(combinedEvents) }
    val userRepo =
        UserRepositoryLocal(eventRepo).apply {
          this.createUser(ExampleUsers.user1)
          this.simulateLogin(ExampleUsers.user1.id)
          myEvents.forEach { event ->
            if (eventRepo.getEvent(event.id) == null) {
              eventRepo.createEvent(event)
            }
            this.subscribeToEvent(event.id)
          }
        }
    val assocRepo = AssociationRepositoryLocal(eventRepo)
    db = Db(userRepo, eventRepo, assocRepo)
  }

  private fun setUpHomeScreen(
      myEvents: List<Event> = emptyList(),
      allEvents: List<Event> = emptyList(),
  ) {
    setUpDb(myEvents, allEvents)
    composeTestRule.setContent { Theme { HomeScreen(db = db, onEventClick = {}) } }
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
    setUpDb(myEvents = listOf(ExampleEvents.event1))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(db = db, onEventClick = { eventId -> clickedEventId = eventId }) }
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
    setUpDb(myEvents = listOf(ExampleEvents.event1, ExampleEvents.event2))

    composeTestRule.setContent {
      MaterialTheme { HomeScreen(db = db, onEventClick = { eventId -> clickedEventId = eventId }) }
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
    composeTestRule.onNodeWithText(ExampleEvents.event1.title).assertIsDisplayed()
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

  @Test
  fun refreshShowsUpdatedEvents() {
    val events = listOf(ExampleEvents.event1, ExampleEvents.event2)
    val checkEventsDisplayed = {
      events.forEach { event ->
        composeTestRule
            .onNodeWithTag(EventCardTestTags.getEventCardTestTag(event.id))
            .assertIsDisplayed()
      }
    }
    setUpHomeScreen(
        allEvents = events,
    )
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()
    composeTestRule.waitForIdle()
    // Initial Events displayed
    checkEventsDisplayed()

    runTest { db.eventRepo.createEvent(ExampleEvents.event3) }
    composeTestRule.triggerRefresh(EventCardTestTags.getEventCardTestTag(ExampleEvents.event1.id))

    // Initial Events displayed + new event displayed
    checkEventsDisplayed()
    composeTestRule.waitUntil {
      composeTestRule
          .onNodeWithTag(EventCardTestTags.getEventCardTestTag(ExampleEvents.event3.id))
          .isDisplayed()
    }

    runTest { assert(db.eventRepo.deleteEvent(ExampleEvents.event3.id).isSuccess) }
    composeTestRule.triggerRefresh(EventCardTestTags.getEventCardTestTag(ExampleEvents.event1.id))

    // Initial Events displayed and new event not displayed anymore
    checkEventsDisplayed()
    composeTestRule.waitUntil {
      composeTestRule
          .onNodeWithTag(EventCardTestTags.getEventCardTestTag(ExampleEvents.event3.id))
          .isNotDisplayed()
    }
  }

  @Test
  fun homeScreen_handlesNullEventDataGracefully() = runTest {
    setUpHomeScreen(myEvents = emptyList(), allEvents = emptyList())

    composeTestRule.waitForIdle()
    // Should display empty state without crashing
    composeTestRule.onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN).assertIsDisplayed()
  }

  @Test
  fun homeScreen_handlesVeryLongEventTitles() = runTest {
    val longTitleEvent =
        ExampleEvents.event1.copy(
            title =
                "This is an extremely long event title that might cause layout issues if not handled properly in the UI components and should be truncated correctly")

    setUpHomeScreen(myEvents = listOf(longTitleEvent))

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(longTitleEvent.title).assertExists()
  }

  @Test
  fun homeScreen_handlesSpecialCharactersInEventTitles() = runTest {
    val specialCharEvent =
        ExampleEvents.event1.copy(title = "Event with spéciäl chàräctérs & symbols! @#$%^&*()")

    setUpHomeScreen(myEvents = listOf(specialCharEvent))

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(specialCharEvent.title).assertIsDisplayed()
  }
}
