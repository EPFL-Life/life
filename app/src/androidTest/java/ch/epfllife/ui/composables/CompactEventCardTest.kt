package ch.epfllife.ui.composables

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.ui.theme.Theme
import org.junit.Rule
import org.junit.Test

class CompactEventCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displayEventDetails() {
    // Arrange
    val event = ExampleEvents.event1
    val isEnrolled = false

    // Act
    composeTestRule.setContent {
      Theme { CompactEventCard(event = event, isEnrolled = isEnrolled, onClick = {}) }
    }

    // Assert
    composeTestRule.onNodeWithText(event.title).assertIsDisplayed()
    composeTestRule.onNodeWithText(event.association.name).assertIsDisplayed()
  }

  @Test
  fun displayEnrolledStatus() {
    // Arrange
    val event = ExampleEvents.event1
    val isEnrolled = true

    // Act
    composeTestRule.setContent {
      Theme { CompactEventCard(event = event, isEnrolled = isEnrolled, onClick = {}) }
    }
  }

  @Test
  fun handleCardClick() {
    // Arrange
    val event = ExampleEvents.event1
    var clicked = false

    // Act
    composeTestRule.setContent {
      Theme { CompactEventCard(event = event, isEnrolled = false, onClick = { clicked = true }) }
    }

    composeTestRule.onNodeWithTag(EventCardTestTags.getEventCardTestTag(event.id)).performClick()

    // Assert
    assert(clicked)
  }
}
