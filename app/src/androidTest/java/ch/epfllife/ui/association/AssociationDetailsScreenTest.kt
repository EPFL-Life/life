package ch.epfllife.ui.association

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.example_data.ExampleAssociation
import ch.epfllife.ui.theme.Theme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AssociationDetailsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ============ AssociationDetailsContent Tests ============

  @Test
  fun content_DisplaysAssociationDescription() {
    val association = ExampleAssociation.association4
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithText(association.description).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysAssociationImage() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
  }

  @Test
  fun content_DisplaysBackButton() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysSubscribeButton() {
    val association = ExampleAssociation.association2
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("Subscribe to ${association.name}").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysAboutSection() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ABOUT_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithText("About").assertIsDisplayed()
    composeTestRule.onNodeWithText(association.about!!).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysSocialLinksRow() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertIsDisplayed()
    composeTestRule.onNodeWithText("Social Pages").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysUpcomingEventsColumn() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Upcoming Events").assertIsDisplayed()
  }

  // ============ Interaction Tests ============

  @Test
  fun content_BackButtonTriggersCallback() {
    val association = ExampleAssociation.association1
    var backClicked = false
    composeTestRule.setContent {
      Theme {
        AssociationDetailsContent(association = association, onGoBack = { backClicked = true })
      }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).performClick()
    assertTrue("Back button should trigger onGoBack callback", backClicked)
  }

  @Test
  fun content_SubscribeButtonIsClickable() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun content_SubscribeButtonTogglesState() {
    val association = ExampleAssociation.association2
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }

    // Initially should show "Subscribe to [name]"
    composeTestRule.onNodeWithText("Subscribe to ${association.name}").assertIsDisplayed()

    // Click subscribe button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Should now show "Unsubscribe from [name]"
    composeTestRule.onNodeWithText("Unsubscribe from ${association.name}").assertIsDisplayed()
  }

  @Test
  fun content_SubscribeButtonTogglesBackAndForth() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }

    // Initially subscribed = false
    composeTestRule.onNodeWithText("Subscribe to ${association.name}").assertIsDisplayed()

    // Click to subscribe
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Unsubscribe from ${association.name}").assertIsDisplayed()

    // Click to unsubscribe
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Subscribe to ${association.name}").assertIsDisplayed()
  }

  // ============ Different Association Data Tests ============

  @Test
  fun content_DisplaysAssociationWithoutAbout() {
    val association = ExampleAssociation.association3
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("About").assertIsDisplayed()
    composeTestRule.onNodeWithText("No description available.").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysAssociationWithEmptyAbout() {
    val association = ExampleAssociation.association2
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("About").assertIsDisplayed()
    // Empty string should still be displayed
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ABOUT_SECTION).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysLongAboutText() {
    val longAbout =
        "This is a very long about text that contains a lot of information about the association. " +
            "It should wrap properly and display all the content to the user. " +
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
            "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
    val association = ExampleAssociation.association1.copy(about = longAbout)
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText(longAbout, substring = true).assertIsDisplayed()
  }

  @Test
  fun content_HandlesAssociationWithoutImageUrl() {
    val association = ExampleAssociation.association3
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    // Image should still exist even with empty URL
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
  }

  @Test
  fun content_DisplaysAssociationWithMultipleSocialLinks() {
    val association = ExampleAssociation.association4
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    // Should display without crashing
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertIsDisplayed()
  }

  // ============ Integration Tests ============

  @Test
  fun integration_AllContentDisplayedTogether() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }

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
  fun integration_MultipleClicksOnSubscribeButton() {
    val association = ExampleAssociation.association2
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }

    // Click subscribe button multiple times
    repeat(5) {
      composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
      composeTestRule.waitForIdle()
    }

    // Should not crash and button should still be displayed
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun integration_MultipleClicksOnBackButton() {
    val association = ExampleAssociation.association1
    var backClickCount = 0
    composeTestRule.setContent {
      Theme {
        AssociationDetailsContent(association = association, onGoBack = { backClickCount++ })
      }
    }

    // Click back button multiple times
    repeat(3) {
      composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).performClick()
      composeTestRule.waitForIdle()
    }

    assertEquals("Back button should be clicked 3 times", 3, backClickCount)
  }

  @Test
  fun integration_NavigationBetweenStates() {
    val association = ExampleAssociation.association1
    var goBackCalled = false
    composeTestRule.setContent {
      Theme {
        AssociationDetailsContent(association = association, onGoBack = { goBackCalled = true })
      }
    }

    // Verify content is displayed
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()

    // Click back button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).performClick()

    assertTrue("Navigation callback should be triggered", goBackCalled)
  }

  @Test
  fun content_AllButtonsAreAccessible() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }

    // All interactive elements should be present and accessible
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertHasClickAction()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun content_AboutSectionLabelDisplayed() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("About").assertIsDisplayed()
  }

  @Test
  fun content_SocialPagesSectionLabelDisplayed() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("Social Pages").assertIsDisplayed()
  }

  @Test
  fun content_UpcomingEventsSectionLabelDisplayed() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("Upcoming Events").assertIsDisplayed()
  }

  // ============ AssociationDetailsScreen Tests ============

  @Test
  fun screen_DisplaysWithAssociationId() {
    val testId = ExampleAssociation.association1.id
    composeTestRule.setContent {
      Theme { AssociationDetailsScreen(associationId = testId, onGoBack = {}) }
    }
    // Should display without crashing
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()
  }

  @Test
  fun screen_CallsOnGoBackWhenBackButtonClicked() {
    var backCalled = false
    composeTestRule.setContent {
      Theme {
        AssociationDetailsScreen(associationId = "test_id", onGoBack = { backCalled = true })
      }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).performClick()
    assertTrue("onGoBack should be called when back button is clicked", backCalled)
  }

  // ============ Scrolling Tests ============

  @Test
  fun content_AllSectionsAreAccessibleAfterScrolling() {
    val association = ExampleAssociation.association1
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }

    // Check that all major sections exist and can be accessed
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ABOUT_SECTION).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN).assertExists()
  }

  // ============ Edge Case Tests ============

  @Test
  fun content_HandlesNullPictureUrl() {
    val association = ExampleAssociation.association3
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = association, onGoBack = {}) }
    }
    // Should still display image component
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
  }
}
