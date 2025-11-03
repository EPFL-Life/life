package ch.epfllife.ui.myevents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun MyEvents(modifier: Modifier = Modifier) {
  Box(
      modifier = modifier.fillMaxSize().testTag(NavigationTestTags.MYEVENTS_SCREEN),
      contentAlignment = Alignment.Center) {
        Text(text = "MyEvents")
      }
}
