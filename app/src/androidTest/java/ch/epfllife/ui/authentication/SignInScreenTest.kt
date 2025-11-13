package ch.epfllife.ui.authentication

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialUnknownException
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.user.UserRepositoryFirestore
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.assertToastMessage
import ch.epfllife.utils.setUpEmulatorAuth
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.times

class SignInScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private lateinit var decorView: View

  @Before
  fun setUp() {
    // We need to wait for toasts to disappear before each test,
    // otherwise new toasts might not be displayed.
    composeTestRule.waitForIdle()
    composeTestRule.activityRule.scenario.onActivity { activity ->
      decorView = activity.window.decorView
    }
    setUpEmulatorAuth(auth, "SignInScreenTest")
  }

  @Test
  fun contentIsDisplayed() {
    composeTestRule.setContent { SignInScreen(auth, onSignedIn = {}) }
    listOf(
            NavigationTestTags.SIGN_IN_SCREEN,
            SignInScreenTestTags.SIGN_IN_APP_LOGO,
            SignInScreenTestTags.SIGN_IN_TITLE,
            SignInScreenTestTags.SIGN_IN_BUTTON,
            SignInScreenTestTags.GOOGLE_LOGO,
        )
        .map(composeTestRule::assertTagIsDisplayed)
  }

  @Test
  fun canSignIn() {
    Assert.assertNull(Firebase.auth.currentUser)
    var clicked = false
    composeTestRule.setContent { SignInScreen(auth, onSignedIn = { clicked = true }) }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).performClick()
    composeTestRule.waitUntil(5000) { clicked }
    Assert.assertNotNull(Firebase.auth.currentUser)
    assertToastMessage(decorView, R.string.signin_success_message)
  }

  @Test
  fun canHandleSignInFailureUserCollision() =
      assertSignInProblem(
          testAuth =
              createMockAuthWithSignInTaskResult(
                  Tasks.forException(FirebaseAuthUserCollisionException("|o|", "|o|"))),
          message = R.string.signin_failure_message,
      )

  @Test
  fun canHandleSignInFailureInvalidUser() =
      assertSignInProblem(
          testAuth =
              createMockAuthWithSignInTaskResult(
                  Tasks.forException(FirebaseAuthInvalidUserException("|o|", "|o|"))),
          message = R.string.signin_failure_message,
      )

  @Test
  fun canHandleSignInFailureInvalidCredentials() =
      assertSignInProblem(
          testAuth =
              createMockAuthWithSignInTaskResult(
                  Tasks.forException(FirebaseAuthInvalidCredentialsException("|o|", "|o|"))),
          message = R.string.signin_failure_message,
      )

  @Test
  fun canHandleSignInFailureTaskCancellation() =
      assertSignInProblem(
          testAuth = createMockAuthWithSignInTaskResult(Tasks.forCanceled()),
          message = R.string.signin_failure_message,
      )

  @Test
  fun canHandleSignInFailureNoUser() {
    val mockAuthResult = mock(AuthResult::class.java)
    `when`(mockAuthResult.user).thenReturn(null)
    assertSignInProblem(
        testAuth = createMockAuthWithSignInTaskResult(Tasks.forResult(mockAuthResult)),
        message = R.string.signin_failure_message,
    )
  }

  @Test
  fun canHandleSignInCredentialException() =
      assertSignInProblem(
          testAuth = createMockAuthWithCredentialException(GetCredentialUnknownException()),
          message = R.string.signin_failure_message,
      )

  @Test
  fun canHandleSignInCancellation() =
      assertSignInProblem(
          testAuth = createMockAuthWithCredentialException(GetCredentialCancellationException()),
          message = R.string.signin_cancelled_message,
      )

  @Test
  fun firstSignIn_createsUserInRepository() = runTest {
    var onSignedInCalled = false

    // Arrange
    val firestore = FirebaseFirestore.getInstance()
    firestore.useEmulator("10.0.2.2", 8080)
    firestore.clearPersistence() // optional, start clean

    val repo = UserRepositoryFirestore(firestore)
    val viewModel = SignInViewModel(auth, repo)

    // Act
    composeTestRule.setContent {
      SignInScreen(auth = auth, authViewModel = viewModel, onSignedIn = { onSignedInCalled = true })
    }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).performClick()
    composeTestRule.waitUntil(5000) { onSignedInCalled }

    // Assert
    val uid = Firebase.auth.currentUser?.uid!!
    val snapshot = firestore.collection("users").document(uid).get().await()
    Assert.assertTrue(snapshot.exists())

    // cleanup emulator
    repo.deleteUser(uid)
  }

  private fun createMockAuthWithCredentialException(exception: Exception): Auth {
    val mockCredentialManager = mockk<FakeCredentialManager>(relaxed = true)
    coEvery {
      mockCredentialManager.getCredential(
          any(),
          any<GetCredentialRequest>(),
      )
    } throws exception
    return Auth(credentialManager = mockCredentialManager)
  }

  private fun createMockAuthWithSignInTaskResult(task: Task<AuthResult>): Auth {
    val mockFirebaseAuth = mock(FirebaseAuth::class.java)
    `when`(mockFirebaseAuth.signInWithCredential(org.mockito.kotlin.any())).thenReturn(task)
    return Auth(
        credentialManager = FakeCredentialManager.withDefaultTestUser,
        auth = mockFirebaseAuth,
    )
  }

  private fun assertSignInProblem(testAuth: Auth, message: Int) {
    var onSignedInCalled = false
    composeTestRule.setContent { SignInScreen(testAuth, onSignedIn = { onSignedInCalled = true }) }
    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_BUTTON).performClick()
    composeTestRule.waitForIdle()
    Assert.assertFalse(onSignedInCalled)
    assertToastMessage(decorView, message)
  }
}
