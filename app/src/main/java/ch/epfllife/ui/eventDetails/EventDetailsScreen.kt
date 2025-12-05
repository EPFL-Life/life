package ch.epfllife.ui.eventDetails

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.Map
import ch.epfllife.ui.theme.LifeRed
import coil.compose.AsyncImage
import coil.request.ImageRequest

object EventDetailsTestTags {
  const val LOADING_INDICATOR = "loadingIndicator"
  const val ERROR_MESSAGE = "errorMessage"
  const val EVENT_IMAGE = "eventImage"
  const val BACK_BUTTON = "backButton"
  const val EVENT_TITLE = "eventTitle"
  const val EVENT_ASSOCIATION = "eventAssociation"
  const val EVENT_PRICE = "eventPrice"
  const val EVENT_TIME = "eventTime"
  const val EVENT_LOCATION = "eventLocation"
  const val EVENT_DESCRIPTION = "eventDescription"
  const val VIEW_LOCATION_BUTTON = "viewLocationButton"
  const val ENROLL_BUTTON = "enrollButton"
  const val CONTENT = "eventDetailsContent"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    db: Db,
    viewModel: EventDetailsViewModel = viewModel { EventDetailsViewModel(db) },
    onGoBack: () -> Unit = {},
    onOpenMap: (Location) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  LaunchedEffect(eventId) {
    viewModel.loadEvent(eventId, context)
  } // this is triggered once the screen opens

  when (val state = uiState) {
    is EventDetailsUIState.Loading -> {
      // Show loading spinner
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.testTag(EventDetailsTestTags.LOADING_INDICATOR))
      }
    }

    is EventDetailsUIState.Error -> {
      // Show error message
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = state.message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag(EventDetailsTestTags.ERROR_MESSAGE),
        )
      }
    }

    is EventDetailsUIState.Success -> {
      // Show event content
      EventDetailsContent(
          event = state.event,
          isEnrolled = state.isEnrolled,
          onGoBack = onGoBack,
          onOpenMap = onOpenMap,
          onEnrollClick = { viewModel.enrollInEvent(state.event, context) },
          onUnenrollClick = { viewModel.unenrollFromEvent(state.event, context) },
      )
    }
  }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun EventDetailsContent(
    modifier: Modifier = Modifier,
    event: Event,
    isEnrolled: Boolean = false,
    onGoBack: () -> Unit,
    onOpenMap: (Location) -> Unit,
    onEnrollClick: () -> Unit,
    onUnenrollClick: () -> Unit = {},
) {
  val context = LocalContext.current
  val (formattedDate, formattedTime) =
      remember(event.time) {
        val trimmed = event.time.trim()
        val parts = trimmed.split(" ", limit = 2)
        val datePart = parts.getOrNull(0).orEmpty().ifBlank { trimmed }
        val rawTimePart = parts.getOrNull(1).orEmpty()
        val timePart =
            when {
              rawTimePart.isBlank() -> ""
              ':' in rawTimePart -> rawTimePart
              rawTimePart.count { it == '-' } == 1 -> rawTimePart.replace('-', ':')
              else -> rawTimePart
            }.ifBlank { trimmed }
        datePart to timePart
      }
  val formattedLocation =
      remember(event.location.name) {
        event.location.name.lines().map { it.trim() }.filter { it.isNotEmpty() }.joinToString(", ")
      }
  Box(
      modifier =
          modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surface)
              .testTag(EventDetailsTestTags.CONTENT)) {

        // Header with image and overlayed back button
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
          Box(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model =
                    ImageRequest.Builder(context)
                        .data(
                            event.pictureUrl
                                ?: "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png")
                        .crossfade(true)
                        .build(),
                contentDescription = "Event Image",
                modifier =
                    Modifier.fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                        .testTag(EventDetailsTestTags.EVENT_IMAGE),
                contentScale = ContentScale.Crop,
            )
          }

          // Start of Text Information
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(16.dp)
                      .background(MaterialTheme.colorScheme.surface),
              verticalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            // Row containing: Title, Club, Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                  Column {
                    Text(
                        text = event.title,
                        style =
                            MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag(EventDetailsTestTags.EVENT_TITLE),
                    )
                    Text(
                        text = event.association.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(EventDetailsTestTags.EVENT_ASSOCIATION),
                    )
                  }
                  Text(
                      text = event.price.let { "$it" },
                      style =
                          MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.testTag(EventDetailsTestTags.EVENT_PRICE),
                  )
                }

            // Row containing: Date, Time, Location
            // TODO-question: make this clickable to be displayed in Calender?
            FlowRow(
                modifier =
                    Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2,
            ) {
              Row(
                  modifier = Modifier.weight(1f, fill = false),
                  verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                      Text(
                          text = formattedDate,
                          style = MaterialTheme.typography.bodyMedium,
                          modifier = Modifier.testTag(EventDetailsTestTags.EVENT_TIME),
                      )
                      Text(
                          text = formattedLocation,
                          style = MaterialTheme.typography.bodySmall,
                          maxLines = 3,
                          overflow = TextOverflow.Ellipsis,
                          modifier = Modifier.testTag(EventDetailsTestTags.EVENT_LOCATION),
                      )
                    }
                  }
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Time",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = formattedTime, style = MaterialTheme.typography.bodyMedium)
              }
            }

            // Description
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                  text = "Description",
                  style =
                      MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              )
              Text(
                  text = event.description,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.testTag(EventDetailsTestTags.EVENT_DESCRIPTION),
              )
            }

            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))) {
              Map(
                  target = event.location,
                  enableControls = false,
                  locationPermissionRequest = {
                    val isLocationGranted =
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    it(isLocationGranted)
                  },
              )
              // Spacer required to prevent clicks on the map itself
              Spacer(
                  modifier =
                      Modifier.matchParentSize()
                          .clickable { onOpenMap(event.location) }
                          .testTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON),
              )
            }

            // Enroll Button
            Button(
                onClick = if (isEnrolled) onUnenrollClick else onEnrollClick,
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag(EventDetailsTestTags.ENROLL_BUTTON),
                shape = RoundedCornerShape(6.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = if (isEnrolled) Color.Gray else LifeRed,
                        contentColor = Color.White,
                    ),
            ) {
              Text(
                  if (isEnrolled) "Unenroll" else "Enrol in event",
                  style = MaterialTheme.typography.titleMedium)
            }
          }
        }
        BackButton(
            modifier = Modifier.align(Alignment.TopStart).testTag(EventDetailsTestTags.BACK_BUTTON),
            onGoBack = onGoBack,
        )
      }
}
