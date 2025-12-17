package ch.epfllife.ui.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.model.user.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class PublicProfileScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun displaysUserProfile() {
    val db = Db.freshLocal()
    val repo = db.userRepo as UserRepositoryLocal
    val targetUser = User(id = "target", name = "Target User", role = UserRole.USER)

    runBlocking {
      repo.createUser(User(id = "me", name = "Me", role = UserRole.USER))
      repo.createUser(targetUser)
      repo.simulateLogin("me")
    }

    composeTestRule.setContent { PublicProfileScreen(db = db, userId = targetUser.id, onBack = {}) }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag(PublicProfileTestTags.NAME_TEXT).isDisplayed()
    }

    composeTestRule.onNodeWithTag(PublicProfileTestTags.NAME_TEXT).assertTextEquals("Target User")
    // Should show Follow button initially (not following)
    composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).assertTextEquals("Follow")
  }

  @Test
  fun displaysUnfollow_whenAlreadyFollowing() {
    val db = Db.freshLocal()
    val repo = db.userRepo as UserRepositoryLocal
    val targetUser = User(id = "target", name = "Target User", role = UserRole.USER)
    // "me" follows "target"
    val me = User(id = "me", name = "Me", role = UserRole.USER, following = listOf("target"))

    runBlocking {
      repo.createUser(me)
      repo.createUser(targetUser)
      repo.simulateLogin("me")
    }

    composeTestRule.setContent { PublicProfileScreen(db = db, userId = targetUser.id, onBack = {}) }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).isDisplayed()
    }

    composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).assertTextEquals("Unfollow")
  }

  @Test
  fun clickingFollowButton_togglesState() {
    val db = Db.freshLocal()
    val repo = db.userRepo as UserRepositoryLocal
    val targetUser = User(id = "target", name = "Target User", role = UserRole.USER)

    runBlocking {
      repo.createUser(User(id = "me", name = "Me", role = UserRole.USER))
      repo.createUser(targetUser)
      repo.simulateLogin("me")
    }

    composeTestRule.setContent { PublicProfileScreen(db = db, userId = targetUser.id, onBack = {}) }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).isDisplayed()
    }

    // Initial: Follow
    composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).assertTextEquals("Follow")

    // Click
    composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).performClick()

    // Wait for update (Unfollow)
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onNode(hasTestTag(PublicProfileTestTags.FOLLOW_BUTTON).and(hasText("Unfollow")))
          .isDisplayed()
    }

    // Click again (Unfollow -> Follow)
    composeTestRule.onNodeWithTag(PublicProfileTestTags.FOLLOW_BUTTON).performClick()

    // Wait for update (Follow)
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onNode(hasTestTag(PublicProfileTestTags.FOLLOW_BUTTON).and(hasText("Follow")))
          .isDisplayed()
    }
  }
}
