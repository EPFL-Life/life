package ch.epfllife.ui.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
// import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * This test class verifies the correct behavior and visual rendering of the HomeScreen composable.
 * It ensures that elements such as the logo, search bar, filter buttons, and event list behave as expected.
 */
class HomeScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        // Prepare the composable before each test
        composeTestRule.setContent { HomeScreen() }
    }

    /**
     * This test verifies that the EPFL Life logo image is correctly displayed on the HomeScreen.
     */
    @Test
    fun epflLogo_isDisplayed() {
        composeTestRule
            .onNodeWithContentDescription("EPFL Life Logo")
            .assertIsDisplayed() // Ensure the logo is visible
    }

    /**
     * This test verifies that the SearchBar composable and event filter buttons are displayed.
     */
    @Test
    fun searchBar_andFilterButtons_areDisplayed() {
        // Verify the search bar (it may contain placeholder text like "Search")
        composeTestRule.onNodeWithText("Search", ignoreCase = true).assertExists()

        // Verify both event filter buttons exist
        composeTestRule.onNodeWithText("Subscribed", ignoreCase = true).assertExists()
        composeTestRule.onNodeWithText("All", ignoreCase = true).assertExists()
    }

    /**
     * This test verifies that when the default filter (Subscribed) is selected,
     * only subscribed events are displayed.
     */
    @Test
    fun subscribedFilter_showsOnlyMyEvents() {
        // Default selected is "Subscribed"
        composeTestRule.onAllNodesWithText("Via Ferrata").assertCountEquals(1)
        composeTestRule.onNodeWithText("Music Festival").assertDoesNotExist()
    }

    /**
     * This test verifies that when the "All" filter button is pressed,
     * all events (both subscribed and non-subscribed) are displayed.
     */
    @Test
    fun allFilter_showsAllEvents() {
        composeTestRule.onNodeWithText("All", ignoreCase = true).performClick()

        // After switching to "All", both events should appear
        composeTestRule.onAllNodesWithText("Via Ferrata").assertCountEquals(1)
        composeTestRule.onAllNodesWithText("Music Festival").assertCountEquals(1)
    }

    /**
     * This test verifies that the event cards display the correct event title and description.
     */
    @Test
    fun eventCards_displayCorrectInformation() {
        // The subscribed event should show its title and description
        composeTestRule.onNodeWithText("Via Ferrata").assertIsDisplayed()
        composeTestRule.onNodeWithText("Excursion to the Alps").assertIsDisplayed()
    }

    /**
     * This test verifies that the LazyColumn correctly displays the list of shown events.
     */
    @Test
    fun lazyColumn_displaysEventsProperly() {
        // At least one event card should be visible
        composeTestRule.onNodeWithText("Via Ferrata").assertIsDisplayed()
    }

    /**
     * This test verifies that switching back from "All" to "Subscribed" updates the displayed events.
     */
    @Test
    fun switchingBackToSubscribed_showsOnlyMyEventsAgain() {
        // Go to "All"
        composeTestRule.onNodeWithText("All", ignoreCase = true).performClick()
        // Go back to "Subscribed"
        composeTestRule.onNodeWithText("Subscribed", ignoreCase = true).performClick()

        // Only subscribed event should remain
        composeTestRule.onAllNodesWithText("Via Ferrata").assertCountEquals(1)
        composeTestRule.onNodeWithText("Music Festival").assertDoesNotExist()
    }
}