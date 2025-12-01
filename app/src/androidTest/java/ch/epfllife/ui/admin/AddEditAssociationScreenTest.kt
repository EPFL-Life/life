package ch.epfllife.ui.admin

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.model.association.Association
import ch.epfllife.ui.theme.Theme
import org.junit.Rule
import org.junit.Test

class AddEditAssociationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private fun setContent(
      viewModel: AddEditAssociationViewModel = AddEditAssociationViewModel(),
      onSubmitSuccess: () -> Unit = {},
  ) {
    composeTestRule.setContent {
      Theme {
        AddEditAssociationScreen(
            viewModel = viewModel, onBack = {}, onSubmitSuccess = onSubmitSuccess)
      }
    }
  }

  @Test
  fun submitButtonRequiresMandatoryFields() {
    setContent()

    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON).assertIsNotEnabled()

    fillMandatoryFields(ExampleAssociations.association4)

    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON).assertIsEnabled()
  }

  @Test
  fun editingExistingAssociationPrefillsForm() {
    val association = ExampleAssociations.association1
    setContent(AddEditAssociationViewModel(existingAssociation = association))

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
  }

  private fun fillMandatoryFields(association: Association) {
    val name = association.name
    val description = association.description
    val about = association.about ?: "About ${association.name}"

    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.NAME_FIELD).performTextInput(name)
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.DESCRIPTION_FIELD)
        .performTextInput(description)
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.ABOUT_FIELD).performTextInput(about)
  }
}
