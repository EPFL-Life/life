package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Refreshable(onRefresh: () -> Unit, content: @Composable BoxScope.() -> Unit) {
  var isRefreshing by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()

  PullToRefreshBox(
      isRefreshing,
      onRefresh = {
        isRefreshing = true
        coroutineScope.launch {
          awaitAll(
              async { onRefresh() },
              async {
                // The animation seems broken anyway,
                // but this makes it at least visible for a short time
                delay(300)
              },
          )
          isRefreshing = false
        }
      },
      content = content,
  )
}
