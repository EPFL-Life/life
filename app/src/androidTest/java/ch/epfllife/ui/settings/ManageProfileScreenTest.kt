package ch.epfllife.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import ch.epfllife.R
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.ui.theme.Theme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ManageProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun prefillsDisplayName_togglesSubmitEnabled_andSubmittingUpdatesUserAndCallsOnSubmitSuccess() {
    val db = Db.freshLocal()
    val userRepo = db.userRepo as UserRepositoryLocal
    val user = ExampleUsers.user1

    runBlocking {
      userRepo.createUser(user)
      userRepo.simulateLogin(user.id)
    }

    var submitCalled = false
    composeTestRule.setContent {
      Theme { ManageProfileScreen(db = db, onBack = {}, onSubmitSuccess = { submitCalled = true }) }
    }

    val title = "Manage Profile" // Hardcoded in screen
    val submitLabel = composeTestRule.activity.getString(R.string.submit)

    composeTestRule.waitUntil(5_000) {
      try {
        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        true
      } catch (_: AssertionError) {
        false
      }
    }

    composeTestRule
        .onNode(hasSetTextAction() and hasText(user.name, substring = false))
        .assertIsDisplayed()

    composeTestRule.onNodeWithText(submitLabel).assertIsEnabled()

    composeTestRule.onNode(hasSetTextAction()).performTextClearance()
    // Depending on logic, empty might disable submit or validaiton
    composeTestRule.onNodeWithText(submitLabel).assertIsNotEnabled()

    val newNameRaw = "  ${ExampleUsers.user2.name}  "
    val expectedTrimmed = newNameRaw.trim()
    composeTestRule.onNode(hasSetTextAction()).performTextInput(newNameRaw)
    composeTestRule.onNodeWithText(submitLabel).assertIsEnabled()

    // Submit
    composeTestRule.onNodeWithText(submitLabel).performClick()

    waitUntilTrue {
      val updatedName = runBlocking { db.userRepo.getUser(user.id)?.name }
      submitCalled && updatedName == expectedTrimmed
    }

    val updatedName = runBlocking { db.userRepo.getUser(user.id)?.name }
    assertEquals(expectedTrimmed, updatedName)
    assertTrue(submitCalled)
  }

  @Test
  fun submitFailure_showsErrorState_andBackButtonInvokesOnBack() {
    val db = Db.freshLocal()
    val userRepo = db.userRepo as UserRepositoryLocal
    val user = ExampleUsers.user1

    runBlocking {
      userRepo.createUser(user)
      userRepo.simulateLogin(user.id)
    }

    val viewModel = ManageProfileViewModel(db)
    viewModel.awaitUiStateSuccess()

    runBlocking { userRepo.deleteUser(user.id) }

    var backCalled = false
    composeTestRule.setContent {
      Theme {
        ManageProfileScreen(
            db = db, viewModel = viewModel, onBack = { backCalled = true }, onSubmitSuccess = {})
      }
    }

    val submitLabel = composeTestRule.activity.getString(R.string.submit)
    val backDesc = composeTestRule.activity.getString(R.string.back_button_description)
    val updateErrorText = composeTestRule.activity.getString(R.string.error_updating_profile)

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(submitLabel).assertIsEnabled()
    composeTestRule.onNodeWithText(submitLabel).performClick()

    composeTestRule.waitUntil(5_000) {
      try {
        composeTestRule.onNodeWithText(updateErrorText).assertIsDisplayed()
        true
      } catch (_: AssertionError) {
        false
      }
    }

    composeTestRule.onNodeWithText(updateErrorText).assertIsDisplayed()

    composeTestRule.onNodeWithContentDescription(backDesc).performClick()
    assertTrue(backCalled)
  }

  private fun waitUntilTrue(timeoutMillis: Long = 20_000, condition: () -> Boolean) {
    val timeoutAt = System.currentTimeMillis() + timeoutMillis
    while (!condition()) {
      if (System.currentTimeMillis() >= timeoutAt) {
        error("Condition not met within ${timeoutMillis}ms")
      }
      Thread.sleep(50)
    }
  }

  private fun ManageProfileViewModel.awaitUiStateSuccess(timeoutMillis: Long = 5_000) {
    val timeoutAt = System.currentTimeMillis() + timeoutMillis
    while (uiState.value != ManageProfileUiState.Success) {
      if (System.currentTimeMillis() >= timeoutAt) {
        error("ManageProfileScreen did not load within ${timeoutMillis}ms")
      }
      Thread.sleep(50)
    }
  }
}
