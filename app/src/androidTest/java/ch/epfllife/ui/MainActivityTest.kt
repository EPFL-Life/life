package ch.epfllife.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import ch.epfllife.ThemedApp
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.Association
import ch.epfllife.model.authentication.Auth
import ch.epfllife.model.authentication.SignInResult
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.UserRepositoryLocal
import ch.epfllife.ui.admin.AddEditAssociationTestTags
import ch.epfllife.ui.admin.AssociationAdminScreenTestTags
import ch.epfllife.ui.admin.SelectAssociationTestTags
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
import ch.epfllife.ui.settings.SettingsScreenTestTags
import ch.epfllife.utils.FakeCredentialManager
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.navigateToTab
import ch.epfllife.utils.setUpEmulator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val auth = Auth(FakeCredentialManager.withDefaultTestUser)
    private lateinit var db: Db

    @Before
    fun setUp() {
        db = Db.freshLocal()
        setUpEmulator(auth, "MainActivityTest")
        runTest {
            val signInResult =
                auth.signInWithCredential(FakeCredentialManager.defaultUserCredentials)
            Assert.assertTrue("Sign in must succeed", signInResult is SignInResult.Success)
        }
    }

    @Test
    fun themedApp_startsWithHomeScreen() {
        composeTestRule.setContent { ThemedApp(auth, db) }

        composeTestRule.waitForIdle()

        // Verify home screen is displayed
        composeTestRule
            .onNodeWithTag(NavigationTestTags.HOMESCREEN_SCREEN, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun themedApp_showsBottomNavigationOnMainScreens() {
        composeTestRule.setContent { ThemedApp(auth, db) }

        composeTestRule.waitForIdle()

        // Bottom bar should be visible on home screen
        composeTestRule
            .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
            .assertIsDisplayed()

        // Bottom bar should be visible on all main tabs
        composeTestRule.navigateToTab(Tab.AssociationBrowser)
        composeTestRule
            .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.navigateToTab(Tab.Calendar)
        composeTestRule
            .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule.navigateToTab(Tab.Settings)
        composeTestRule
            .onNodeWithTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun themedApp_hasAllBottomNavigationTabs() {
        composeTestRule.setContent { ThemedApp(auth, db) }

        composeTestRule.waitForIdle()

        // Verify all tabs are present
        composeTestRule
            .onNodeWithTag(NavigationTestTags.HOMESCREEN_TAB, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(NavigationTestTags.ASSOCIATIONBROWSER_TAB, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(NavigationTestTags.CALENDAR_TAB, useUnmergedTree = true)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(NavigationTestTags.SETTINGS_TAB, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun creatingAssociationFromSettings_selectsItOnReturn() {
        seedAdminUser()
        val association = ExampleAssociations.sampleAssociation.copy(name = "New Admin Assoc")

        composeTestRule.setContent { ThemedApp(auth, db) }
        composeTestRule.waitForIdle()
        composeTestRule.navigateToTab(Tab.Settings)

        composeTestRule
            .onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON, useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(
                AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON,
                useUnmergedTree = true
            )
            .performClick()

        composeTestRule
            .onNodeWithTag(SelectAssociationTestTags.ADD_NEW_BUTTON, useUnmergedTree = true)
            .performClick()

        composeTestRule.enterAssociationForm(association)

        composeTestRule
            .onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON, useUnmergedTree = true)
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntilNodeExists(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON)
        composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON)
        composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.MANAGE_EVENTS_BUTTON)
    }

    @Test
    fun addAssociationFromSelectAssociation_returnsToSettingsScreen() {
        seedAdminUser()
        val association = ExampleAssociations.sampleAssociation.copy(name = "Select Flow Assoc")

        composeTestRule.setContent { ThemedApp(auth, db) }
        composeTestRule.waitForIdle()

        composeTestRule.navigateToTab(Tab.Settings)

        composeTestRule
            .onNodeWithTag(SettingsScreenTestTags.ADMIN_CONSOLE_BUTTON, useUnmergedTree = true)
            .performClick()

        composeTestRule
            .onNodeWithTag(
                AssociationAdminScreenTestTags.SELECT_ASSOCIATION_BUTTON,
                useUnmergedTree = true
            )
            .performClick()

        composeTestRule
            .onNodeWithTag(SelectAssociationTestTags.ADD_NEW_BUTTON, useUnmergedTree = true)
            .performClick()

        composeTestRule.enterAssociationForm(association)
        composeTestRule
            .onNodeWithTag(AddEditAssociationTestTags.SUBMIT_BUTTON, useUnmergedTree = true)
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntilNodeExists(NavigationTestTags.SETTINGS_SCREEN)
        composeTestRule.assertTagIsDisplayed(AssociationAdminScreenTestTags.MANAGE_ASSOCIATION_BUTTON)
    }

    private fun seedAdminUser() {
        val userRepo = db.userRepo as UserRepositoryLocal
        runBlocking {
            userRepo.createUser(ExampleUsers.adminUser)
            userRepo.simulateLogin(ExampleUsers.adminUser.id)
        }
    }

    private fun ComposeContentTestRule.enterAssociationForm(association: Association) {
        fillField(AddEditAssociationTestTags.NAME_FIELD, association.name)
        fillField(AddEditAssociationTestTags.DESCRIPTION_FIELD, association.description)
        fillField(
            AddEditAssociationTestTags.ABOUT_FIELD, association.about ?: "About ${association.name}"
        )
    }

    private fun ComposeContentTestRule.fillField(tag: String, value: String) {
        onNodeWithTag(tag, useUnmergedTree = true).performTextClearance()
        onNodeWithTag(tag, useUnmergedTree = true).performTextInput(value)
    }

    private fun ComposeContentTestRule.waitUntilNodeExists(
        tag: String,
        timeoutMillis: Long = 5000,
    ) {
        waitUntil(timeoutMillis) {
            try {
                onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }
}
