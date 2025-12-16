package ch.epfllife.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.epfllife.R

@Composable
fun ShareButton(modifier: Modifier = Modifier, onShare: () -> Unit) {
  IconButton(
      onClick = onShare,
      modifier =
          modifier
              .padding(16.dp)
              .size(40.dp)
              .background(Color.Black.copy(alpha = 0.4f), CircleShape)) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(R.string.share),
            tint = Color.White)
      }
}
