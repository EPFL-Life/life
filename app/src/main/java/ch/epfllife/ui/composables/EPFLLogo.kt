package ch.epfllife.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfllife.R

object EPFLLogoTestTags {
  const val LOGO = "EPFL_LOGO"
}

@Composable
fun EPFLLogo(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
    Image(
        painter = painterResource(id = R.drawable.epfl_life_logo),
        contentDescription = "EPFL Life Logo",
        modifier = Modifier.height(40.dp).testTag(EPFLLogoTestTags.LOGO),
        contentScale = ContentScale.Fit,
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun EPFLLogoPreview() {
  EPFLLogo()
}
