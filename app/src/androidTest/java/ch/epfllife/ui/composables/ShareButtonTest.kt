package ch.epfllife.ui.composables

import android.content.Context
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import ch.epfllife.R
import ch.epfllife.ui.theme.Theme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ShareButtonTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun shareButton_isDisplayed_withShareContentDescription_andIsClickable() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val shareContentDescription = context.getString(R.string.share)

    composeTestRule.setContent { Theme { ShareButton(onShare = {}) } }

    composeTestRule
        .onNodeWithContentDescription(shareContentDescription)
        .assertIsDisplayed()
        .assertHasClickAction()
  }

  @Test
  fun shareButton_triggersCallback_whenClicked() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val shareContentDescription = context.getString(R.string.share)
    var clicks = 0

    composeTestRule.setContent { Theme { ShareButton(onShare = { clicks += 1 }) } }

    composeTestRule.onNodeWithContentDescription(shareContentDescription).performClick()

    composeTestRule.runOnIdle { assertEquals(1, clicks) }
  }
}