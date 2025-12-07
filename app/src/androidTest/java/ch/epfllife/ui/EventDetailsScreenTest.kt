package ch.epfllife.ui.eventDetails

// import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
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

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var sampleEvent: Event

  @Before
  fun setUp() {
    sampleEvent = ExampleEvents.sampleEvent
  }

  private fun setSuccessContent(event: Event = sampleEvent) {
    composeTestRule.setContent {
      Theme {
        EventDetailsContent(
            event = event,
            onGoBack = {},
            onOpenMap = {},
            onEnrollClick = {},
            onAssociationClick = {})
      }
    }
  }

  /** Ensures that the event image is visible. */
  @Test
  fun eventImage_isDisplayed() {
    setSuccessContent()
    composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
  }

  /** Verifies that the title, club name, price, and description appear correctly. */
  @Test
  fun eventInformation_isDisplayedCorrectly() {
    setSuccessContent()
    composeTestRule.onNodeWithText(sampleEvent.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(sampleEvent.association.name).assertIsDisplayed()
    composeTestRule.onNodeWithText(sampleEvent.price.toString()).assertIsDisplayed()
    composeTestRule.onNodeWithText("Description").assertIsDisplayed()
    composeTestRule.onNodeWithText(sampleEvent.description).assertIsDisplayed()
  }

  /**
   * Ensures that the date and time information are shown. Option 2 fix: avoids failure when
   * multiple identical nodes are found.
   */
  @Test
  fun dateAndTime_areDisplayed() {
    setSuccessContent()
    composeTestRule.onNodeWithContentDescription("Date").assertExists()
    composeTestRule.onNodeWithContentDescription("Time").assertExists()
    val (expectedDate, expectedTime) = sampleEvent.time.split(" ")
    composeTestRule.onNodeWithText(expectedDate).assertIsDisplayed()
    composeTestRule.onNodeWithText(expectedTime).assertIsDisplayed()
  }

  @Test
  fun timeWithDash_isFormattedWithColon() {
    val dashedTime = ExampleEvents.event1.time.replace(":", "-")
    val dashedEvent = ExampleEvents.event1.copy(time = dashedTime)
    setSuccessContent(dashedEvent)
    val expectedTime = ExampleEvents.event1.time.split(" ")[1]
    composeTestRule.onNodeWithText(expectedTime).assertIsDisplayed()
  }

  @Test
  fun errorState_displaysErrorMessage() {
    val localDb = Db.freshLocal()
    composeTestRule.setContent {
      Theme {
        EventDetailsScreen(
            eventId = "missing",
            db = localDb,
            onOpenMap = {},
            onGoBack = {},
            onAssociationClick = {},
        )
      }
    }

    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodes(hasTestTag(EventDetailsTestTags.ERROR_MESSAGE))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(EventDetailsTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  /** Checks that the “View Location on Map” section is present and clickable. */
  @Test
  fun viewLocationOnMap_isDisplayedAndClickable() {
    setSuccessContent()
    composeTestRule
        .onNodeWithTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON)
        .assertIsDisplayed()
        .performClick()
  }

  /** Ensures that the enrolment button is visible and can be clicked. */
  @Test
  fun enrollButton_isDisplayedAndClickable() {
    setSuccessContent()
    composeTestRule.onNodeWithText("Enrol in event").assertIsDisplayed()
    composeTestRule.onNodeWithText("Enrol in event").performClick()
  }

  /** Checks that the back arrow button exists and can be clicked. */
  @Test
  fun backButton_isDisplayedAndClickable() {
    setSuccessContent()
    composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Back").performClick()
  }
}
