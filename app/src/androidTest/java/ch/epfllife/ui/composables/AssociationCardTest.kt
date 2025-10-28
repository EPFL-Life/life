package ch.epfllife.ui.composables

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.EventCategory
import ch.epfllife.utils.assertClickable
import org.junit.Rule
import org.junit.Test

class AssociationCardTest {

  @get:Rule val composeTestRule = createComposeRule()
  private val association =
      Association(
          id = "0",
          name = "Assoc",
          description = "This is a test event",
          eventCategory = EventCategory.TECH,
          pictureUrl = null)

  @Test
  fun isClickable() {
    composeTestRule.assertClickable(
        { clickHandler -> AssociationCard(association, onClick = clickHandler) },
        AssociationCardTestTags.ASSOCIATION_CARD,
    )
  }

  @Test
  fun contentIsDisplayed() {
    composeTestRule.setContent {
      AssociationCard(association, onClick = {})
    }
    composeTestRule.waitUntil {
      composeTestRule.onNodeWithTag(AssociationCardTestTags.ASSOCIATION_CARD).isDisplayed()
    }

    composeTestRule
      .onNodeWithTag(AssociationCardTestTags.ASSOCIATION_NAME)
      .assertExists()
      .assertTextEquals(association.name)

    composeTestRule
      .onNodeWithTag(AssociationCardTestTags.ASSOCIATION_DESCRIPTION)
      .assertExists()
      .assertTextEquals(association.description)
  }
}
