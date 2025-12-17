package ch.epfllife.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.model.user.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ManageFriendsScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun displaysUserList_whenUsersExist() {
    val db = Db.freshLocal()
    val repo = db.userRepo as UserRepositoryLocal

    runBlocking {
      repo.createUser(User(id = "me", name = "Me", role = UserRole.USER))
      repo.createUser(User(id = "other", name = "Other User", role = UserRole.USER))
      repo.simulateLogin("me")
    }

    composeTestRule.setContent { ManageFriendsScreen(db = db, onBack = {}, onUserClick = {}) }

    // Wait for loading to finish
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(ManageFriendsTestTags.USER_LIST).assertIsDisplayed()
        true
      } catch (e: AssertionError) {
        false
      }
    }

    composeTestRule.onNodeWithText("Other User").assertIsDisplayed()
    composeTestRule.onNodeWithText("Me").assertDoesNotExist() // Should be filtered out
  }

  @Test
  fun onUserClick_callsCallback() {
    val db = Db.freshLocal()
    val repo = db.userRepo as UserRepositoryLocal

    runBlocking {
      repo.createUser(User(id = "me", name = "Me", role = UserRole.USER))
      repo.createUser(User(id = "other", name = "Other User", role = UserRole.USER))
      repo.simulateLogin("me")
    }

    var clickedId = ""
    composeTestRule.setContent {
      ManageFriendsScreen(db = db, onBack = {}, onUserClick = { clickedId = it })
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(ManageFriendsTestTags.USER_LIST).assertIsDisplayed()
        true
      } catch (e: AssertionError) {
        false
      }
    }

    composeTestRule.onNodeWithText("Other User").performClick()
    assertTrue(clickedId == "other")
  }

  @Test
  fun searchBar_filtersUsers() {
    val db = Db.freshLocal()
    val repo = db.userRepo as UserRepositoryLocal

    runBlocking {
      repo.createUser(User(id = "me", name = "Me", role = UserRole.USER))
      repo.createUser(User(id = "alice", name = "Alice", role = UserRole.USER))
      repo.createUser(User(id = "bob", name = "Bob", role = UserRole.USER))
      repo.simulateLogin("me")
    }

    composeTestRule.setContent { ManageFriendsScreen(db = db, onBack = {}, onUserClick = {}) }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(ManageFriendsTestTags.USER_LIST).assertIsDisplayed()
        true
      } catch (e: AssertionError) {
        false
      }
    }

    // Initial state: both visible
    composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bob").assertIsDisplayed()

    // here we should somehow type a name into searchbar an check the other is not displayed but
    // cant figure it out...

  }
}
