package ch.epfllife.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.user.User
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.SearchBar
import coil.compose.AsyncImage

object ManageFriendsTestTags {
  const val SEARCH_BAR = "ManageFriends_SearchBar"
  const val USER_LIST = "ManageFriends_UserList"
  const val USER_ITEM = "ManageFriends_UserItem"
}

@Composable
fun ManageFriendsScreen(
    db: Db,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: ManageFriendsViewModel = viewModel { ManageFriendsViewModel(db.userRepo) }
) {
  val uiState by viewModel.uiState.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()

  Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().padding(top = 72.dp, start = 16.dp, end = 16.dp)) {
      Text(
          text = stringResource(ch.epfllife.R.string.manage_friends_title),
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

      Spacer(Modifier.height(12.dp))

      SearchBar(
          query = searchQuery,
          onQueryChange = viewModel::onSearchQueryChanged,
          modifier = Modifier.testTag(ManageFriendsTestTags.SEARCH_BAR))

      Spacer(Modifier.height(16.dp))

      when (val state = uiState) {
        is ManageFriendsUiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }
        is ManageFriendsUiState.Error -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = androidx.compose.ui.res.stringResource(state.message),
                color = MaterialTheme.colorScheme.error)
          }
        }
        is ManageFriendsUiState.Success -> {
          if (state.users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text(
                  text = stringResource(ch.epfllife.R.string.manage_friends_no_users_found),
                  style = MaterialTheme.typography.bodyLarge)
            }
          } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().testTag(ManageFriendsTestTags.USER_LIST)) {
                  items(state.users) { user ->
                    UserListItem(user = user, onClick = { onUserClick(user.id) })
                  }
                }
          }
        }
      }
    }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
  }
}

@Composable
fun UserListItem(user: User, onClick: () -> Unit) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .clickable(onClick = onClick)
              .testTag(ManageFriendsTestTags.USER_ITEM),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
              if (user.photoUrl != null) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp).clip(CircleShape))
              } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Spacer(Modifier.width(16.dp))

              Text(
                  text = user.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.Medium)
            }
      }
}
