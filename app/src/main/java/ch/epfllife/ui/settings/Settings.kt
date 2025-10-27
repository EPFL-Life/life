package ch.epfllife.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.epfllife.ui.navigation.NavigationTestTags

@Composable
fun Settings(modifier: Modifier = Modifier) {
  Box(
      modifier = modifier.fillMaxSize().testTag(NavigationTestTags.SETTINGS_SCREEN),
      contentAlignment = Alignment.Center,
  ) {
    Text(text = "Settings")
  }
}
