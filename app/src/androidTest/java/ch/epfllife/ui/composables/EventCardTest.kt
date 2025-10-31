package ch.epfllife.ui.composables

import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.map.Location
import ch.epfllife.utils.assertClickable
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test

class EventCardTest {

  @get:Rule val composeTestRule = createComposeRule()
  private val event =
      Event(
          id = "0",
          title = "Test Event",
          description = "This is a test event",
          location = Location(46.520278, 6.565556, "EPFL"),
          time = Timestamp.Companion.now().toString(),
          association =
              Association(
                  name = "TestAssociation",
                  id = "gejn82",
                  description = "This is a test",
                  eventCategory = EventCategory.ACADEMIC),
          tags = emptySet(),
          price = 0u,
      )

  @Test
  fun isClickable() {
    composeTestRule.assertClickable(
        { clickHandler -> EventCard(event, onClick = clickHandler) },
        EventCardTestTags.EVENT_CARD,
    )
  }
}
