package ch.epfllife.ui.composables

import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.ui.theme.Theme
import ch.epfllife.utils.assertClickable
import ch.epfllife.utils.assertTagIsDisplayed
import ch.epfllife.utils.assertTagTextEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class CalendarCardTest {

    @get:Rule val composeTestRule = createComposeRule()
    private val event = ExampleEvents.event1

    @Test
    fun calendarCard_isClickable() {
        composeTestRule.assertClickable(
            { clickHandler ->
                Theme {
                    CalendarCard(event = event, onClick = clickHandler)
                }
            },
            CalendarTestTags.EVENT_CARD
        )
    }

    @Test
    fun calendarCard_displaysContentCorrectly() {
        composeTestRule.setContent {
            Theme {
                CalendarCard(event = event, onClick = {})
            }
        }

        // Title and association should match the event data
        composeTestRule.assertTagTextEquals(CalendarTestTags.EVENT_TITLE, event.title)
        composeTestRule.assertTagTextEquals(
            CalendarTestTags.EVENT_ASSOCIATION,
            event.association.name
        )

        // Date box and arrow icon should always be visible
        composeTestRule.assertTagIsDisplayed(CalendarTestTags.EVENT_DATE_BOX)
        composeTestRule.assertTagIsDisplayed(CalendarTestTags.EVENT_ARROW)
    }
}
