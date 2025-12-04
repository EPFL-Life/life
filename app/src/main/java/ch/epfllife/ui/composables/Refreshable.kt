package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ch.epfllife.R
import ch.epfllife.utils.SystemToastHelper
import ch.epfllife.utils.ToastHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Refreshable(
    modifier: Modifier = Modifier,
    onRefresh: (signalFinished: () -> Unit, signalFailed: () -> Unit) -> Unit,
    toastHelper: ToastHelper = SystemToastHelper(),
    content: @Composable BoxScope.() -> Unit,
) {
  var isRefreshing by remember { mutableStateOf(false) }
  var didFail by remember { mutableStateOf(false) }
  val context = LocalContext.current

  LaunchedEffect(didFail) {
    if (didFail) {
      didFail = false
      toastHelper.show(context, R.string.refresh_failed)
    }
  }

  PullToRefreshBox(
      isRefreshing,
      onRefresh = {
        isRefreshing = true
        onRefresh(
            { isRefreshing = false },
            {
              isRefreshing = false
              didFail = true
            },
        )
      },
      modifier = modifier,
      content = content,
  )
}
