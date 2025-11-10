package ch.epfllife.ui.association

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.EventCategory
import ch.epfllife.ui.theme.Theme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AssociationDetailsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val sampleAssociation =
      Association(
          id = "esn_lausanne",
          name = "ESN Lausanne",
          description = "Erasmus Student Network at EPFL.",
          eventCategory = EventCategory.CULTURE,
          pictureUrl =
              "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png",
          about =
              "The Erasmus Student Network (ESN) Lausanne is a student association that helps exchange students integrate into life at EPFL and Lausanne through social and cultural activities.",
          socialLinks =
              mapOf(
                  "instagram" to "https://www.instagram.com/esnlausanne",
                  "telegram" to "https://t.me/esnlausanne",
                  "whatsapp" to "https://wa.me/41791234567",
                  "linkedin" to "https://www.linkedin.com/company/esnlausanne",
                  "website" to "https://esnlausanne.ch"))

  // ============ AssociationDetailsContent Tests ============


  @Test
  fun content_DisplaysAssociationDescription() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithText("Erasmus Student Network at EPFL.").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysAssociationImage() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
  }

  @Test
  fun content_DisplaysBackButton() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
  }

  @Test
  fun content_DisplaysSubscribeButton() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithText("Subscribe to ESN Lausanne").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysAboutSection() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ABOUT_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithText("About").assertIsDisplayed()
    composeTestRule
        .onNodeWithText(
            "The Erasmus Student Network (ESN) Lausanne is a student association that helps exchange students integrate into life at EPFL and Lausanne through social and cultural activities.")
        .assertIsDisplayed()
  }

  @Test
  fun content_DisplaysSocialLinksRow() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertIsDisplayed()
    composeTestRule.onNodeWithText("Social Pages").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysUpcomingEventsColumn() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Upcoming Events").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysUpcomingEvents() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    // Check that dummy events are displayed
    composeTestRule.onNodeWithText("Welcome Party").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hiking Trip").assertIsDisplayed()
  }

  // ============ Interaction Tests ============

  @Test
  fun content_BackButtonTriggersCallback() {
    var backClicked = false
    composeTestRule.setContent {
      Theme {
        AssociationDetailsContent(
            association = sampleAssociation, onGoBack = { backClicked = true })
      }
    }
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).performClick()
    assertTrue("Back button should trigger onGoBack callback", backClicked)
  }

  @Test
  fun content_SubscribeButtonIsClickable() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun content_SubscribeButtonTogglesState() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }

    // Initially should show "Subscribe to ESN Lausanne"
    composeTestRule.onNodeWithText("Subscribe to ESN Lausanne").assertIsDisplayed()

    // Click subscribe button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Should now show "Unsubscribe from ESN Lausanne"
    composeTestRule.onNodeWithText("Unsubscribe from ESN Lausanne").assertIsDisplayed()
  }

  @Test
  fun content_SubscribeButtonTogglesBackAndForth() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }

    // Initially subscribed = false
    composeTestRule.onNodeWithText("Subscribe to ESN Lausanne").assertIsDisplayed()

    // Click to subscribe
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Unsubscribe from ESN Lausanne").assertIsDisplayed()

    // Click to unsubscribe
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Subscribe to ESN Lausanne").assertIsDisplayed()
  }

  // ============ Different Association Data Tests ============

  @Test
  fun content_DisplaysAssociationWithoutAbout() {
    val associationWithoutAbout = sampleAssociation.copy(about = null)
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = associationWithoutAbout, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("About").assertIsDisplayed()
    composeTestRule.onNodeWithText("No description available.").assertIsDisplayed()
  }

  @Test
  fun content_DisplaysAssociationWithEmptyAbout() {
    val associationWithEmptyAbout = sampleAssociation.copy(about = "")
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = associationWithEmptyAbout, onGoBack = {}) }
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
    val longAboutAssociation = sampleAssociation.copy(about = longAbout)
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = longAboutAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText(longAbout, substring = true).assertIsDisplayed()
  }

  @Test
  fun content_HandlesAssociationWithoutImageUrl() {
    val associationWithoutImage = sampleAssociation.copy(pictureUrl = "")
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = associationWithoutImage, onGoBack = {}) }
    }
    // Image should still exist even with empty URL
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
  }

  @Test
  fun content_DisplaysAssociationWithMultipleSocialLinks() {
    val associationWithManySocialLinks =
        sampleAssociation.copy(
            socialLinks =
                mapOf(
                    "instagram" to "https://www.instagram.com/test",
                    "telegram" to "https://t.me/test",
                    "whatsapp" to "https://wa.me/41791234567",
                    "linkedin" to "https://www.linkedin.com/company/test",
                    "website" to "https://test.ch",
                    "facebook" to "https://www.facebook.com/test"))
    composeTestRule.setContent {
      Theme {
        AssociationDetailsContent(association = associationWithManySocialLinks, onGoBack = {})
      }
    }
    // Should display without crashing
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertIsDisplayed()
  }

  // ============ Integration Tests ============

  @Test
  fun integration_AllContentDisplayedTogether() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
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
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
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
    var backClickCount = 0
    composeTestRule.setContent {
      Theme {
        AssociationDetailsContent(association = sampleAssociation, onGoBack = { backClickCount++ })
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
    var goBackCalled = false
    composeTestRule.setContent {
      Theme {
        AssociationDetailsContent(
            association = sampleAssociation, onGoBack = { goBackCalled = true })
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
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }

    // All interactive elements should be present and accessible
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertHasClickAction()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun content_AboutSectionLabelDisplayed() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("About").assertIsDisplayed()
  }

  @Test
  fun content_SocialPagesSectionLabelDisplayed() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("Social Pages").assertIsDisplayed()
  }

  @Test
  fun content_UpcomingEventsSectionLabelDisplayed() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }
    composeTestRule.onNodeWithText("Upcoming Events").assertIsDisplayed()
  }

  // ============ AssociationDetailsScreen Tests ============

  @Test
  fun screen_DisplaysWithAssociationId() {
    composeTestRule.setContent {
      Theme { AssociationDetailsScreen(associationId = "test_id", onGoBack = {}) }
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
  fun content_CanScrollThroughAllContent() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
    }

    // Verify top content is visible
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()

    // Scroll to bottom
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN)
        .assertIsDisplayed()

    // Verify bottom content is visible after scrolling
    composeTestRule.onNodeWithText("Hiking Trip").assertIsDisplayed()
  }

  @Test
  fun content_AllSectionsAreAccessibleAfterScrolling() {
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = sampleAssociation, onGoBack = {}) }
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
    val associationWithNullPicture = sampleAssociation.copy(pictureUrl = null)
    composeTestRule.setContent {
      Theme { AssociationDetailsContent(association = associationWithNullPicture, onGoBack = {}) }
    }
    // Should still display image component
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
  }

}
