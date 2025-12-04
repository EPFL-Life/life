package ch.epfllife.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.db.Db
import ch.epfllife.ui.theme.Theme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ManageEventsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setContent(
      db: Db = Db.freshLocal(),
      associationId: String,
      viewModel: ManageEventsViewModel? = null,
      onAddNewEvent: (String) -> Unit = {},
      onEditEvent: (String) -> Unit = {},
  ) {
    composeTestRule.setContent {
      Theme {
        if (viewModel != null) {
          ManageEventsScreen(
              db = db,
              associationId = associationId,
              viewModel = viewModel,
              onGoBack = {},
              onAddNewEvent = onAddNewEvent,
              onEditEvent = onEditEvent)
        } else {
          ManageEventsScreen(
              db = db,
              associationId = associationId,
              onGoBack = {},
              onAddNewEvent = onAddNewEvent,
              onEditEvent = onEditEvent)
        }
      }
    }
  }

  @Test
  fun displayEmptyStateWhenNoEvents() {
    // Arrange
    val association = ExampleAssociations.association1
    val db = Db.freshLocal()
    runBlocking { db.assocRepo.createAssociation(association) }

    // Act
    setContent(db = db, associationId = association.id)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithTag(ManageEventsTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ManageEventsTestTags.ADD_EVENT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(ManageEventsTestTags.EMPTY_TEXT).assertIsDisplayed()
  }

  @Test
  fun displayEventsForAssociation() {
    // Arrange
    val association = ExampleAssociations.association2 // Use association2 for event2
    val event = ExampleEvents.event2
    val db = Db.freshLocal()
    runBlocking {
      db.assocRepo.createAssociation(association)
      db.eventRepo.createEvent(event)
    }

    // Act
    setContent(db = db, associationId = association.id)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithTag(ManageEventsTestTags.TITLE).assertIsDisplayed()
    // Verify event title is displayed (assuming EventCard displays title)
    composeTestRule.onNode(hasText(event.title)).assertIsDisplayed()
    // Verify empty text is NOT displayed
    composeTestRule.onNodeWithTag(ManageEventsTestTags.EMPTY_TEXT).assertDoesNotExist()
  }

  @Test
  fun viewModelInjectionWorks() {
    // Arrange
    val association = ExampleAssociations.association1
    val db = Db.freshLocal()
    runBlocking { db.assocRepo.createAssociation(association) }

    // Create a ViewModel with a mock or specific state if needed,
    // but here we just check if passing it works.
    val viewModel = ManageEventsViewModel(db, association.id)

    // Act
    setContent(db = db, associationId = association.id, viewModel = viewModel)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithTag(ManageEventsTestTags.TITLE).assertIsDisplayed()
  }
}
