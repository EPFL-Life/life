package ch.epfllife.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfllife.ui.navigation.NavigationActions
import ch.epfllife.ui.navigation.Screen
import ch.epfllife.ui.theme.Theme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

/**
 * UI tests for the HomeScreen.
 *
 * These tests verify that the main UI components of the home screen are displayed
 * and that the navigation actions are correctly triggered when the user interacts
 * with the navigation bar.
 *
 * Assumptions:
 * - The HomeScreen has a TopAppBar with the title "Home".
 * - The HomeScreen contains a main content area identified by a test tag "HomeScreenContent".
 * - A BottomNavigation bar is present with items that have specific content descriptions
 * for navigating to other screens.
 */
@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Mock the NavigationActions to verify calls without performing actual navigation
    private val mockNavigationActions: NavigationActions = mock()

    @Before
    fun setUp() {
        // Set the content for all tests in this class
        composeTestRule.setContent {
            Theme {
                HomeScreen(navigationActions = mockNavigationActions)
            }
        }
    }

    @Test
    fun homeScreen_displaysTopBarTitle() {
        // Assert that the title "Home" is visible on the screen.
        // This covers the rendering of the TopAppBar.
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysMainContentArea() {
        // Assert that the primary content container of the home screen is present.
        // This ensures the main body of the scaffold is rendered.
        composeTestRule.onNodeWithTag("HomeScreenContent").assertIsDisplayed()
    }

    @Test
    fun clickingAssociationsTab_navigatesToAssociationBrowser() {
        // Find the navigation item by its assumed content description.
        val associationsButton = composeTestRule.onNodeWithContentDescription("Navigate to Associations")

        // Act: Click the button. This executes the onClick lambda in the HomeScreen.
        associationsButton.performClick()

        // Assert: Verify that the correct navigation function was called on our mock object.
        verify(mockNavigationActions).navigateTo(Screen.AssociationBrowser)
    }

    @Test
    fun clickingMyEventsTab_navigatesToMyEvents() {
        // Find the navigation item.
        val myEventsButton = composeTestRule.onNodeWithContentDescription("Navigate to My Events")

        // Act: Click the button.
        myEventsButton.performClick()

        // Assert: Verify the correct navigation call was made.
        verify(mockNavigationActions).navigateTo(Screen.MyEvents)
    }

    @Test
    fun clickingSettingsTab_navigatesToSettings() {
        // Find the navigation item.
        val settingsButton = composeTestRule.onNodeWithContentDescription("Navigate to Settings")

        // Act: Click the button.
        settingsButton.performClick()

        // Assert: Verify the correct navigation call was made.
        verify(mockNavigationActions).navigateTo(Screen.Settings)
    }
}