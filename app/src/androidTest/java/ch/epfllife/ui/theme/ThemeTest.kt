package ch.epfllife.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeTest {

  /**
   * We use `createAndroidComposeRule` because the `Theme` composable interacts with the Activity's
   * `window` to set the status bar color. `createComposeRule` does not provide access to an
   * Activity.
   */
  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun lightTheme_appliesCorrectColorsAndSystemUi() {
    // Act: Set the content with the light theme
    composeTestRule.setContent {
      Theme(darkTheme = false) {
        // Assert that the correct color scheme is available within the composition
        assertEquals(LightColorScheme, MaterialTheme.colorScheme)
      }
    }

    // Assert: Verify the side effects on the system UI after composition
    val activity = composeTestRule.activity
    val window = activity.window
    val expectedStatusBarColor = LightColorScheme.background.toArgb()
    val areStatusBarsLight =
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars

    assertEquals(
        "Status bar color should match light theme background",
        expectedStatusBarColor,
        window.statusBarColor)
    assertEquals("Status bar icons should be dark for light theme", true, areStatusBarsLight)
  }

  @Test
  fun darkTheme_appliesCorrectColorsAndSystemUi() {
    // Act: Set the content with the dark theme
    composeTestRule.setContent {
      Theme(darkTheme = true) {
        // Assert that the correct color scheme is available within the composition
        assertEquals(DarkColorScheme, MaterialTheme.colorScheme)
      }
    }

    // Assert: Verify the side effects on the system UI after composition
    val activity = composeTestRule.activity
    val window = activity.window
    val expectedStatusBarColor = DarkColorScheme.background.toArgb()
    val areStatusBarsLight =
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars

    assertEquals(
        "Status bar color should match dark theme background",
        expectedStatusBarColor,
        window.statusBarColor)
    assertEquals("Status bar icons should be light for dark theme", false, areStatusBarsLight)
  }
}
