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
import ch.epfllife.model.association.AssociationRepository
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
    val fakeRepo = FakeAssociationRepository().apply { newUidToReturn = "new-assoc-id" }
    val viewModel =
        AddEditAssociationViewModel(
            associationRepository = fakeRepo,
            submitDispatcher = dispatcher,
        )

    viewModel.populateMandatoryFields(ExampleAssociations.association4)

    viewModel.submit {}
    advanceUntilIdle()

    fakeRepo.assertCreateCalls(1)
    val created = fakeRepo.createdAssociations.first()
    assert(created.id == "new-assoc-id")
    assert(created.name == ExampleAssociations.association4.name)
    assert(created.description == ExampleAssociations.association4.description)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitExistingAssociation_callsUpdateWithModifiedData() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val existing = ExampleAssociations.association1
    val fakeRepo = FakeAssociationRepository()
    val viewModel =
        AddEditAssociationViewModel(
            associationRepository = fakeRepo,
            existingAssociation = existing,
            submitDispatcher = dispatcher,
        )

    val updatedName = "Updated ${existing.name}"
    viewModel.updateName(updatedName)

    viewModel.submit {}
    advanceUntilIdle()

    fakeRepo.assertUpdateCalls(1)
    val (updatedId, updatedAssoc) = fakeRepo.updatedAssociations.first()

    assert(updatedId == existing.id)
    assert(updatedAssoc.name == updatedName)
    assert(updatedAssoc.description == existing.description)
    assert(updatedAssoc.eventCategory == existing.eventCategory)
  }

  @Test
  fun invalidForm_doesNotCallRepository() {
    val fakeRepo = FakeAssociationRepository()
    val viewModel = AddEditAssociationViewModel(associationRepository = fakeRepo)

    setContent(viewModel = viewModel)

    // Do not fill mandatory fields so the form stays invalid
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON).assertIsNotEnabled()

    // Try to click the button anyway (should be a no-op in terms of ViewModel.submit())
    composeTestRule.onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON).performClick()

    composeTestRule.waitForIdle()

    assert(fakeRepo.createdAssociations.isEmpty())
    assert(fakeRepo.updatedAssociations.isEmpty())
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitNewAssociation_preservesOptionalFields() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val base = ExampleAssociations.association4
    val fakeRepo = FakeAssociationRepository().apply { newUidToReturn = "new-assoc-id" }
    val viewModel =
        AddEditAssociationViewModel(
            associationRepository = fakeRepo,
            submitDispatcher = dispatcher,
        )

    viewModel.populateMandatoryFields(base)

    viewModel.submit {}
    advanceUntilIdle()

    fakeRepo.assertCreateCalls(1)
    val created = fakeRepo.createdAssociations.first()

    // Name/description come from the form (which we populated from base)
    assert(created.name == base.name)
    assert(created.description == base.description)

    // Optional fields are not populated in the form in this test, so they should be null/empty
    // in the resulting Association built by the ViewModel.
    assert(created.pictureUrl == null)
    assert(created.logoUrl == null)
    assert(created.socialLinks == null)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitExistingAssociation_preservesUneditedOptionalFields() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val existing = ExampleAssociations.association1
    val fakeRepo = FakeAssociationRepository()
    val viewModel =
        AddEditAssociationViewModel(
            associationRepository = fakeRepo,
            existingAssociation = existing,
            submitDispatcher = dispatcher,
        )

    // Change only the name, leaving other optional fields untouched
    val updatedName = "Updated ${existing.name}"
    viewModel.updateName(updatedName)

    viewModel.submit {}
    advanceUntilIdle()

    fakeRepo.assertUpdateCalls(1)
    val (_, updatedAssoc) = fakeRepo.updatedAssociations.first()

    // Name changed, but optional fields should remain the same as the original
    assert(updatedAssoc.pictureUrl == existing.pictureUrl)
    assert(updatedAssoc.logoUrl == existing.logoUrl)
    assert(updatedAssoc.socialLinks == existing.socialLinks)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun submitNewAssociation_savesSocialAndMediaFields() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val fakeRepo = FakeAssociationRepository()
    val viewModel =
        AddEditAssociationViewModel(
            associationRepository = fakeRepo,
            submitDispatcher = dispatcher,
        )

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

    fakeRepo.assertCreateCalls(1)
    val created = fakeRepo.createdAssociations.first()
    assert(created.logoUrl == logoUrl)
    assert(created.pictureUrl == bannerUrl)
    assert(created.socialLinks?.get(targetPlatform) == socialUrl)
  }

  private fun AddEditAssociationViewModel.populateMandatoryFields(source: Association) {
    updateName(source.name)
    updateDescription(source.description)
    updateAbout(source.about ?: "About ${source.name}")
  }

  private class FakeAssociationRepository : AssociationRepository {
    var newUidToReturn: String = "fake-new-id"
    val createdAssociations = mutableListOf<Association>()
    val updatedAssociations = mutableListOf<Pair<String, Association>>()

    override fun getNewUid(): String = newUidToReturn

    override suspend fun getAssociation(associationId: String): Association? = null

    override suspend fun getAllAssociations(): List<Association> = emptyList()

    override suspend fun createAssociation(association: Association): Result<Unit> {
      createdAssociations.add(association)
      return Result.success(Unit)
    }

    override suspend fun updateAssociation(
        associationId: String,
        newAssociation: Association
    ): Result<Unit> {
      updatedAssociations.add(associationId to newAssociation)
      return Result.success(Unit)
    }

    override suspend fun deleteAssociation(associationId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun getEventsForAssociation(associationId: String) =
        Result.success(emptyList<ch.epfllife.model.event.Event>())

    fun assertCreateCalls(expected: Int) {
      check(createdAssociations.size == expected) {
        "Expected $expected create calls but had ${createdAssociations.size}"
      }
    }

    fun assertUpdateCalls(expected: Int) {
      check(updatedAssociations.size == expected) {
        "Expected $expected update calls but had ${updatedAssociations.size}"
      }
    }
  }
}
