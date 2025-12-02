package ch.epfllife.ui.settings

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.FakeToastHelper
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.setUpEmulator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.test.runTest
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
    setUpEmulator(auth, "SettingsScreenTest")
    fakeToastHelper.lastMessage = null
    composeTestRule.waitForIdle()
    composeTestRule.activityRule.scenario.onActivity { activity ->
      decorView = activity.window.decorView
    }
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
  }

  @Test
  fun contentIsDisplayed() {
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          onSignedOut = {},
          onSelectAssociationClick = {},
          onManageAssociationClick = {},
          onManageAssociationEventsClick = {},
          onAddNewAssociationClick = {})
    }
    listOf(
            NavigationTestTags.SETTINGS_SCREEN,
            SettingsScreenTestTags.SIGN_OUT_BUTTON,
        )
        .map(composeTestRule::assertTagIsDisplayed)
  }

  @Test
  fun canSignOut() {
    Assert.assertNotNull(Firebase.auth.currentUser)
    var clicked = false
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          onSignedOut = { clicked = true },
          toastHelper = fakeToastHelper,
          onSelectAssociationClick = {},
          onManageAssociationClick = {},
          onManageAssociationEventsClick = {},
          onAddNewAssociationClick = {})
    }
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SIGN_OUT_BUTTON).performClick()
    composeTestRule.waitForIdle()
    Assert.assertNull(Firebase.auth.currentUser)
    Assert.assertEquals(
        composeTestRule.activity.getString(R.string.signout_successful),
        fakeToastHelper.lastMessage)
  }

  @Test
  fun selectAssociationButtonInvokesCallback() {
    var clicked = false
    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          onSignedOut = {},
          onSelectAssociationClick = { clicked = true },
          onManageAssociationClick = {},
          onManageAssociationEventsClick = {},
          onAddNewAssociationClick = {})
    }

    composeTestRule.onNodeWithTag(SettingsScreenTestTags.SELECT_ASSOCIATION_BUTTON).performClick()
    Assert.assertTrue(clicked)
  }

  @Test
  fun manageButtonsVisibleAndClickableWhenAssociationSelected() {
    val associationId = "asso-sat"
    val associationName = "Satellite"
    var manageClickedId: String? = null
    var manageEventsClickedId: String? = null

    composeTestRule.setContent {
      SettingsScreen(
          auth = auth,
          onSignedOut = {},
          selectedAssociationId = associationId,
          selectedAssociationName = associationName,
          onSelectAssociationClick = {},
          onManageAssociationClick = { manageClickedId = it },
          onManageAssociationEventsClick = { manageEventsClickedId = it },
          onAddNewAssociationClick = {})
    }

    composeTestRule.onNodeWithTag(SettingsScreenTestTags.MANAGE_ASSOCIATION_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SettingsScreenTestTags.MANAGE_EVENTS_BUTTON).performClick()

    Assert.assertEquals(associationId, manageClickedId)
    Assert.assertEquals(associationId, manageEventsClickedId)
  }
}
