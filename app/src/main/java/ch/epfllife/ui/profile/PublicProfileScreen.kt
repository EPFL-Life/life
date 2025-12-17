package ch.epfllife.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import ch.epfllife.model.db.Db
import ch.epfllife.ui.composables.BackButton
import coil.compose.AsyncImage

object PublicProfileTestTags {
  const val SCREEN = "PublicProfileScreen"
  const val PROFILE_PICTURE = "PublicProfile_Picture"
  const val NAME_TEXT = "PublicProfile_Name"
  const val FOLLOW_BUTTON = "PublicProfile_FollowButton"
}

@Composable
fun PublicProfileScreen(
    db: Db,
    userId: String,
    onBack: () -> Unit,
    viewModel: PublicProfileViewModel = viewModel { PublicProfileViewModel(db.userRepo, userId) }
) {
  val uiState by viewModel.uiState.collectAsState()

  Box(modifier = Modifier.fillMaxSize().testTag(PublicProfileTestTags.SCREEN)) {
    when (val state = uiState) {
      is PublicProfileUiState.Loading -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is PublicProfileUiState.Error -> {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text(text = stringResource(state.message), color = MaterialTheme.colorScheme.error)
        }
      }
      is PublicProfileUiState.Success -> {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 96.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
              // Profile Picture
              if (state.user.photoUrl != null) {
                AsyncImage(
                    model = state.user.photoUrl,
                    contentDescription = state.user.name,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier.size(120.dp)
                            .clip(CircleShape)
                            .testTag(PublicProfileTestTags.PROFILE_PICTURE))
              } else {
                Box(
                    modifier =
                        Modifier.size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag(PublicProfileTestTags.PROFILE_PICTURE),
                    contentAlignment = Alignment.Center) {
                      Icon(
                          imageVector = Icons.Default.Person,
                          contentDescription = null,
                          modifier = Modifier.size(60.dp),
                          tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
              }

              Spacer(Modifier.height(24.dp))

              Text(
                  text = state.user.name,
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag(PublicProfileTestTags.NAME_TEXT))

              Spacer(Modifier.height(32.dp))

              Button(
                  onClick = { viewModel.toggleFollow() },
                  modifier = Modifier.testTag(PublicProfileTestTags.FOLLOW_BUTTON)) {
                    // TODO the Follow button could be a bit nicer
                    Text(
                        text =
                            if (state.isFollowing)
                                stringResource(ch.epfllife.R.string.follow_button_unfollow)
                            else stringResource(ch.epfllife.R.string.follow_button_follow))
                  }
            }
      }
    }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
  }
}
