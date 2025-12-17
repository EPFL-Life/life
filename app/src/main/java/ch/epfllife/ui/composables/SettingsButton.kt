package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
  Button(
      onClick = onClick,
      modifier = modifier.fillMaxWidth().height(56.dp),
      shape = RoundedCornerShape(12.dp),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
              contentColor = MaterialTheme.colorScheme.onSurface),
      elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
      contentPadding = PaddingValues(0.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically) {
              if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(16.dp))
              }

              Text(
                  text = text,
                  style = MaterialTheme.typography.bodyLarge,
                  modifier = Modifier.weight(1f))

              Icon(
                  imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
      }
}
