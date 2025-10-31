package ch.epfllife.ui.composables

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.model.association.Association
import ch.epfllife.model.enums.SubscriptionFilter
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.map.Location
import ch.epfllife.utils.assertClickable
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ComposablesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ============ EventCard Tests ============

    private val sampleEvent =
        Event(
            id = "event1",
            title = "Sample Event",
            description = "This is a sample event",
            location = Location(46.520278, 6.565556, "EPFL"),
            time = "2024-03-15 14:00",
            association = Association(
                name = "TestAssociation",
                id = "gejn82",
                description = "This is a test", eventCategory = EventCategory.ACADEMIC
            ),
            tags = emptySet(),
            price = 0u,
        )

    @Test
    fun eventCard_IsClickable() {
        composeTestRule.assertClickable(
            { clickHandler -> EventCard(sampleEvent, onClick = clickHandler) },
            EventCardTestTags.EVENT_CARD,
        )
    }

    @Test
    fun eventCard_DisplaysEventTitle() {
        composeTestRule.setContent { EventCard(sampleEvent) }
        composeTestRule.onNodeWithText("Sample Event").assertIsDisplayed()
    }

    @Test
    fun eventCard_DisplaysFreePrice() {
        val freeEvent = sampleEvent.copy(price = 0u)
        composeTestRule.setContent { EventCard(freeEvent) }
        composeTestRule.onNodeWithText("Free").assertIsDisplayed()
    }

    @Test
    fun eventCard_DisplaysPaidPrice() {
        val paidEvent = sampleEvent.copy(price = 25u)
        composeTestRule.setContent { EventCard(paidEvent) }
        composeTestRule.onNodeWithText("CHF 25").assertIsDisplayed()
    }

    @Test
    fun eventCard_DisplaysAssociationId() {
        composeTestRule.setContent { EventCard(sampleEvent) }
        composeTestRule.onNodeWithText("TestAssociation").assertIsDisplayed()
    }

    @Test
    fun eventCard_DisplaysLocationName() {
        composeTestRule.setContent { EventCard(sampleEvent) }
        composeTestRule.onNodeWithText("EPFL").assertIsDisplayed()
    }

    @Test
    fun eventCard_DisplaysEventTime() {
        composeTestRule.setContent { EventCard(sampleEvent) }
        composeTestRule.onNodeWithText("2024-03-15 14:00").assertIsDisplayed()
    }

    @Test
    fun eventCard_TriggersOnClick() {
        var clicked = false
        composeTestRule.setContent { EventCard(sampleEvent, onClick = { clicked = true }) }
        composeTestRule.onNodeWithTag(EventCardTestTags.EVENT_CARD).performClick()
        assertTrue("EventCard should trigger onClick callback", clicked)
    }

    @Test
    fun eventCard_DisplaysMultipleEvents() {
        val event1 = sampleEvent.copy(id = "1", title = "Event One")
        val event2 = sampleEvent.copy(id = "2", title = "Event Two", price = 10u)

        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                EventCard(event1)
                EventCard(event2)
            }
        }

        composeTestRule.onNodeWithText("Event One").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Two").assertIsDisplayed()
        composeTestRule.onNodeWithText("Free").assertIsDisplayed()
        composeTestRule.onNodeWithText("CHF 10").assertIsDisplayed()
    }

    @Test
    fun eventCard_DisplaysCorrectlyWithLongTitle() {
        val longTitleEvent =
            sampleEvent.copy(title = "This is a very long event title that should still display")
        composeTestRule.setContent { EventCard(longTitleEvent) }
        composeTestRule
            .onNodeWithText("This is a very long event title that should still display")
            .assertIsDisplayed()
    }

    @Test
    fun eventCard_DisplaysCorrectlyWithLongLocationName() {
        val longLocationEvent =
            sampleEvent.copy(
                location = Location(
                    46.520278,
                    6.565556,
                    "EPFL - Rolex Learning Center"
                )
            )
        composeTestRule.setContent { EventCard(longLocationEvent) }
        composeTestRule.onNodeWithText("EPFL - Rolex Learning Center").assertIsDisplayed()
    }

    // ============ SearchBar Tests ============

    @Test
    fun searchBar_IsDisplayed() {
        composeTestRule.setContent { SearchBar() }
        composeTestRule.onNode(hasContentDescription("Search")).assertIsDisplayed()
    }

    @Test
    fun searchBar_DisplaysSearchIcon() {
        composeTestRule.setContent { SearchBar() }
        composeTestRule.onNode(hasContentDescription("Search")).assertExists()
    }

    @Test
    fun searchBar_DisplaysFilterIcon() {
        composeTestRule.setContent { SearchBar() }
        composeTestRule.onNode(hasContentDescription("Filter")).assertExists()
    }

    @Test
    fun searchBar_TriggersOnSearchClick() {
        var searchClicked = false
        composeTestRule.setContent { SearchBar(onSearchClick = { searchClicked = true }) }
        composeTestRule.onNode(hasContentDescription("Search")).performClick()
        assertTrue("SearchBar should trigger onSearchClick callback", searchClicked)
    }

    @Test
    fun searchBar_TriggersOnFilterClick() {
        var filterClicked = false
        composeTestRule.setContent { SearchBar(onFilterClick = { filterClicked = true }) }
        composeTestRule.onNode(hasContentDescription("Filter")).performClick()
        assertTrue("SearchBar should trigger onFilterClick callback", filterClicked)
    }

    @Test
    fun searchBar_OnlySearchClickWhenClickingSearchArea() {
        var searchClicked = false
        var filterClicked = false
        composeTestRule.setContent {
            SearchBar(
                onSearchClick = { searchClicked = true },
                onFilterClick = { filterClicked = true })
        }
        composeTestRule.onNode(hasContentDescription("Search")).performClick()
        assertTrue("SearchBar should trigger onSearchClick", searchClicked)
        assertFalse("SearchBar should not trigger onFilterClick", filterClicked)
    }

    @Test
    fun searchBar_OnlyFilterClickWhenClickingFilterButton() {
        var filterClicked = false
        composeTestRule.setContent { SearchBar(onFilterClick = { filterClicked = true }) }
        composeTestRule.onNode(hasContentDescription("Filter")).performClick()
        assertTrue("SearchBar should trigger onFilterClick", filterClicked)
    }

    @Test
    fun searchBar_AcceptsCustomColor() {
        composeTestRule.setContent { SearchBar(searchColorBar = Color.Red) }
        // Just verify it renders without crashing
        composeTestRule.onNode(hasContentDescription("Search")).assertExists()
    }

    // ============ DisplayedSubscriptionFilter Tests ============

    @Test
    fun eventsFilterButtons_DisplaysSubscribedButton() {
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = SubscriptionFilter.All,
                onSelected = {},
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }
        composeTestRule.onNodeWithText("Subscribed").assertIsDisplayed()
    }

    @Test
    fun eventsFilterButtons_DisplaysAllEventsButton() {
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = SubscriptionFilter.All,
                onSelected = {},
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }
        composeTestRule.onNodeWithText("All Events").assertIsDisplayed()
    }

    @Test
    fun eventsFilterButtons_SubscribedButtonIsClickable() {
        var selectedFilter: SubscriptionFilter? = null
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = SubscriptionFilter.All,
                onSelected = { selectedFilter = it },
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }
        composeTestRule.onNodeWithText("Subscribed").performClick()
        assertEquals(
            "Clicking Subscribed should call onSelected with Subscribed",
            SubscriptionFilter.Subscribed,
            selectedFilter
        )
    }

    @Test
    fun eventsFilterButtons_AllEventsButtonIsClickable() {
        var selectedFilter: SubscriptionFilter? = null
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = SubscriptionFilter.Subscribed,
                onSelected = { selectedFilter = it },
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }
        composeTestRule.onNodeWithText("All Events").performClick()
        assertEquals(
            "Clicking All Events should call onSelected with All",
            SubscriptionFilter.All,
            selectedFilter
        )
    }

    @Test
    fun eventsFilterButtons_ShowsCorrectSelectionForSubscribed() {
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = SubscriptionFilter.Subscribed,
                onSelected = {},
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }
        // The selected button should be displayed (bold text styling)
        composeTestRule.onNodeWithText("Subscribed").assertIsDisplayed()
        composeTestRule.onNodeWithText("All Events").assertIsDisplayed()
    }

    @Test
    fun eventsFilterButtons_ShowsCorrectSelectionForAll() {
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = SubscriptionFilter.All,
                onSelected = {},
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }
        composeTestRule.onNodeWithText("Subscribed").assertIsDisplayed()
        composeTestRule.onNodeWithText("All Events").assertIsDisplayed()
    }

    @Test
    fun eventsFilterButtons_ChangesSelectionOnClick() {
        var selectedFilter = SubscriptionFilter.All
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = selectedFilter,
                onSelected = { selectedFilter = it },
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }
        composeTestRule.onNodeWithText("Subscribed").performClick()
        composeTestRule.waitForIdle()
        assertEquals(
            "Selection should change to Subscribed", SubscriptionFilter.Subscribed, selectedFilter
        )
    }

    @Test
    fun eventsFilterButtons_CanSwitchBetweenFilters() {
        var selectedFilter = SubscriptionFilter.All
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = selectedFilter,
                onSelected = { selectedFilter = it },
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }

        // Switch to Subscribed
        composeTestRule.onNodeWithText("Subscribed").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Should switch to Subscribed", SubscriptionFilter.Subscribed, selectedFilter)

        // Switch back to All
        composeTestRule.onNodeWithText("All Events").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Should switch back to All", SubscriptionFilter.All, selectedFilter)
    }

    @Test
    fun eventsFilterButtons_CallbackCalledWithCorrectFilter() {
        val callbackResults = mutableListOf<SubscriptionFilter>()
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = SubscriptionFilter.All,
                onSelected = { callbackResults.add(it) },
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }

        composeTestRule.onNodeWithText("Subscribed").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("All Events").performClick()
        composeTestRule.waitForIdle()

        assertEquals("Should have two callback calls", 2, callbackResults.size)
        assertEquals(
            "First callback should be Subscribed", SubscriptionFilter.Subscribed, callbackResults[0]
        )
        assertEquals("Second callback should be All", SubscriptionFilter.All, callbackResults[1])
    }

    // ============ Integration Tests ============

    @Test
    fun composables_WorkTogetherInLayout() {
        var searchClicked = false
        var selectedFilter = SubscriptionFilter.All
        var eventClicked = false

        composeTestRule.setContent {
            androidx.compose.foundation.layout.Column {
                SearchBar(onSearchClick = { searchClicked = true })
                DisplayedSubscriptionFilter(
                    selected = selectedFilter,
                    onSelected = { selectedFilter = it },
                    subscribedLabel = "Subscribed",
                    allLabel = "All Events"
                )
                EventCard(sampleEvent, onClick = { eventClicked = true })
            }
        }

        // Verify all components are present
        composeTestRule.onNode(hasContentDescription("Search")).assertIsDisplayed()
        composeTestRule.onNodeWithText("Subscribed").assertIsDisplayed()
        composeTestRule.onNodeWithText("All Events").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sample Event").assertIsDisplayed()

        // Test interactions
        composeTestRule.onNode(hasContentDescription("Search")).performClick()
        assertTrue("Search should be clicked", searchClicked)

        composeTestRule.onNodeWithText("Subscribed").performClick()
        assertEquals("Filter should change", SubscriptionFilter.Subscribed, selectedFilter)

        composeTestRule.onNodeWithText("Sample Event").performClick()
        assertTrue("Event should be clicked", eventClicked)
    }

    @Test
    fun eventCard_HandlesEmptyTags() {
        val eventWithEmptyTags = sampleEvent.copy(tags = emptySet())
        composeTestRule.setContent { EventCard(eventWithEmptyTags) }
        composeTestRule.onNodeWithText("Sample Event").assertIsDisplayed()
    }

    @Test
    fun eventCard_HandlesZeroPrice() {
        val freeEvent = sampleEvent.copy(price = 0u)
        composeTestRule.setContent { EventCard(freeEvent) }
        composeTestRule.onNodeWithText("Free").assertIsDisplayed()
        composeTestRule.onNodeWithText("CHF", substring = true).assertDoesNotExist()
    }

    @Test
    fun eventCard_HandlesLargePrice() {
        val expensiveEvent = sampleEvent.copy(price = 9999u)
        composeTestRule.setContent { EventCard(expensiveEvent) }
        composeTestRule.onNodeWithText("CHF 9999").assertIsDisplayed()
    }

    @Test
    fun searchBar_DoesNotCrashWithMultipleClicks() {
        var clickCount = 0
        composeTestRule.setContent { SearchBar(onSearchClick = { clickCount++ }) }

        repeat(10) { composeTestRule.onNode(hasContentDescription("Search")).performClick() }

        assertEquals("Should handle multiple clicks", 10, clickCount)
    }

    @Test
    fun eventsFilterButtons_DoesNotCrashWithRapidClicks() {
        var clickCount = 0
        composeTestRule.setContent {
            DisplayedSubscriptionFilter(
                selected = SubscriptionFilter.All,
                onSelected = { clickCount++ },
                subscribedLabel = "Subscribed",
                allLabel = "All Events"
            )
        }

        repeat(5) {
            composeTestRule.onNodeWithText("Subscribed").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("All Events").performClick()
            composeTestRule.waitForIdle()
        }

        assertEquals("Should handle rapid clicks", 10, clickCount)
    }
}
