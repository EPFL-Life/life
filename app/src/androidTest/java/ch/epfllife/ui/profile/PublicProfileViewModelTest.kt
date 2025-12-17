package ch.epfllife.ui.profile

import ch.epfllife.R
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PublicProfileViewModelTest {

  @Mock lateinit var mockUserRepo: UserRepository

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    MockitoAnnotations.openMocks(this)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun loadProfile_success_showsUserAndFollowingStatus() = runTest {
    // Arrange
    val targetUser = ExampleUsers.user2
    val currentUser = ExampleUsers.user1.copy(following = listOf(targetUser.id))

    whenever(mockUserRepo.getUser(targetUser.id)).thenReturn(targetUser)
    whenever(mockUserRepo.getCurrentUser()).thenReturn(currentUser)

    // Act
    val viewModel = PublicProfileViewModel(mockUserRepo, targetUser.id)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertTrue(state is PublicProfileUiState.Success)
    val successState = state as PublicProfileUiState.Success
    assertEquals(targetUser, successState.user)
    assertTrue(successState.isFollowing)
  }

  @Test
  fun loadProfile_error_whenUserNotFound() = runTest {
    // Arrange
    whenever(mockUserRepo.getUser("unknown")).thenReturn(null)
    whenever(mockUserRepo.getCurrentUser()).thenReturn(ExampleUsers.user1)

    // Act
    val viewModel = PublicProfileViewModel(mockUserRepo, "unknown")
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertTrue(state is PublicProfileUiState.Error)
    assertEquals(
        R.string.error_user_not_found_or_logged_in, (state as PublicProfileUiState.Error).message)
  }

  @Test
  fun toggleFollow_callsRepoAndReloads() = runTest {
    // Arrange
    val targetUser = ExampleUsers.user2
    // Initially NOT following
    var currentUser = ExampleUsers.user1.copy(following = emptyList())

    whenever(mockUserRepo.getUser(targetUser.id)).thenReturn(targetUser)
    whenever(mockUserRepo.getCurrentUser()).thenReturn(currentUser)
    whenever(mockUserRepo.followUser(targetUser.id)).thenReturn(Result.success(Unit))

    val viewModel = PublicProfileViewModel(mockUserRepo, targetUser.id)
    advanceUntilIdle()

    // Check initial state
    var state = viewModel.uiState.value as PublicProfileUiState.Success
    assertTrue(!state.isFollowing)

    // Update mock for reload
    whenever(mockUserRepo.getCurrentUser())
        .thenReturn(currentUser.copy(following = listOf(targetUser.id)))

    // Act
    viewModel.toggleFollow()
    advanceUntilIdle()

    // Assert
    verify(mockUserRepo).followUser(targetUser.id)
    // Should have reloaded
    state = viewModel.uiState.value as PublicProfileUiState.Success
    assertTrue(state.isFollowing)
  }
}
