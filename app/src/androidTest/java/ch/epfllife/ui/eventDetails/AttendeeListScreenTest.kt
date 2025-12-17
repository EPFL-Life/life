package ch.epfllife.ui.eventDetails

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.db.Db
import ch.epfllife.ui.theme.Theme
import org.junit.Rule
import org.junit.Test

class AttendeeListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun showsHeaderAndAttendeeNames() {
    val attendees = listOf(ExampleUsers.user1, ExampleUsers.user2)

    val db = Db.freshLocal()
    composeTestRule.setContent {
      Theme { AttendeeListScreen(attendees = attendees, onBack = {}, db = db) }
    }

    composeTestRule.onNodeWithText("Event Attendees").assertIsDisplayed()
    composeTestRule.onNodeWithText(attendees[0].name).assertIsDisplayed()
    composeTestRule.onNodeWithText(attendees[1].name).assertIsDisplayed()
  }
}
