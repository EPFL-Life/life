package ch.epfllife.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImageUploadField(
    label: String,
    imageUrl: String,
    isUploading: Boolean,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp))

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier =
            Modifier.fillMaxWidth().height(150.dp).clickable(enabled = !isUploading) {
              onUploadClick()
            }) {
          Box(contentAlignment = Alignment.Center) {
            if (imageUrl.isNotBlank()) {
              AsyncImage(
                  model = imageUrl,
                  contentDescription = null, // decorative
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize())
            }

            if (isUploading) {
              Box(
                  modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                  contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                  }
            } else if (imageUrl.isBlank()) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = "Upload",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to select",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
  }
}
