package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Refreshable(
    onRefresh: (signalFinished: () -> Unit) -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
  var isRefreshing by remember { mutableStateOf(false) }

  PullToRefreshBox(
      isRefreshing,
      onRefresh = {
        isRefreshing = true
        onRefresh { isRefreshing = false }
      },
      content = content,
  )
}
