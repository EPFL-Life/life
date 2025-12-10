package ch.epfllife.ui.composables

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import ch.epfllife.model.enums.SubscriptionFilter

fun Modifier.subscriptionSwipe(
    selected: SubscriptionFilter,
    onSelect: (SubscriptionFilter) -> Unit
): Modifier =
    pointerInput(selected) {
      detectHorizontalDragGestures { _, dragAmount ->
        if (dragAmount > 40 && selected == SubscriptionFilter.All) {
          onSelect(SubscriptionFilter.Subscribed)
        } else if (dragAmount < -40 && selected == SubscriptionFilter.Subscribed) {
          onSelect(SubscriptionFilter.All)
        }
      }
    }
