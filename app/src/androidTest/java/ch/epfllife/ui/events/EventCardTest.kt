package ch.epfllife.ui.events

import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.EventCard
import ch.epfllife.ui.composables.EventCardTestTags
import ch.epfllife.ui.composables.Price
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
          time = Timestamp.now().toString(),
          associationId = "assoc1",
          tags = emptySet(),
          price = Price(0u),
      )

  @Test
  fun isClickable() {
    composeTestRule.assertClickable(
        { clickHandler -> EventCard(event, onClick = clickHandler) },
        EventCardTestTags.EVENT_CARD,
    )
  }
}
