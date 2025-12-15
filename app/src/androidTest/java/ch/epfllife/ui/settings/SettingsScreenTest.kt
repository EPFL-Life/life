package ch.epfllife.ui.settings

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.model.user.UserRole
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.FakeToastHelper
import ch.epfllife.utils.assertTagIsDisplayed
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private lateinit var decorView: View
  private val fakeToastHelper = FakeToastHelper()

  @Before
  fun setUp() {
    fakeToastHelper.lastMessage = null
    composeTestRule.waitForIdle()
    composeTestRule.activityRule.scenario.onActivity { activity ->
      decorView = activity.window.decorView
    }
  }

  @Test
  fun contentIsDisplayed() {
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          onSignedOut = {},
          onAdminConsoleClick = {},
          onNavigateToLanguageSelection = {},
          onNavigateToDisplayName = {})
    }
    listOf(NavigationTestTags.SETTINGS_SCREEN, SettingsScreenTestTags.SIGN_OUT_BUTTON)
        .map(composeTestRule::assertTagIsDisplayed)
  }

  @org.junit.Ignore("Requires Firebase Emulator")
  @Test
  fun canSignOut() {
    Assert.assertNotNull(Firebase.auth.currentUser)
    var clicked = false
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          onSignedOut = { clicked = true },
          toastHelper = fakeToastHelper,
          onAdminConsoleClick = {},
          onNavigateToLanguageSelection = {},
          onNavigateToDisplayName = {})
    }
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SIGN_OUT_BUTTON).performClick()
    composeTestRule.waitForIdle()
    Assert.assertTrue(clicked)
    Assert.assertNull(Firebase.auth.currentUser)
    Assert.assertEquals(
        composeTestRule.activity.getString(R.string.signout_successful),
        fakeToastHelper.lastMessage)
  }

  @Test
  fun adminConsoleButtonInvokesCallback() {
    val db = fakeDbWithUserRole(UserRole.ADMIN)
    var clicked = false
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          viewModel = SettingsViewModel(auth, db),
          onSignedOut = {},
          onAdminConsoleClick = { clicked = true },
          onNavigateToLanguageSelection = {},
          onNavigateToDisplayName = {})
    }

    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).performClick()
    Assert.assertTrue(clicked)
  }

  @Test
  fun adminConsoleButtonHiddenForUser() {
    val db = fakeDbWithUserRole(UserRole.USER)
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          viewModel = SettingsViewModel(auth, db),
          onSignedOut = {},
          onAdminConsoleClick = {},
          onNavigateToLanguageSelection = {},
          onNavigateToDisplayName = {})
    }
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).assertDoesNotExist()
  }

  @Test
  fun adminConsoleButtonVisibleForAdmin() {
    val db = fakeDbWithUserRole(UserRole.ADMIN)
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          viewModel = SettingsViewModel(auth, db),
          onSignedOut = {},
          onAdminConsoleClick = {},
          onNavigateToLanguageSelection = {},
          onNavigateToDisplayName = {})
    }
    composeTestRule.waitUntil(5000) {
      try {
        composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).isDisplayed()
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun adminConsoleButtonVisibleForAssociationAdmin() {
    val db = fakeDbWithUserRole(UserRole.ASSOCIATION_ADMIN)
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          viewModel = SettingsViewModel(auth, db),
          onSignedOut = {},
          onAdminConsoleClick = {},
          onNavigateToLanguageSelection = {},
          onNavigateToDisplayName = {})
    }
    composeTestRule.waitUntil(5000) {
      try {
        composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).isDisplayed()
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun displayNameButtonInvokesCallback() {
    val db = fakeDbWithUserRole(UserRole.USER)
    var clicked = false

    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          viewModel = SettingsViewModel(auth, db),
          onSignedOut = {},
          onAdminConsoleClick = {},
          onNavigateToLanguageSelection = {},
          onNavigateToDisplayName = { clicked = true })
    }

    val displayNameLabel = composeTestRule.activity.getString(R.string.display_name)
    composeTestRule.onNode(hasText(displayNameLabel) and hasClickAction()).performClick()

    Assert.assertTrue(clicked)
  }

  private fun fakeDbWithUserRole(role: UserRole): Db {
    val db = Db.freshLocal()
    val userRepo = db.userRepo as UserRepositoryLocal
    val user = User(id = "0", name = "Test User", role = role)
    runBlocking {
      userRepo.createUser(user)
      userRepo.simulateLogin(user.id)
    }
    return db
  }
}
