package ch.epfllife.ui.admin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.model.association.Association
import ch.epfllife.model.association.AssociationRepository
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.ui.theme.Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class SelectAssociationScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysAssociationsFromRepository() {
    val db =
        fakeDbWithAssociations(ExampleAssociations.association1, ExampleAssociations.association2)

    composeTestRule.setContent {
      Theme {
        SelectAssociationScreen(
            db = db, onGoBack = {}, onAssociationSelected = {}, onAddNewAssociation = {})
      }
    }

    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(
              hasTestTag(
                  SelectAssociationTestTags.associationCard(ExampleAssociations.association1.id)))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(
            SelectAssociationTestTags.associationCard(ExampleAssociations.association1.id))
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(
            SelectAssociationTestTags.associationCard(ExampleAssociations.association2.id))
        .assertIsDisplayed()
  }

  @Test
  fun selectingAssociationCallsBackWithCorrectData() {
    val db =
        fakeDbWithAssociations(ExampleAssociations.association1, ExampleAssociations.association2)
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

    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(
              hasTestTag(
                  SelectAssociationTestTags.associationCard(ExampleAssociations.association2.id)))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onNodeWithTag(
            SelectAssociationTestTags.associationCard(ExampleAssociations.association2.id))
        .performClick()

    composeTestRule.waitUntil(5_000) { selectedAssociation != null }
    assertNotNull(selectedAssociation)
    assertEquals(ExampleAssociations.association2.id, selectedAssociation?.id)
  }

  private fun fakeDbWithAssociations(vararg associations: Association): Db {
    val baseDb = Db.freshLocal()
    val fakeRepo = FakeAssociationRepository(associations.toList())
    return baseDb.copy(assocRepo = fakeRepo)
  }

  @Test
  fun addNewAssociationButtonInvokesCallback() {
    val db = fakeDbWithAssociations(ExampleAssociations.association1)
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

    composeTestRule.waitUntil(5_000) {
      composeTestRule
          .onAllNodes(hasTestTag(SelectAssociationTestTags.ADD_NEW_BUTTON))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(SelectAssociationTestTags.ADD_NEW_BUTTON).performClick()

    composeTestRule.waitUntil(5_000) { addNewClicked }
  }
}

private class FakeAssociationRepository(
    private val associations: List<Association>,
) : AssociationRepository {

  override suspend fun getAllAssociations(): List<Association> = associations

  override suspend fun getAssociation(associationId: String): Association? =
      associations.firstOrNull { it.id == associationId }

  override fun getNewUid(): String = "fake-id"

  override suspend fun createAssociation(association: Association): Result<Unit> =
      Result.failure(UnsupportedOperationException("Not needed"))

  override suspend fun deleteAssociation(associationId: String): Result<Unit> =
      Result.failure(UnsupportedOperationException("Not needed"))

  override suspend fun updateAssociation(
      associationId: String,
      newAssociation: Association,
  ): Result<Unit> = Result.failure(UnsupportedOperationException("Not needed"))

  override suspend fun getEventsForAssociation(associationId: String): Result<List<Event>> =
      Result.success(emptyList())
}
