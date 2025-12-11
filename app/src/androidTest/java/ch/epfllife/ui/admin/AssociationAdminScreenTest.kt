package ch.epfllife.ui.admin

import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.db.Db
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.FakeToastHelper
import ch.epfllife.utils.SystemToastHelper
import ch.epfllife.utils.ToastHelper
import ch.epfllife.utils.assertTagIsDisplayed
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AssociationAdminScreenTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()
  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private lateinit var decorView: View
  private val fakeToastHelper = FakeToastHelper()
  private val associationId = "asso-sat"

  @Before
  fun setUp() {
    fakeToastHelper.lastMessage = null
    composeTestRule.waitForIdle()
    composeTestRule.activityRule.scenario.onActivity { activity ->
      decorView = activity.window.decorView
    }
  }

  // helper function -> use this so the unit tests stay simple and understandable
  private fun setContent(
      associationId: String? = this.associationId,
      associationName: String? = "Satellite",
      db: Db = Db.freshLocal(),
      onSelectAssociationClick: () -> Unit = {},
      onManageAssociationClick: (String) -> Unit = {},
      onManageAssociationEventsClick: (String) -> Unit = {},
      onAssociationDeleted: () -> Unit = {},
      onGoBack: () -> Unit = {},
      toastHelper: ToastHelper = SystemToastHelper()
  ) {
    composeTestRule.setContent {
      AssociationAdminScreen(
          auth = auth,
          db = db,
          associationId = associationId,
          associationName = associationName,
          onSelectAssociationClick = onSelectAssociationClick,
          onManageAssociationClick = onManageAssociationClick,
          onManageAssociationEventsClick = onManageAssociationEventsClick,
          onAssociationDeleted = onAssociationDeleted,
          onGoBack = onGoBack,
          toastHelper = toastHelper)
    }
  }

  @Test
  fun contentIsDisplayed() {
    setContent()
    listOf(
            AssociationAdminScreenTestTags.SCREEN,
            AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON,
            AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON,
            AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON,
            AssociationAdminScreenTestTags.DELETE_ASSOCIATION_BUTTON)
        .map(composeTestRule::assertTagIsDisplayed)
  }

  @Test
  fun manageButtonsNotVisibleWhenAssociationNotSelected() {
    // Case 1: No association selected
    setContent(associationId = null, associationName = null)
    composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON)
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.DELETE_ASSOCIATION_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun manageButtonsVisibleOnlyWhenAssociationSelected() {
    // Case 2: Association selected
    setContent(associationId = associationId, associationName = "Satellite")
    composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON)
    composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON)
    composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON)
    composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.DELETE_ASSOCIATION_BUTTON)
  }

  @Test
  fun selectAssociationButtonInvokesCallback() {
    var clicked = false
    setContent(onSelectAssociationClick = { clicked = true })
    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON)
        .performClick()
    Assert.assertTrue(clicked)
  }

  @Test
  fun manageButtonsInvokeCallbacks() {
    var manageClickedId: String? = null
    var manageEventsClickedId: String? = null

    setContent(
        onManageAssociationClick = { manageClickedId = it },
        onManageAssociationEventsClick = { manageEventsClickedId = it })

    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON)
        .performClick()
    Assert.assertEquals(associationId, manageClickedId)

    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON)
        .performClick()
    Assert.assertEquals(associationId, manageEventsClickedId)
  }

  @Test
  fun deleteAssociationButtonShowsDialogAndDeletesUser() {
    var deleted = false
    val db = fakeDbWithAssociation(associationId)
    setContent(db = db, toastHelper = fakeToastHelper, onAssociationDeleted = { deleted = true })

    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.DELETE_ASSOCIATION_BUTTON)
        .performClick()

    composeTestRule
        .onNodeWithText(
            composeTestRule.activity.getString(R.string.delete_association_confirmation))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText(composeTestRule.activity.getString(R.string.delete_button))
        .performClick()
    composeTestRule.waitForIdle()

    Assert.assertTrue(deleted)
  }

  @Test
  fun deleteAssociationButtonShowsDialogAndCanCancel() {
    var deleted = false
    val db = fakeDbWithAssociation(associationId)
    setContent(db = db, toastHelper = fakeToastHelper, onAssociationDeleted = { deleted = true })

    composeTestRule
        .onNodeWithTag(AssociationAdminScreenTestTags.DELETE_ASSOCIATION_BUTTON)
        .performClick()

    composeTestRule
        .onNodeWithText(composeTestRule.activity.getString(R.string.cancel_button))
        .performClick()
    composeTestRule.waitForIdle()

    Assert.assertFalse(deleted)
    composeTestRule
        .onNodeWithText(
            composeTestRule.activity.getString(R.string.delete_association_confirmation))
        .assertDoesNotExist()
  }

  // im aware that fake repos are a bad approach but otherwise >80% is impossible ¯\_(ツ)_/¯
  private fun fakeDbWithAssociation(id: String): Db {
    val db = Db.freshLocal()
    val assoc =
        ch.epfllife.model.association.Association(
            id = id,
            name = "Satellite",
            description = "Sat",
            eventCategory = ch.epfllife.model.event.EventCategory.ACADEMIC)
    kotlinx.coroutines.runBlocking { db.assocRepo.createAssociation(assoc) }
    return db
  }
}
