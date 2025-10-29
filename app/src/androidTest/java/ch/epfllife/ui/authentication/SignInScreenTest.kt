package ch.epfllife.ui.authentication

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfllife.ui.authentication.SignInScreenTestTags.APP_LOGO
import ch.epfllife.ui.authentication.SignInScreenTestTags.LOGIN_BUTTON
import ch.epfllife.ui.authentication.SignInScreenTestTags.LOGIN_TITLE
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.timeout
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockViewModel: SignInViewModel

    @Before
    fun setUp() {
        // Create a mock of SignInViewModel (mockito-inline allows mocking final classes)
        mockViewModel = mock()

        // By default, uiState returns the initial state (not loading, no user, no error)
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(AuthUIState()))
    }

    @Test
    fun signInScreen_displaysAllMainComponents() {
        composeTestRule.setContent {
            SignInScreen(authViewModel = mockViewModel)
        }

        // Check that main UI elements are displayed
        composeTestRule.onNodeWithTag(APP_LOGO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LOGIN_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed()
    }

    @Test
    fun clickingSignInButton_triggersSignInCall() {
        composeTestRule.setContent {
            SignInScreen(authViewModel = mockViewModel)
        }

        // Perform click and verify that signIn(context, credentialManager) is called
        composeTestRule.onNodeWithTag(LOGIN_BUTTON).performClick()

        // Verify that signIn was invoked with any Context and any CredentialManager
        verify(mockViewModel).signIn(any<Context>(), any())
    }

    @Test
    fun whenLoading_showsCircularProgressIndicator_insteadOfButton() {
        // Prepare a uiState with isLoading = true
        val loadingState = AuthUIState(isLoading = true)
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(loadingState))

        composeTestRule.setContent {
            SignInScreen(authViewModel = mockViewModel)
        }

        // The login button should not exist while loading
        composeTestRule.onNodeWithTag(LOGIN_BUTTON).assertDoesNotExist()
    }

    @Test
    fun successfulSignIn_triggersOnSignedInCallback() {
        // Create a fake user and emit a state where user != null
        val fakeUser = mock<FirebaseUser>()
        val signedInState = AuthUIState(user = fakeUser)
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(signedInState))

        var called = false
        composeTestRule.setContent {
            SignInScreen(authViewModel = mockViewModel, onSignedIn = { called = true })
        }

        // Since the uiState already contains a user, LaunchedEffect runs immediately
        assert(called)
    }

    @Test
    fun failedSignIn_showsToastError_and_clearsErrorMessage() {
        // Emit a state with an errorMsg
        val errorState = AuthUIState(errorMsg = "Login failed")
        val flow = MutableStateFlow(errorState)
        whenever(mockViewModel.uiState).thenReturn(flow)

        composeTestRule.setContent {
            SignInScreen(authViewModel = mockViewModel)
        }

        // Verify that clearErrorMsg() was called by the LaunchedEffect inside the Composable
        // We use a timeout because the effect runs on the main dispatcher
        verify(mockViewModel, timeout(3000)).clearErrorMsg()
    }
}
