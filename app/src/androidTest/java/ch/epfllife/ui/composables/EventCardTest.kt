package ch.epfllife.ui.composables

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.event.Event
import ch.epfllife.ui.theme.Theme
import ch.epfllife.utils.assertClickable
import org.junit.Rule
import org.junit.Test

class EventCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val eventWithBanner = ExampleEvents.event1
  private val eventWithoutBanner = ExampleEvents.event3

  private fun setEventCardContent(event: Event) {
    composeTestRule.setContent {
      Theme {
        EventCard(
            event = event,
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
                onClick = clickHandler,
            )
          }
        },
        EventCardTestTags.getEventCardTestTag(eventWithBanner.id),
    )
  }

  @Test
  fun bannerImage_isDisplayed_whenPictureUrlAvailable() {
    setEventCardContent(eventWithBanner)

    composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
  }

  @Test
  fun bannerImage_isDisplayed_whenPictureMissing() {
    setEventCardContent(eventWithoutBanner)

    composeTestRule.onNodeWithContentDescription("Event Image").assertIsDisplayed()
  }

  @Test
  fun coreEventDetails_areVisible() {
    setEventCardContent(eventWithBanner)

    composeTestRule.onNodeWithText(eventWithBanner.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(eventWithBanner.association.name).assertIsDisplayed()
    composeTestRule.onNodeWithText(eventWithBanner.location.name).assertIsDisplayed()
    composeTestRule.onNodeWithText(eventWithBanner.time).assertIsDisplayed()
  }
}
