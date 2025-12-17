package ch.epfllife.ui.composables

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location
import ch.epfllife.ui.theme.Theme
import ch.epfllife.utils.assertClickable
import org.junit.Rule
import org.junit.Test

class EventCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val eventWithBanner = ExampleEvents.event1
  private val eventWithoutBanner = ExampleEvents.event3

  private fun setEventCardContent(event: Event, isEnrolled: Boolean) {
    composeTestRule.setContent {
      Theme {
        EventCard(
            event = event,
            isEnrolled = isEnrolled,
            onClick = {},
        )
      }
    }
  }

  @Test
  fun card_isClickable() {
    composeTestRule.assertClickable(
        { clickHandler ->
          Theme {
            EventCard(
                event = eventWithBanner,
                isEnrolled = true,
                onClick = clickHandler,
            )
          }
        },
        EventCardTestTags.getEventCardTestTag(eventWithBanner.id),
    )
  }

  @Test
  fun bannerImage_isDisplayed_whenPictureUrlAvailable() {
    setEventCardContent(eventWithBanner, isEnrolled = true)

    composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
  }

  @Test
  fun bannerImage_isDisplayed_whenPictureMissing() {
    setEventCardContent(eventWithoutBanner, isEnrolled = true)

    composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
  }

  @Test
  fun coreEventDetails_areVisible() {
    setEventCardContent(eventWithBanner, isEnrolled = true)

    composeTestRule.onNodeWithText(eventWithBanner.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(eventWithBanner.association.name).assertIsDisplayed()
    composeTestRule.onNodeWithText(eventWithBanner.location.name).assertIsDisplayed()
    composeTestRule.onNodeWithText(eventWithBanner.time).assertIsDisplayed()
  }

  @Test
  fun location_isShortenedToTextBeforeFirstComma_onCard() {
    val longLocationName = "Here, Blackwall Tunnel, Blackwall Reach, Greater London, United Kingdom"
    val eventWithLongLocation =
        eventWithBanner.copy(
            location =
                Location(
                    latitude = eventWithBanner.location.latitude,
                    longitude = eventWithBanner.location.longitude,
                    name = longLocationName,
                ),
        )

    setEventCardContent(eventWithLongLocation, isEnrolled = false)

    composeTestRule.onNodeWithText("Here").assertIsDisplayed()
    composeTestRule.onNodeWithText(longLocationName).assertDoesNotExist()
  }

  @Test
  fun enrollmentIndicator_isDisplayed_whenEnrolled() {
    setEventCardContent(eventWithBanner, isEnrolled = true)

    composeTestRule.onNodeWithText("Enrolled", ignoreCase = false).assertIsDisplayed()
  }

  @Test
  fun enrollmentButton_isDisplayed_whenNotEnrolled() {
    setEventCardContent(eventWithBanner, isEnrolled = false)
    composeTestRule.onNodeWithText("Enrolled", ignoreCase = false).assertDoesNotExist()
  }
}
