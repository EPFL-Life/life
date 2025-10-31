package ch.epfllife.ui.eventDetails

// import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.map.Location
import ch.epfllife.ui.theme.Theme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the EventDetailsScreen composable. These tests verify that the event details, image,
 * and buttons are correctly displayed and that interactions such as navigation and enrollment work
 * as expected.
 */
class EventDetailsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var sampleEvent: Event

    @Before
    fun setUp() {
        // Create a mock event similar to the one used in the Preview
        sampleEvent =
            Event(
                id = "1",
                title = "Drone Workshop",
                description =
                    "The Drone Workshop is a multi-evening workshop organized by AéroPoly, where you can build your own 3-inch FPV drone...",
                location = Location(46.5191, 6.5668, "Centre Sport et Santé"),
                time = "2025-10-12 18:00",
                association = Association(
                    id = "fefeijf",
                    name = "AeroPoly",
                    description = "This is a association",
                    eventCategory = EventCategory.ACADEMIC
                ),
                tags = setOf("workshop"),
                price = 10u,
                pictureUrl =
                    "https://www.shutterstock.com/image-photo/engineer-working-on-racing-fpv-600nw-2278353271.jpg"
            )

        composeTestRule.setContent {
            Theme {
                EventDetailsContent(
                    event = sampleEvent,
                    onGoBack = {},
                    viewModel = viewModel()
                )
            }
        }
    }

    /** Ensures that the event image is visible. */
    @Test
    fun eventImage_isDisplayed() {
        composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
    }

    /** Verifies that the title, club name, price, and description appear correctly. */
    @Test
    fun eventInformation_isDisplayedCorrectly() {
        composeTestRule.onNodeWithText("Drone Workshop").assertIsDisplayed()
        composeTestRule.onNodeWithText("AeroPoly").assertIsDisplayed()
        composeTestRule.onNodeWithText("CHF 10").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
        composeTestRule.onNodeWithText(sampleEvent.description).assertIsDisplayed()
    }

    /**
     * Ensures that the date and time information are shown. Option 2 fix: avoids failure when
     * multiple identical nodes are found.
     */
    @Test
    fun dateAndTime_areDisplayed() {
        composeTestRule.onNodeWithContentDescription("Date").assertExists()
        composeTestRule.onNodeWithContentDescription("Time").assertExists()
        // FIX: Instead of assuming a single node, we assert that *at least one* node displays the text
        composeTestRule.onAllNodesWithText("2025-10-12 18:00")[0].assertIsDisplayed()
    }

    /** Checks that the “View Location on Map” section is present and clickable. */
    @Test
    fun viewLocationOnMap_isDisplayedAndClickable() {
        composeTestRule.onNodeWithText("View Location on Map").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Location on Map").performClick()
    }

    /** Ensures that the enrolment button is visible and can be clicked. */
    @Test
    fun enrollButton_isDisplayedAndClickable() {
        composeTestRule.onNodeWithText("Enrol in event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enrol in event").performClick()
    }

    /** Checks that the back arrow button exists and can be clicked. */
    @Test
    fun backButton_isDisplayedAndClickable() {
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
    }
}
