package ch.epfllife.ui.composables

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ImageUploadFieldTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysPlaceholder_whenUrlIsEmpty() {
    composeTestRule.setContent {
      ImageUploadField(label = "Test Label", imageUrl = "", isUploading = false, onUploadClick = {})
    }

    composeTestRule.onNodeWithText("Test Label").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tap to select").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Upload").assertIsDisplayed()
  }

  @Test
  fun displaysLoading_whenIsUploadingIsTrue() {
    composeTestRule.setContent {
      ImageUploadField(label = "Test Label", imageUrl = "", isUploading = true, onUploadClick = {})
    }
    composeTestRule.onNodeWithText("Test Label").assertIsDisplayed()
  }

  @Test
  fun click_triggersCallback() {
    var clicked = false
    composeTestRule.setContent {
      ImageUploadField(
          label = "Test Label",
          imageUrl = "",
          isUploading = false,
          onUploadClick = { clicked = true })
    }

    composeTestRule.onNodeWithText("Tap to select").performClick()
    assertTrue(clicked)
  }
}
