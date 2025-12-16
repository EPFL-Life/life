package ch.epfllife.ui.admin

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.event.displayString
import ch.epfllife.ui.association.SocialIcons
import ch.epfllife.ui.theme.Theme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
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

    val targetCategory = EventCategory.SPORTS
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.EVENT_CATEGORY_FIELD).performClick()
    composeTestRule.onNodeWithText(targetCategory.displayString()).performClick()
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.EVENT_CATEGORY_FIELD)
        .assert(hasText(targetCategory.displayString(), substring = false))
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

  @Test
  fun submitNewAssociation_callsCreateOnRepository() {
    val db = Db.freshLocal()
    val viewModel = AddEditAssociationViewModel(db = db)

    viewModel.populateMandatoryFields(ExampleAssociations.association4)

    viewModel.submit {}

    waitUntilTrue { runBlocking { db.assocRepo.getAllAssociations().isNotEmpty() } }

    val associations = runBlocking { db.assocRepo.getAllAssociations() }
    val created = associations.first()
    assert(created.id.isNotBlank())
    assertEquals(created.name, ExampleAssociations.association4.name)
    assertEquals(created.description, ExampleAssociations.association4.description)
  }

  @Test
  fun submitExistingAssociation_callsUpdateWithModifiedData() {
    val existing = ExampleAssociations.association1
    val db = Db.freshLocal()
    runBlocking { db.assocRepo.createAssociation(existing) }
    val viewModel = AddEditAssociationViewModel(db = db, associationId = existing.id)

    viewModel.awaitAssociationLoaded()
    viewModel.populateMandatoryFields(existing)
    val updatedName = "Updated ${existing.name}"
    viewModel.updateName(updatedName)

    viewModel.submit {}

    waitUntilTrue { runBlocking { db.assocRepo.getAssociation(existing.id)?.name == updatedName } }

    val updated = runBlocking { db.assocRepo.getAssociation(existing.id) }
    checkNotNull(updated)
    assertEquals(updated.name, updatedName)
    assertEquals(updated.description, existing.description)
    assertEquals(updated.eventCategory, existing.eventCategory)
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

  @Test
  fun submitNewAssociation_preservesOptionalFields() {
    val base = ExampleAssociations.association4
    val db = Db.freshLocal()
    val viewModel = AddEditAssociationViewModel(db = db)

    viewModel.populateMandatoryFields(base)

    viewModel.submit {}

    waitUntilTrue { runBlocking { db.assocRepo.getAllAssociations().isNotEmpty() } }

    val created = runBlocking { db.assocRepo.getAllAssociations().first() }
    assertEquals(created.name, base.name)
    assertEquals(created.description, base.description)
    assertEquals(created.pictureUrl, null)
    assertEquals(created.logoUrl, null)
    assertEquals(created.socialLinks, null)
  }

  @Test
  fun submitExistingAssociation_preservesUneditedOptionalFields() {
    val existing = ExampleAssociations.association1
    val db = Db.freshLocal()
    runBlocking { db.assocRepo.createAssociation(existing) }
    val viewModel = AddEditAssociationViewModel(db = db, associationId = existing.id)

    viewModel.awaitAssociationLoaded()
    viewModel.populateMandatoryFields(existing)
    val updatedName = "Updated ${existing.name}"
    viewModel.updateName(updatedName)

    viewModel.submit {}

    waitUntilTrue { runBlocking { db.assocRepo.getAssociation(existing.id)?.name == updatedName } }

    val updated = runBlocking { db.assocRepo.getAssociation(existing.id) }
    checkNotNull(updated)
    assertEquals(updated.pictureUrl, existing.pictureUrl)
    assertEquals(updated.logoUrl, existing.logoUrl)
    assertEquals(updated.socialLinks, existing.socialLinks)
  }

  @Test
  fun submitNewAssociation_savesSocialAndMediaFields() {
    val db = Db.freshLocal()
    val viewModel = AddEditAssociationViewModel(db = db)

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

    waitUntilTrue { runBlocking { db.assocRepo.getAllAssociations().isNotEmpty() } }

    val created = runBlocking { db.assocRepo.getAllAssociations().first() }
    assertEquals(created.logoUrl, logoUrl)
    assertEquals(created.pictureUrl, bannerUrl)
    assertEquals(created.socialLinks?.get(targetPlatform), socialUrl)
  }

  @Test
  fun verifyUploadButtonsExist() {
    // Arrange
    val db = Db.freshLocal()

    // Act
    setContent(db = db)
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.UPLOAD_LOGO_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.UPLOAD_BANNER_BUTTON).assertExists()
  }

  private fun waitUntilTrue(timeoutMillis: Long = 20_000, condition: () -> Boolean) {
    val timeoutAt = System.currentTimeMillis() + timeoutMillis
    while (!condition()) {
      if (System.currentTimeMillis() >= timeoutAt) {
        error("Condition not met within ${timeoutMillis}ms")
      }
      Thread.sleep(50)
    }
  }

  private fun AddEditAssociationViewModel.awaitAssociationLoaded(timeoutMillis: Long = 5_000) {
    val timeoutAt = System.currentTimeMillis() + timeoutMillis
    while (uiState.value != AddEditAssociationUIState.Success) {
      if (System.currentTimeMillis() >= timeoutAt) {
        error("Association did not load within ${timeoutMillis}ms")
      }
      Thread.sleep(50)
    }
  }

  private fun AddEditAssociationViewModel.populateMandatoryFields(source: Association) {
    updateName(source.name)
    updateDescription(source.description)
    updateAbout(source.about ?: "About ${source.name}")
  }
}
