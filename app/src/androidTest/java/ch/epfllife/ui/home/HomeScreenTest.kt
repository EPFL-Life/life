package ch.epfllife.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.Association
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
import org.junit.Assert.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test


class HomeScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var db: Db

  private fun setUpDb(
      myEvents: List<Event> = emptyList(),
      allEvents: List<Event> = emptyList(),
      subscribedAssociations: List<Association> = emptyList()
  ) = runTest {
    val combinedEvents = (allEvents + myEvents).distinctBy { it.id }
    val eventRepo = EventRepositoryLocal().apply { seedEvents(combinedEvents) }
    val assocRepo =
        AssociationRepositoryLocal(eventRepo).apply {
          subscribedAssociations.forEach { assoc -> createAssociation(assoc) }
        }
    val userRepo =
        UserRepositoryLocal(eventRepo, assocRepo).apply {
          this.createUser(ExampleUsers.user1)
          this.simulateLogin(ExampleUsers.user1.id)
          myEvents.forEach { event ->
            if (eventRepo.getEvent(event.id) == null) {
              eventRepo.createEvent(event)
            }
            this.subscribeToEvent(event.id)
          }
          subscribedAssociations.forEach { assoc -> this.subscribeToAssociation(assoc.id) }
        }
    db = Db(userRepo, eventRepo, assocRepo)
  }

  private fun setUpHomeScreen(
      myEvents: List<Event> = emptyList(),
      allEvents: List<Event> = emptyList(),
      subscribedAssociations: List<Association> = emptyList()
  ) {
    setUpDb(myEvents, allEvents, subscribedAssociations)
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

  @Test
  fun homeScreen_subscribedFilterShowsEnrolledAndSubscribedAssocEvents() {
    setUpHomeScreen(
        myEvents = listOf(ExampleEvents.event1),
        allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2),
        subscribedAssociations = listOf(ExampleAssociations.association2))
    composeTestRule.waitForIdle()

    val enrolledTag = EventCardTestTags.getEventCardTestTag(ExampleEvents.event1.id)
    // event 2 is Association's 2 created event
    val notEnrolledAssociationEvent = EventCardTestTags.getEventCardTestTag(ExampleEvents.event2.id)

    composeTestRule.onNodeWithTag(enrolledTag).assertIsDisplayed()
    composeTestRule.onNodeWithTag(notEnrolledAssociationEvent).assertIsDisplayed()

    // the enrolled events should appear before the association event you are NOT enrolled
    val enrolledBounds = composeTestRule.onNodeWithTag(enrolledTag).getUnclippedBoundsInRoot()
    val assocBounds =
        composeTestRule.onNodeWithTag(notEnrolledAssociationEvent).getUnclippedBoundsInRoot()
    assertTrue(enrolledBounds.top <= assocBounds.top)
  }

  @Test
  fun homeScreen_subscribedFilterExcludesNonSubscribedAssocEvents() {
    val eventEnrolled = ExampleEvents.event1
    val eventNotRelevant = ExampleEvents.event3

    setUpHomeScreen(
        myEvents = listOf(eventEnrolled),
        allEvents = ExampleEvents.allEvents,
        subscribedAssociations = listOf(ExampleAssociations.association2))

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(eventEnrolled.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(eventNotRelevant.title).assertDoesNotExist()
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_ALL).performClick()
    composeTestRule.onNodeWithText(eventNotRelevant.title).assertIsDisplayed()
  }


  @Test
  fun homeScreen_subscribedFilterUpdatesAfterSubscribingToAssociation() {
    setUpHomeScreen(
      myEvents = listOf(ExampleEvents.event1),
      allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2),
      subscribedAssociations = emptyList()
    )

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()
    composeTestRule.waitForIdle()

    val assocTag = EventCardTestTags.getEventCardTestTag(ExampleEvents.event2.id)
    composeTestRule.onNodeWithTag(assocTag).assertDoesNotExist()

    runTest {
      db.assocRepo.createAssociation(ExampleAssociations.association2)
      db.userRepo.subscribeToAssociation(ExampleAssociations.association2.id)
    }

    composeTestRule.triggerRefresh(EventCardTestTags.getEventCardTestTag(ExampleEvents.event1.id))
    composeTestRule.onNodeWithTag(assocTag).assertIsDisplayed()
  }

  @Test
  fun homeScreen_subscribedFilterOrderWithMultipleEnrolled() {
    setUpHomeScreen(
      myEvents = listOf(ExampleEvents.event1, ExampleEvents.event3),
      allEvents = ExampleEvents.allEvents,
      subscribedAssociations = listOf(ExampleAssociations.association2)
    )

    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()
    composeTestRule.waitForIdle()

    val tag1 = EventCardTestTags.getEventCardTestTag(ExampleEvents.event1.id)
    val tag3 = EventCardTestTags.getEventCardTestTag(ExampleEvents.event3.id)
    val tagAssoc = EventCardTestTags.getEventCardTestTag(ExampleEvents.event2.id)


    val b1 = composeTestRule.onNodeWithTag(tag1).getUnclippedBoundsInRoot()
    val b3 = composeTestRule.onNodeWithTag(tag3).getUnclippedBoundsInRoot()
    val assocB = composeTestRule.onNodeWithTag(tagAssoc).getUnclippedBoundsInRoot()

    assertTrue(b1.top <= assocB.top)
    assertTrue(b3.top <= assocB.top)
  }

  @Test
  fun homeScreen_associationEventsNotAboveEnrolled() {
    setUpHomeScreen(
      myEvents = listOf(ExampleEvents.event1, ExampleEvents.event3),
      allEvents = listOf(ExampleEvents.event1, ExampleEvents.event2, ExampleEvents.event3),
      subscribedAssociations = listOf(ExampleAssociations.association2)
    )

    composeTestRule.onNodeWithTag(DisplayedEventsTestTags.BUTTON_SUBSCRIBED).performClick()
    composeTestRule.waitForIdle()

    val enrolledTags = listOf(
      EventCardTestTags.getEventCardTestTag(ExampleEvents.event1.id),
      EventCardTestTags.getEventCardTestTag(ExampleEvents.event3.id)
    )
    val assocTag = EventCardTestTags.getEventCardTestTag(ExampleEvents.event2.id)


    val enrolledTops = enrolledTags.map { tag ->
      composeTestRule.onNodeWithTag(tag).assertIsDisplayed()
      composeTestRule.onNodeWithTag(tag).getUnclippedBoundsInRoot().top
    }

    val maxEnrolledTop = enrolledTops.maxOrNull() ?: 0.dp

    composeTestRule.onNodeWithTag(assocTag).assertIsDisplayed()
    val assocTop = composeTestRule.onNodeWithTag(assocTag).getUnclippedBoundsInRoot().top

    assertTrue(
      assocTop >= maxEnrolledTop
    )
  }
}
