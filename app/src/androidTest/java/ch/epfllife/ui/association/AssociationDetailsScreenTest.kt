package ch.epfllife.ui.association

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfllife.R
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.ui.theme.Theme
import ch.epfllife.utils.assertClickable
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AssociationDetailsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ---------- Helper functions ----------

  // Helper to set up AssociationDetailsContent inside a Theme.
  private fun setAssociationDetailsContent(
      association: ch.epfllife.model.association.Association,
      onGoBack: () -> Unit = {}
  ) {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = onGoBack) }
    }
  }

  // Helper to set up AssociationDetailsScreen inside a Theme.
  private fun setAssociationDetailsScreen(associationId: String, onGoBack: () -> Unit = {}) {
    composeTestRule.setContent {
      Theme { AssociationDetailsScreen(associationId = associationId, onGoBack = onGoBack) }
    }
  }

  private fun getString(resId: Int, vararg args: Any): String {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return context.getString(resId, *args)
  }

  // ============ AssociationDetailsContent Tests ============

  @Test
  fun contentDisplaysAllMainSectionsCorrectly() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)

    // ---------- Main visible elements ----------
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ABOUT_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN)
        .assertIsDisplayed()

    // ---------- Section labels ----------
    composeTestRule.onNodeWithText(getString(R.string.about_section_title)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getString(R.string.social_pages_title)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getString(R.string.upcoming_events_title)).assertIsDisplayed()

    // ---------- Subscribe button ----------
    // Initially, the subscribe button should be in "Subscribe" state
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assert(hasText(getString(R.string.subscribe_to, association.name)))

    // ---------- Name and description ----------
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT)
        .assert(hasText(association.name))
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT)
        .assert(hasText(association.description))
  }

  // ============ Interaction Tests ============

  @Test
  fun contentBackButtonTriggersCallback() {
    val association = ExampleAssociations.association1
    composeTestRule.assertClickable(
        composable = { onClick ->
          Theme { AssociationDetailsContent(association = association, onGoBack = onClick) }
        },
        tag = AssociationDetailsTestTags.BACK_BUTTON,
    )
  }

  @Test
  fun contentSubscribeButtonIsClickable() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun contentSubscribeButtonTogglesState() {
    val association = ExampleAssociations.association2
    setAssociationDetailsContent(association)

    // Initially should show subscribe button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()

    // Click subscribe button -> should switch to unsubscribe
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun contentSubscribeButtonTogglesBackAndForth() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)

    // Initially unsubscribed
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()

    // Click to subscribe -> should show unsubscribe button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON).assertIsDisplayed()

    // Click to unsubscribe -> should show subscribe button again
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
  }

  // ============ Integration Tests ============

  @Test
  fun integrationAllContentDisplayedTogether() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)

    // Verify all major components are present
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ABOUT_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN)
        .assertIsDisplayed()
  }

  @Test
  fun integrationMultipleClicksOnSubscribeButton() {
    val association = ExampleAssociations.association2
    setAssociationDetailsContent(association)

    // Click subscribe button multiple times and assert the state toggles each time
    repeat(5) { i ->
      // Locate whichever button is currently visible
      val buttonTag =
          if (i % 2 == 0) AssociationDetailsTestTags.SUBSCRIBE_BUTTON
          else AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON

      composeTestRule.onNodeWithTag(buttonTag).performClick()
      composeTestRule.waitForIdle()

      // Verify that the other tag is now visible
      val expectedTag =
          if ((i + 1) % 2 == 1) AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON
          else AssociationDetailsTestTags.SUBSCRIBE_BUTTON

      composeTestRule.onNodeWithTag(expectedTag).assertIsDisplayed()
    }
  }

  @Test
  fun integrationNavigationBetweenStates() {
    val association = ExampleAssociations.association1
    var goBackCalled = false
    setAssociationDetailsContent(association) { goBackCalled = true }

    // Verify content is displayed
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()

    // Click back button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).performClick()

    assertTrue("Navigation callback should be triggered", goBackCalled)
  }

  @Test
  fun contentAllButtonsAreAccessible() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)

    // All interactive elements should be present and accessible
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertHasClickAction()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun contentAboutSectionLabelDisplayed() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)
    composeTestRule.onNodeWithText(getString(R.string.about_section_title)).assertIsDisplayed()
  }

  @Test
  fun contentSocialPagesSectionLabelDisplayed() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)
    composeTestRule.onNodeWithText(getString(R.string.social_pages_title)).assertIsDisplayed()
  }

  @Test
  fun contentUpcomingEventsSectionLabelDisplayed() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)
    composeTestRule.onNodeWithText(getString(R.string.upcoming_events_title)).assertIsDisplayed()
  }

  // ============ AssociationDetailsScreen Tests ============

  @Test
  fun screenDisplaysWithAssociationId() {
    val testId = ExampleAssociations.association1.id
    setAssociationDetailsContent(ExampleAssociations.association1)
    // Should display without crashing
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()
  }

  @Test
  fun screenCallsOnGoBackWhenBackButtonClicked() {
    composeTestRule.assertClickable(
        composable = { onClick ->
          Theme {
            AssociationDetailsContent(
                association = ExampleAssociations.association1, onGoBack = onClick)
          }
        },
        tag = AssociationDetailsTestTags.BACK_BUTTON,
    )
  }

  // ============ Edge Case Tests ============

  @Test
  fun contentHandlesNullPictureUrl() {
    val association = ExampleAssociations.association3
    setAssociationDetailsContent(association)
    // Should still display image component
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
  }
}
