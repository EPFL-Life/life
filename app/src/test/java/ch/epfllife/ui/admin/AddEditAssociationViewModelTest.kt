package ch.epfllife.ui.admin

import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.EventCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class AssociationMainDispatcherRule(val testDispatcher: TestDispatcher = StandardTestDispatcher()) :
    TestWatcher() {
  override fun starting(description: Description) {
    Dispatchers.setMain(testDispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditAssociationViewModelTest {

  @get:Rule val mainDispatcherRule = AssociationMainDispatcherRule()

  private val db = Db.freshLocal()

  @Test
  fun submit_createsNewAssociation() = runTest {
    // Arrange
    val viewModel = AddEditAssociationViewModel(db, null)
    viewModel.updateName("New Association")
    viewModel.updateDescription("Description")
    viewModel.updateAbout("About")

    var successCalled = false

    // Act
    viewModel.submit { successCalled = true }
    advanceUntilIdle()

    // Assert
    assertTrue(successCalled)
    val associations = db.assocRepo.getAllAssociations()
    assertEquals(1, associations.size)
    assertEquals("New Association", associations[0].name)
    assertEquals(EventCategory.OTHER, associations[0].eventCategory)
  }

  @Test
  fun submit_updatesExistingAssociation() = runTest {
    // Arrange
    val association =
        Association(
            id = "assoc1",
            name = "Old Name",
            description = "Old Description",
            eventCategory = EventCategory.CULTURE,
            about = "Old About")
    db.assocRepo.createAssociation(association)

    val viewModel = AddEditAssociationViewModel(db, "assoc1")
    advanceUntilIdle() // Wait for loadData

    viewModel.updateName("Updated Name")

    var successCalled = false

    // Act
    viewModel.submit { successCalled = true }
    advanceUntilIdle()

    // Assert
    assertTrue(successCalled)
    val updatedAssociation = db.assocRepo.getAssociation("assoc1")
    assertEquals("Updated Name", updatedAssociation?.name)
    assertEquals(EventCategory.CULTURE, updatedAssociation?.eventCategory)
  }

  @Test
  fun updateSocialMedia_updatesEnabledState() {
    val viewModel = AddEditAssociationViewModel(db, null)
    // Use the first available platform to ensure it exists
    val platform = viewModel.formState.socialMedia.first().platform

    viewModel.updateSocialMedia(platform, true)

    val entry = viewModel.formState.socialMedia.find { it.platform == platform }
    assertTrue(entry?.enabled == true)

    viewModel.updateSocialMedia(platform, false)
    val entryDisabled = viewModel.formState.socialMedia.find { it.platform == platform }
    assertTrue(entryDisabled?.enabled == false)
  }

  @Test
  fun updateSocialMediaLink_updatesLink() {
    val viewModel = AddEditAssociationViewModel(db, null)
    val platform = viewModel.formState.socialMedia.first().platform
    val link = "https://example.com/test"

    viewModel.updateSocialMediaLink(platform, link)

    val entry = viewModel.formState.socialMedia.find { it.platform == platform }
    assertEquals(link, entry?.link)
  }

  @Test
  fun updateLogoUrl_updatesLogoUrl() {
    val viewModel = AddEditAssociationViewModel(db, null)
    val url = "https://example.com/logo.png"

    viewModel.updateLogoUrl(url)

    assertEquals(url, viewModel.formState.logoUrl)
  }

  @Test
  fun updateBannerUrl_updatesBannerUrl() {
    val viewModel = AddEditAssociationViewModel(db, null)
    val url = "https://example.com/banner.png"

    viewModel.updateBannerUrl(url)

    assertEquals(url, viewModel.formState.bannerUrl)
  }
}
