package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import ch.epfllife.model.enums.SubscriptionFilter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SwipeModifierTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val swipeTag = "subscriptionSwipeBox"

  @Test
  fun swipeRightFromAll_selectsSubscribed() {
    var selected = SubscriptionFilter.All

    composeTestRule.setContent {
      Box(Modifier.fillMaxSize().testTag(swipeTag).subscriptionSwipe(selected) { selected = it })
    }

    composeTestRule.onNodeWithTag(swipeTag).performTouchInput { swipeRight() }

    composeTestRule.runOnIdle { assertEquals(SubscriptionFilter.Subscribed, selected) }
  }

  @Test
  fun swipeLeftFromSubscribed_selectsAll() {
    var selected = SubscriptionFilter.Subscribed

    composeTestRule.setContent {
      Box(Modifier.fillMaxSize().testTag(swipeTag).subscriptionSwipe(selected) { selected = it })
    }

    composeTestRule.onNodeWithTag(swipeTag).performTouchInput { swipeLeft() }

    composeTestRule.runOnIdle { assertEquals(SubscriptionFilter.All, selected) }
  }

  @Test
  fun smallDrags_doNotTriggerSelectionChange() {
    var selected = SubscriptionFilter.All
    var callbackCalls = 0

    composeTestRule.setContent {
      Box(
          Modifier.fillMaxSize().testTag(swipeTag).subscriptionSwipe(selected) {
            selected = it
            callbackCalls++
          })
    }

    // Drag less than the 40px threshold in both directions
    composeTestRule.onNodeWithTag(swipeTag).performTouchInput {
      down(center)
      moveBy(Offset(20f, 0f))
      up()
    }
    composeTestRule.onNodeWithTag(swipeTag).performTouchInput {
      down(center)
      moveBy(Offset(-20f, 0f))
      up()
    }

    composeTestRule.runOnIdle {
      assertEquals(SubscriptionFilter.All, selected)
      assertEquals(0, callbackCalls)
    }
  }
}
