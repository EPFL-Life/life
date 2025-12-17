package ch.epfllife.ui.settings

import ch.epfllife.R
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepository
import ch.epfllife.model.user.UserRole
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
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ManageFriendsViewModelTest {

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
  fun loadUsers_success_showsAdminsButFiltersSelf() = runTest {
    // Arrange
    val currentUser = ExampleUsers.user1.copy(role = UserRole.USER)
    val otherUser = ExampleUsers.user2.copy(role = UserRole.USER)
    val adminUser = User(id = "admin", name = "Admin", role = UserRole.ADMIN, photoUrl = null)

    whenever(mockUserRepo.getCurrentUser()).thenReturn(currentUser)
    whenever(mockUserRepo.getAllUsers()).thenReturn(listOf(currentUser, otherUser, adminUser))

    // Act
    val viewModel = ManageFriendsViewModel(mockUserRepo)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertTrue(state is ManageFriendsUiState.Success)
    val successState = state as ManageFriendsUiState.Success
    assertEquals(2, successState.users.size)
    assertTrue(successState.users.contains(otherUser))
    assertTrue(successState.users.contains(adminUser))
    assertTrue(!successState.users.contains(currentUser))
  }

  @Test
  fun loadUsers_error_whenNotLoggedIn() = runTest {
    // Arrange
    whenever(mockUserRepo.getCurrentUser()).thenReturn(null)

    // Act
    val viewModel = ManageFriendsViewModel(mockUserRepo)
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertTrue(state is ManageFriendsUiState.Error)
    assertEquals(R.string.user_not_logged_in, (state as ManageFriendsUiState.Error).message)
  }

  @Test
  fun onSearchQueryChanged_filtersList() = runTest {
    // Arrange
    val currentUser = ExampleUsers.user1
    val u1 = User(id = "2", name = "Alice", role = UserRole.USER, photoUrl = null)
    val u2 = User(id = "3", name = "Bob", role = UserRole.USER, photoUrl = null)

    whenever(mockUserRepo.getCurrentUser()).thenReturn(currentUser)
    whenever(mockUserRepo.getAllUsers()).thenReturn(listOf(currentUser, u1, u2))

    val viewModel = ManageFriendsViewModel(mockUserRepo)
    advanceUntilIdle()

    // Act
    viewModel.onSearchQueryChanged("Ali")
    advanceUntilIdle()

    // Assert
    val state = viewModel.uiState.value
    assertTrue(state is ManageFriendsUiState.Success)
    val users = (state as ManageFriendsUiState.Success).users
    assertEquals(1, users.size)
    assertEquals(u1, users.first())
  }
}
