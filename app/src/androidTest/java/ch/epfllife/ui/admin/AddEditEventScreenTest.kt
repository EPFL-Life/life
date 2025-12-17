package ch.epfllife.ui.admin

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.db.Db
import ch.epfllife.ui.theme.Theme
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class AddEditEventScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setContent(
      db: Db = Db.freshLocal(),
      associationId: String,
      eventId: String? = null,
      viewModel: AddEditEventViewModel? = null,
      onSubmitSuccess: () -> Unit = {},
  ) {
    composeTestRule.setContent {
      Theme {
        AddEditEventScreen(
            db = db,
            associationId = associationId,
            eventId = eventId,
            viewModel = viewModel,
            onBack = {},
            onSubmitSuccess = onSubmitSuccess)
      }
    }
  }

  @Test
  fun displayEmptyFormForNewEvent() {
    // Arrange
    val association = ExampleAssociations.association1
    val db = Db.freshLocal()
    runBlocking { db.assocRepo.createAssociation(association) }

    // Act
    setContent(db = db, associationId = association.id)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule
        .onNodeWithTag(AddEditEventTestTags.TITLE_FIELD)
        .assert(hasText("", substring = false))
    composeTestRule.onNodeWithTag(AddEditEventTestTags.SUBMIT_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun displayFilledFormForExistingEvent() {
    // Arrange
    val association = ExampleAssociations.association1
    val event = ExampleEvents.event1
    val db = Db.freshLocal()
    runBlocking {
      db.assocRepo.createAssociation(association)
      db.eventRepo.createEvent(event)
    }

    // Act
    setContent(db = db, associationId = association.id, eventId = event.id)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule
        .onNodeWithTag(AddEditEventTestTags.TITLE_FIELD)
        .assert(hasText(event.title, substring = false))
    composeTestRule
        .onNodeWithTag(AddEditEventTestTags.DESCRIPTION_FIELD)
        .assert(hasText(event.description, substring = false))
    composeTestRule.onNodeWithTag(AddEditEventTestTags.SUBMIT_BUTTON).assertIsEnabled()
  }

  @Ignore(
      "This test passes locally but fails on CI. The reason is that the time picker popup can not be clicked in the CI pipeline")
  @Test
  fun timeSelectionUpdatesViewModel() {
    // Arrange
    val db = Db.freshLocal()
    val association = ExampleAssociations.association1
    runBlocking { db.assocRepo.createAssociation(association) }

    // Act
    setContent(db = db, associationId = association.id)
    composeTestRule.waitForIdle()

    // Click on the time picker box to open DatePicker
    composeTestRule.onNodeWithTag(AddEditEventTestTags.TIME_PICKER_BOX).performClick()

    // Interact with DatePicker (Click OK to accept default/current date)
    onView(withText("OK")).perform(click())

    // Interact with TimePicker (Click OK to accept default/current time)
    onView(withText("OK")).perform(click())

    // Assert
    // Verify that the time field is now populated (contains ":" and "-")
    composeTestRule
        .onNodeWithTag(AddEditEventTestTags.TIME_FIELD)
        .assert(hasText("-", substring = true))
    composeTestRule
        .onNodeWithTag(AddEditEventTestTags.TIME_FIELD)
        .assert(hasText(":", substring = true))
  }

  @Test
  fun imageUploadButton_isDisplayed() {
    val association = ExampleAssociations.association1
    val db = Db.freshLocal()
    runBlocking { db.assocRepo.createAssociation(association) }

    setContent(db = db, associationId = association.id)
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AddEditEventTestTags.IMAGE_UPLOAD_BUTTON).assertIsDisplayed()
  }

  @Test
  fun displayErrorStateWhenRepositoryFails() {
    val db = Db.freshLocal()

    setContent(db = db, associationId = "non-existent-id")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AddEditEventTestTags.ERROR_BOX).assertIsDisplayed()
  }
}
