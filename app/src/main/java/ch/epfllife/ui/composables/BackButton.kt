package ch.epfllife.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.epfllife.R

@Composable
fun BackButton(modifier: Modifier = Modifier, onGoBack: () -> Unit) {
  IconButton(
      onClick = onGoBack,
      modifier =
          modifier
              .padding(16.dp)
              .size(40.dp)
              .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape),
  ) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = stringResource(R.string.back_button_description),
        tint = Color.White,
    )
  }
}
