package ch.epfllife.ui.composables

import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.EventCategory
import ch.epfllife.utils.assertClickable
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.assertTagTextEquals
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
          pictureUrl = null,
      )

  @Test
  fun isClickable() {
    composeTestRule.assertClickable(
        { clickHandler -> AssociationCard(association, onClick = clickHandler) },
        AssociationCardTestTags.ASSOCIATION_CARD,
    )
  }

  @Test
  fun contentIsDisplayed() {
    composeTestRule.setContent { AssociationCard(association, onClick = {}) }

    composeTestRule.assertTagTextEquals(AssociationCardTestTags.ASSOCIATION_NAME, association.name)
    composeTestRule.assertTagTextEquals(
        AssociationCardTestTags.ASSOCIATION_DESCRIPTION,
        association.description,
    )
    composeTestRule.assertTagIsDisplayed(AssociationCardTestTags.ASSOCIATION_LOGO)
  }
}
