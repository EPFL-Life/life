package ch.epfllife.ui.admin

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.model.db.Db
import ch.epfllife.ui.theme.Theme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class AddEditAssociationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setContent(
      db: Db = Db.freshLocal(),
      associationId: String? = null,
      viewModel: AddEditAssociationViewModel? = null,
      onSubmitSuccess: () -> Unit = {},
  ) {
    composeTestRule.setContent {
      Theme {
        if (viewModel != null) {
          AddEditAssociationScreen(
              db = db,
              associationId = associationId,
              viewModel = viewModel,
              onBack = {},
              onSubmitSuccess = onSubmitSuccess)
        } else {
          AddEditAssociationScreen(
              db = db,
              associationId = associationId,
              onBack = {},
              onSubmitSuccess = onSubmitSuccess)
        }
      }
    }
  }

  @Test
  fun displayEmptyFormForNewAssociation() {
    // Arrange
    val db = Db.freshLocal()

    // Act
    setContent(db = db)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.NAME_FIELD)
        .assert(hasText("", substring = false))
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON).assertIsNotEnabled()
  }

  @Test
  fun displayFilledFormForExistingAssociation() {
    // Arrange
    val association = ExampleAssociations.association1
    val db = Db.freshLocal()
    runBlocking { db.assocRepo.createAssociation(association) }

    // Act
    setContent(db = db, associationId = association.id)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.NAME_FIELD)
        .assert(hasText(association.name, substring = false))
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.DESCRIPTION_FIELD)
        .assert(hasText(association.description, substring = false))
    association.about?.let {
      composeTestRule
          .onNodeWithTag(AddEditAssociationTestTags.ABOUT_FIELD)
          .assert(hasText(it, substring = false))
    }
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON).assertIsEnabled()
  }

  @Test
  fun viewModelInjectionWorks() {
    // Arrange
    val db = Db.freshLocal()
    val viewModel = AddEditAssociationViewModel(db, null)
    viewModel.updateName("Injected Name")

    // Act
    setContent(viewModel = viewModel)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.NAME_FIELD)
        .assert(hasText("Injected Name", substring = false))
  }
}
