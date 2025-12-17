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
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.User
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.SearchBar
import kotlin.collections.filter

@Composable
fun AttendeeListScreen(attendees: List<User>, onBack: () -> Unit, db: Db) {
  var query by remember { mutableStateOf("") }

  // Use a specialized ViewModel or just a produceState to get current user for sorting
  val userRepo = remember(db) { db.userRepo }
  val currentUserState =
      produceState<User?>(initialValue = null) { value = userRepo.getCurrentUser() }
  val currentUser = currentUserState.value

  val sortedAttendees =
      remember(attendees, currentUser) {
        if (currentUser == null) {
          attendees
        } else {
          val following = currentUser.following.toSet()
          attendees.sortedByDescending { it.id in following }
        }
      }

  val filteredAttendees =
      remember(query, sortedAttendees) {
        sortedAttendees.filter { user -> user.name.contains(query, ignoreCase = true) }
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
              val isFollowing = currentUser?.following?.contains(user.id) == true
              AttendeeItem(user = user, isFollowing = isFollowing)
            }
          }
    }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
  }
}

@Composable
fun AttendeeItem(user: User, isFollowing: Boolean) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isFollowing) FontWeight.Bold else FontWeight.Normal)
        if (isFollowing) {
          // TODO this still looks meh (improve the design here)
          Spacer(Modifier.width(8.dp))
          // Minimal indicator
          Text(
              text = "(Following)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary)
        }
      }
}
