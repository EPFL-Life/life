package ch.epfllife.ui.admin

import android.net.Uri
import ch.epfllife.model.db.Db
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AddEditAssociationViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private val db = Db.freshLocal()

  // Helper to generic a mock Uri
  private fun mockUri(stringUri: String): Uri {
    val uri = mockk<Uri>()
    io.mockk.every { uri.toString() } returns stringUri
    return uri
  }

  @Test
  fun onLogoSelected_uploadsImageAndUpdatesState() = runTest {
    // Arrange
    val viewModel = AddEditAssociationViewModel(db)
    val uriString = "file://logo.jpg"
    val uri = mockUri(uriString)

    // Act
    viewModel.onLogoSelected(uri)

    // Advance time to ensure coroutine executes
    advanceTimeBy(1000)

    // Assert
    assertEquals(uriString, viewModel.formState.logoUrl)
  }

  @Test
  fun onBannerSelected_uploadsImageAndUpdatesState() = runTest {
    // Arrange
    val viewModel = AddEditAssociationViewModel(db)
    val uriString = "file://banner.jpg"
    val uri = mockUri(uriString)

    // Act
    viewModel.onBannerSelected(uri)
    advanceTimeBy(1000)

    // Assert
    assertEquals(uriString, viewModel.formState.bannerUrl)
  }
}
