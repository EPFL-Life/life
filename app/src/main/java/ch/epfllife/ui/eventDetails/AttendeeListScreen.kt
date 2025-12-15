package ch.epfllife.ui.eventDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.model.user.User
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.SearchBar
import kotlin.collections.filter

@Composable
fun AttendeeListScreen(attendees: List<User>, onBack: () -> Unit) {
  var query by remember { mutableStateOf("") }

  val filteredAttendees =
      remember(query, attendees) {
        attendees.filter { user -> user.name.contains(query, ignoreCase = true) }
      }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 72.dp, start = 16.dp, end = 16.dp)) {
      Text(
          text = stringResource(R.string.attendees),
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

      Spacer(Modifier.height(12.dp))

      SearchBar(query = query, onQueryChange = { query = it })

      Spacer(Modifier.height(16.dp))

      LazyColumn(
          verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(filteredAttendees) { user ->
              Text(text = user.name, style = MaterialTheme.typography.bodyLarge)
            }
          }
    }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
  }
}
