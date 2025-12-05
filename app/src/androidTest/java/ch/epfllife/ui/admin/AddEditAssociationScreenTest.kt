package ch.epfllife.ui.admin

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.ui.association.SocialIcons
import ch.epfllife.ui.theme.Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
    val viewModel = AddEditAssociationViewModel(db = db)
    viewModel.updateName("Injected Name")

    // Act
    setContent(viewModel = viewModel)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.NAME_FIELD)
        .assert(hasText("Injected Name", substring = false))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitNewAssociation_callsCreateOnRepository() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val db = Db.freshLocal()
    val viewModel = AddEditAssociationViewModel(db = db, submitDispatcher = dispatcher)

    viewModel.populateMandatoryFields(ExampleAssociations.association4)

    viewModel.submit {}
    advanceUntilIdle()

    val associations = db.assocRepo.getAllAssociations()
    assert(associations.size == 1)
    val created = associations.first()
    assert(created.id.isNotBlank())
    assert(created.name == ExampleAssociations.association4.name)
    assert(created.description == ExampleAssociations.association4.description)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitExistingAssociation_callsUpdateWithModifiedData() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val existing = ExampleAssociations.association1
    val db = Db.freshLocal()
    db.assocRepo.createAssociation(existing)
    val viewModel =
        AddEditAssociationViewModel(
            db = db,
            associationId = existing.id,
            submitDispatcher = dispatcher,
        )

    val updatedName = "Updated ${existing.name}"
    viewModel.updateName(updatedName)

    viewModel.submit {}
    advanceUntilIdle()

    val updated = db.assocRepo.getAssociation(existing.id)
    checkNotNull(updated)
    assert(updated.name == updatedName)
    assert(updated.description == existing.description)
    assert(updated.eventCategory == existing.eventCategory)
  }

  @Test
  fun invalidForm_doesNotCallRepository() {
    val db = Db.freshLocal()
    val viewModel = AddEditAssociationViewModel(db)

    setContent(db = db, viewModel = viewModel)

    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON).assertIsNotEnabled()
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    val associations = runBlocking { db.assocRepo.getAllAssociations() }
    assert(associations.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitNewAssociation_preservesOptionalFields() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val base = ExampleAssociations.association4
    val db = Db.freshLocal()
    val viewModel = AddEditAssociationViewModel(db = db, submitDispatcher = dispatcher)

    viewModel.populateMandatoryFields(base)

    viewModel.submit {}
    advanceUntilIdle()

    val created = db.assocRepo.getAllAssociations().first()
    assert(created.name == base.name)
    assert(created.description == base.description)
    assert(created.pictureUrl == null)
    assert(created.logoUrl == null)
    assert(created.socialLinks == null)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitExistingAssociation_preservesUneditedOptionalFields() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val existing = ExampleAssociations.association1
    val db = Db.freshLocal()
    db.assocRepo.createAssociation(existing)
    val viewModel =
        AddEditAssociationViewModel(
            db = db,
            associationId = existing.id,
            submitDispatcher = dispatcher,
        )

    val updatedName = "Updated ${existing.name}"
    viewModel.updateName(updatedName)

    viewModel.submit {}
    advanceUntilIdle()

    val updated = db.assocRepo.getAssociation(existing.id)
    checkNotNull(updated)
    assert(updated.pictureUrl == existing.pictureUrl)
    assert(updated.logoUrl == existing.logoUrl)
    assert(updated.socialLinks == existing.socialLinks)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitNewAssociation_savesSocialAndMediaFields() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val db = Db.freshLocal()
    val viewModel = AddEditAssociationViewModel(db = db, submitDispatcher = dispatcher)

    viewModel.populateMandatoryFields(ExampleAssociations.association2)
    val targetPlatform = SocialIcons.platformOrder.first()
    val logoUrl = "https://example.com/logo.png"
    val bannerUrl = "https://example.com/banner.png"
    val socialUrl = "https://social.example.com/assoc"

    viewModel.updateLogoUrl(logoUrl)
    viewModel.updateBannerUrl(bannerUrl)
    viewModel.updateSocialMedia(targetPlatform, true)
    viewModel.updateSocialMediaLink(targetPlatform, socialUrl)

    viewModel.submit {}
    advanceUntilIdle()

    val created = db.assocRepo.getAllAssociations().first()
    assert(created.logoUrl == logoUrl)
    assert(created.pictureUrl == bannerUrl)
    assert(created.socialLinks?.get(targetPlatform) == socialUrl)
  }

  private fun AddEditAssociationViewModel.populateMandatoryFields(source: Association) {
    updateName(source.name)
    updateDescription(source.description)
    updateAbout(source.about ?: "About ${source.name}")
  }
}
