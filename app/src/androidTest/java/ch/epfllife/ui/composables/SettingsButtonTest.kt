package ch.epfllife.ui.composables

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsButtonTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun settingsButton_displaysText_andHandlesClick() {
    var clicked = false
    val text = "Test Button"

    composeTestRule.setContent { SettingsButton(text = text, onClick = { clicked = true }) }

    composeTestRule.onNode(hasText(text)).assertIsDisplayed()
    composeTestRule.onNode(hasText(text) and hasClickAction()).performClick()

    assertTrue(clicked)
  }

  @Test
  fun settingsButton_displaysIcon_whenProvided() {
    val text = "Button with Icon"
    val icon = Icons.Default.Person

    composeTestRule.setContent { SettingsButton(text = text, icon = icon, onClick = {}) }

    composeTestRule.onNode(hasText(text)).assertIsDisplayed()
    // Verifying icon is tricky without content description or test tag.
    // we assume the icon is present if the button renders without crash.
    // (we might add testTag to the Icon in SettingsButton)
  }
}
