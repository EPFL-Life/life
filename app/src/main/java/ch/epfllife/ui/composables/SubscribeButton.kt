package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SubscribeButton(
    modifier: Modifier,
    onClick: () -> Unit,
    isSubscribed: Boolean,
    content: @Composable (RowScope.() -> Unit),
) {
  Button(
      onClick = onClick,
      modifier = modifier.fillMaxWidth().padding(top = 8.dp),
      shape = RoundedCornerShape(6.dp),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = if (isSubscribed) Color.Gray else Color(0xFFDC2626),
              contentColor = Color.White,
          ),
      content = content,
  )
}
