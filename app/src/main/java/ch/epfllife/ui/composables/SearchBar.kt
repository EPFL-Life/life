package ch.epfllife.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.epfllife.ui.theme.Gray

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    searchColorBar: Color = Gray,
    onFilterClick: () -> Unit = {}
) {
  Surface(
      color = searchColorBar,
      shape = RoundedCornerShape(12.dp),
      modifier = modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Icon(imageVector = Icons.Outlined.Search, contentDescription = "Search")

              BasicTextField(
                  value = query,
                  onValueChange = onQueryChange,
                  modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                  singleLine = true,
                  decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                      Text(
                          text = "Search...",
                          style = MaterialTheme.typography.bodyMedium,
                          color = Color.Gray)
                    }
                    innerTextField()
                  })

              IconButton(onClick = onFilterClick) {
                Icon(imageVector = Icons.Outlined.FilterAlt, contentDescription = "Filter")
              }
            }
      }
}

@Preview()
@Composable
fun SearchBarPreview() {
  SearchBar()
}
