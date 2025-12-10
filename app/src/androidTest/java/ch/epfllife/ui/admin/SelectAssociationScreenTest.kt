package ch.epfllife.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.UserRole
import ch.epfllife.ui.composables.AssociationCardTestTags
import ch.epfllife.ui.theme.Theme
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class SelectAssociationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysAssociationsFromRepository() {
    val db =
        fakeDbWithAssociationsAndRole(
            UserRole.ADMIN, ExampleAssociations.association1, ExampleAssociations.association2)

    composeTestRule.setContent {
      Theme {
        SelectAssociationScreen(
            db = db, onGoBack = {}, onAssociationSelected = {}, onAddNewAssociation = {})
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(
            AssociationCardTestTags.getAssociationCardTestTag(ExampleAssociations.association1.id))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(
            AssociationCardTestTags.getAssociationCardTestTag(ExampleAssociations.association2.id))
        .assertIsDisplayed()
  }

  @Test
  fun selectingAssociationCallsBackWithCorrectData() {
    val db =
        fakeDbWithAssociationsAndRole(
            UserRole.ADMIN, ExampleAssociations.association1, ExampleAssociations.association2)
    var selectedAssociation: Association? = null

    composeTestRule.setContent {
      Theme {
        SelectAssociationScreen(
            db = db,
            onGoBack = {},
            onAssociationSelected = { selectedAssociation = it },
            onAddNewAssociation = {})
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(
            AssociationCardTestTags.getAssociationCardTestTag(ExampleAssociations.association2.id))
        .performClick()

    assertNotNull(selectedAssociation)
    assertEquals(ExampleAssociations.association2.id, selectedAssociation?.id)
  }

  @Test
  fun addNewAssociationButtonInvokesCallback() {
    val db = fakeDbWithAssociationsAndRole(UserRole.ADMIN, ExampleAssociations.association1)
    var addNewClicked = false

    composeTestRule.setContent {
      Theme {
        SelectAssociationScreen(
            db = db,
            onGoBack = {},
            onAssociationSelected = {},
            onAddNewAssociation = { addNewClicked = true })
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(SelectAssociationTestTags.ADD_NEW_BUTTON).performClick()

    Assert.assertTrue(addNewClicked)
  }

  @Test
  fun addNewAssociationButtonVisibleForAdmin() {
    val db = fakeDbWithAssociationsAndRole(UserRole.ADMIN, ExampleAssociations.association1)
    composeTestRule.setContent {
      Theme {
        SelectAssociationScreen(
            db = db, onGoBack = {}, onAssociationSelected = {}, onAddNewAssociation = {})
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SelectAssociationTestTags.ADD_NEW_BUTTON).assertIsDisplayed()
  }

  @Test
  fun addNewAssociationButtonHiddenForNonAdmin() {
    val db = fakeDbWithAssociationsAndRole(UserRole.USER, ExampleAssociations.association1)
    composeTestRule.setContent {
      Theme {
        SelectAssociationScreen(
            db = db, onGoBack = {}, onAssociationSelected = {}, onAddNewAssociation = {})
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(SelectAssociationTestTags.ADD_NEW_BUTTON).assertDoesNotExist()
  }

  private fun fakeDbWithAssociationsAndRole(role: UserRole, vararg associations: Association): Db {
    val db = Db.freshLocal()
    val assocRepo = db.assocRepo as ch.epfllife.model.association.AssociationRepositoryLocal
    val userRepo = db.userRepo as ch.epfllife.model.user.UserRepositoryLocal

    runBlocking {
      associations.forEach { assocRepo.createAssociation(it) }

      val user =
          when (role) {
            UserRole.ADMIN -> ExampleUsers.adminUser
            UserRole.ASSOCIATION_ADMIN -> ExampleUsers.associationAdminUser
            else -> ExampleUsers.user1
          }
      userRepo.createUser(user)
      userRepo.simulateLogin(user.id)
    }
    return db
  }
}
