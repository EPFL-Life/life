package ch.epfllife.ui.settings

import ch.epfllife.R
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.EventRepository
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class EditDisplayNameViewModelTest {

  @Mock lateinit var mockUserRepo: UserRepository
  @Mock lateinit var mockEventRepo: EventRepository
  @Mock lateinit var mockAssocRepo: ch.epfllife.model.association.AssociationRepository

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
  fun init_loadsCurrentUser_prefillsDisplayName_andSubmitUpdatesRepoWithTrimmedName_andCallsOnSuccess() =
      runTest {
        val user = ExampleUsers.user1
        whenever(mockUserRepo.getCurrentUser()).thenReturn(user)
        whenever(mockUserRepo.updateUser(any(), any())).thenReturn(Result.success(Unit))

        val db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)

        val viewModel = EditDisplayNameViewModel(db)
        advanceUntilIdle()

        assertEquals(EditDisplayNameUiState.Success, viewModel.uiState.value)
        assertEquals(user.name, viewModel.displayName)
        assertTrue(viewModel.isFormValid())

        val newNameRaw = "  ${ExampleUsers.user2.name}  "
        val expectedTrimmed = newNameRaw.trim()
        viewModel.updateDisplayName(newNameRaw)
        assertTrue(viewModel.isFormValid())

        var onSuccessCalled = false
        viewModel.submit { onSuccessCalled = true }
        advanceUntilIdle()

        val idCaptor = argumentCaptor<String>()
        val userCaptor = argumentCaptor<ch.epfllife.model.user.User>()
        verify(mockUserRepo).updateUser(idCaptor.capture(), userCaptor.capture())

        assertEquals(user.id, idCaptor.firstValue)
        assertEquals(expectedTrimmed, userCaptor.firstValue.name)
        assertTrue(onSuccessCalled)
      }

  @Test
  fun init_whenNoCurrentUser_setsErrorLoadingUser_andSubmitDoesNothing() = runTest {
    whenever(mockUserRepo.getCurrentUser()).thenReturn(null)

    val db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
    val viewModel = EditDisplayNameViewModel(db)
    advanceUntilIdle()

    val state = viewModel.uiState.value
    assertTrue(state is EditDisplayNameUiState.Error)
    assertEquals(R.string.error_loading_user, (state as EditDisplayNameUiState.Error).messageRes)

    assertEquals("", viewModel.displayName)
    assertFalse(viewModel.isFormValid())

    var onSuccessCalled = false
    viewModel.submit { onSuccessCalled = true }
    advanceUntilIdle()

    verify(mockUserRepo, never()).updateUser(any(), any())
    assertFalse(onSuccessCalled)
  }
}
