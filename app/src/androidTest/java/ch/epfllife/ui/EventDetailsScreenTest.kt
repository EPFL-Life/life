package ch.epfllife.ui.eventDetails

// import androidx.compose.ui.test.assertExists
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import ch.epfllife.R
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.model.user.User
import ch.epfllife.ui.theme.Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

  private fun setSuccessContent(
      event: Event = sampleEvent,
      attendees: List<User> = emptyList(),
      onAttendeesClick: () -> Unit = {},
  ) {
    composeTestRule.setContent {
      Theme {
        EventDetailsContent(
            event = event,
            attendees = attendees,
            onAttendeesClick = onAttendeesClick,
            onGoBack = {},
            onOpenMap = {},
            onEnrollClick = {},
            onShare = {},
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
            onOpenAttendees = {},
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

  @Test
  fun attendeeCount_isDisplayed() {
    val attendees = listOf(ExampleUsers.user1, ExampleUsers.user2)
    setSuccessContent(attendees = attendees)
    composeTestRule.onNodeWithText("2 attending").assertIsDisplayed()
  }

  @Test
  fun attendeeCount_click_triggersCallback() {
    var clicked = false
    val attendees = listOf(ExampleUsers.user1, ExampleUsers.user2)
    setSuccessContent(attendees = attendees, onAttendeesClick = { clicked = true })
    composeTestRule.onNodeWithText("2 attending").performClick()
    org.junit.Assert.assertTrue("Attendee row should trigger callback", clicked)
  }

  @Test
  fun shareMessage_includesResolvedImageUrl_andChooserWrapsSendIntent() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val event = ExampleEvents.event3 // has pictureUrl = null
    val senderName = ExampleUsers.user1.name

    val shareText =
        buildShareMessage(
            context = context,
            eventTitle = event.title,
            senderName = senderName,
            pictureUrl = event.pictureUrl,
        )

    val expectedImageUrl = resolveShareImageUrl(event.pictureUrl)
    assertTrue(shareText.contains(event.title))
    assertTrue(shareText.contains(senderName))
    assertTrue(shareText.contains(expectedImageUrl))

    val chooser = buildShareChooserIntent(context, shareText)
    assertEquals(Intent.ACTION_CHOOSER, chooser.action)
    assertTrue((chooser.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)

    val innerSendIntent: Intent? =
        if (Build.VERSION.SDK_INT >= 33) {
          chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
          @Suppress("DEPRECATION") chooser.getParcelableExtra(Intent.EXTRA_INTENT) as? Intent
        }
    assertNotNull(innerSendIntent)
    assertEquals(Intent.ACTION_SEND, innerSendIntent!!.action)
    assertEquals("text/plain", innerSendIntent.type)
    assertEquals(shareText, innerSendIntent.getStringExtra(Intent.EXTRA_TEXT))
  }

  @Test
  fun description_showsShowMore_whenOverflow_andTogglesToShowLess_onClick() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val showMore = context.getString(R.string.show_more)
    val showLess = context.getString(R.string.show_less)

    val base = ExampleEvents.sampleEvent
    val longDescription = (base.description + " ").repeat(80)
    val event = base.copy(description = longDescription)

    composeTestRule.setContent {
      Theme {
        // Constrain width to make overflow deterministic in tests.
        Box(Modifier.width(220.dp)) {
          EventDetailsContent(
              event = event,
              attendees = emptyList(),
              onAttendeesClick = {},
              onGoBack = {},
              onOpenMap = {},
              onAssociationClick = {},
              onEnrollClick = {},
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(showMore).assertIsDisplayed()
    composeTestRule.onNodeWithText(showMore).performClick()
    composeTestRule.onNodeWithText(showLess).assertIsDisplayed()
  }
}
