package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.ui.theme.LifeRed

@Composable
fun SubmitButton(modifier: Modifier = Modifier, enabled: Boolean, onClick: () -> Unit) {
  Button(
      modifier = modifier,
      onClick = onClick,
      shape = RoundedCornerShape(6.dp),
      colors = ButtonDefaults.buttonColors(containerColor = LifeRed, contentColor = Color.White),
      enabled = enabled) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          Text(
              text = stringResource(R.string.submit),
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
        }
      }
}

