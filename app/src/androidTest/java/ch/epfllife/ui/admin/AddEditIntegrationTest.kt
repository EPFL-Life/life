package ch.epfllife.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.Association
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.LanguageRepository
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.ui.admin.AddEditAssociationTestTags
import ch.epfllife.ui.admin.AssociationAdminScreenTestTags
import ch.epfllife.ui.admin.SelectAssociationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.SettingsScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.navigateToTab
import ch.epfllife.utils.setUpEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddEditIntegrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
  private lateinit var db: Db

  @Before
  fun setUp() {
    db = Db.freshLocal()
    setUpEmulator(auth, "AddEditIntegrationTest")
    runTest {
      val signInResult = auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
      Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
    }
  }

  @Test
  fun createAndUpdateAssociationFromSettings_selectsItOnReturn() {
    seedAdminUser()
    val association = ExampleAssociations.sampleAssociation.copy(name = "New Admin Assoc")
    val languageRepository = LanguageRepository(db.userRepo)
    composeTestRule.setContent { ThemedApp(auth, db, languageRepository) }
    composeTestRule.waitForIdle()
    composeTestRule.navigateToTab(Tab.Settings)

    composeTestRule
        .onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON, useUnmergedTree = true)
        .performClick()

    composeTestRule
        .onNodeWithTag(
            AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON, useUnmergedTree = true)
        .performClick()

    composeTestRule
        .onNodeWithTag(SelectAssociationTestTags.ADD_NEW_BUTTON, useUnmergedTree = true)
        .performClick()

    composeTestRule.enterAssociationForm(association)

    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON, useUnmergedTree = true)
        .performScrollTo()
        .performClick()

    composeTestRule.waitUntilNodeExists(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON)
    composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON)
    composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON)
    composeTestRule
        .onNodeWithText("Manage New Admin Assoc", useUnmergedTree = true)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(
            AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON, useUnmergedTree = true)
        .performClick()

    // Update name of association
    composeTestRule.waitUntilNodeExists(AddEditAssociationTestTags.NAME_FIELD)
    val updatedName = "Updated Admin Assoc"
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.NAME_FIELD, useUnmergedTree = true)
        .performTextClearance()
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.NAME_FIELD, useUnmergedTree = true)
        .performTextInput(updatedName)
    composeTestRule
        .onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON, useUnmergedTree = true)
        .performScrollTo()
        .performClick()

    // Verify updated name is displayed
    composeTestRule
        .onNodeWithText("Manage $updatedName", useUnmergedTree = true)
        .assertIsDisplayed()
  }

  private fun seedAdminUser() {
    val userRepo = db.userRepo as UserRepositoryLocal
    runBlocking {
      userRepo.createUser(ExampleUsers.adminUser)
      userRepo.simulateLogin(ExampleUsers.adminUser.id)
    }
  }

  private fun ComposeContentTestRule.enterAssociationForm(association: Association) {
    fillField(AddEditAssociationTestTags.NAME_FIELD, association.name)
    fillField(AddEditAssociationTestTags.DESCRIPTION_FIELD, association.description)
    fillField(
        AddEditAssociationTestTags.ABOUT_FIELD, association.about ?: "About ${association.name}")
  }

  private fun ComposeContentTestRule.fillField(tag: String, value: String) {
    onNodeWithTag(tag, useUnmergedTree = true).performTextClearance()
    onNodeWithTag(tag, useUnmergedTree = true).performTextInput(value)
  }

  private fun ComposeContentTestRule.waitUntilNodeExists(
      tag: String,
      timeoutMillis: Long = 5000,
  ) {
    waitUntil(timeoutMillis) {
      try {
        onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        true
      } catch (_: AssertionError) {
        false
      }
    }
  }
}
