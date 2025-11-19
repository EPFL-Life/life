package ch.epfllife.ui.eventDetails

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.model.association.Association
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.map.Location
import ch.epfllife.model.user.Price
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.Map
import ch.epfllife.ui.theme.LifeRed
import ch.epfllife.ui.theme.Theme
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
    viewModel: EventDetailsViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    onOpenMap: (Location) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsState()
  LaunchedEffect(eventId) {
    viewModel.loadEvent(eventId)
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
          onGoBack = onGoBack,
          onOpenMap = onOpenMap,
          onEnrollClick = { viewModel.enrollInEvent(state.event) },
      )
    }
  }
}

@Composable
fun EventDetailsContent(
    event: Event,
    modifier: Modifier = Modifier,
    onGoBack: () -> Unit,
    onOpenMap: (Location) -> Unit,
    onEnrollClick: () -> Unit,
) {
  val context = LocalContext.current
  Column(
      modifier =
          modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.surface)
              .testTag(EventDetailsTestTags.CONTENT)) {

        // Header with image and overlayed back button
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

          // Back Arrow on top of the picture (as in Mockup)
          BackButton(
              modifier =
                  Modifier.align(Alignment.TopStart).testTag(EventDetailsTestTags.BACK_BUTTON),
              onGoBack = onGoBack,
          )
        }

        // Start of Text Information
        Column(
            modifier =
                Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colorScheme.surface),
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
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                      .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Default.CalendarToday,
                  contentDescription = "Date",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                    text = event.time,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(EventDetailsTestTags.EVENT_TIME),
                ) // TODO we need some proper time to time-text
                // formating
                // (implement in repository)
                Text(
                    text = event.location.name,
                    style = MaterialTheme.typography.bodySmall,
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
              Text(text = event.time, style = MaterialTheme.typography.bodyMedium)
            }
          }

          // Description
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
          // TODO: button should be gray and say "Enrolled" if user already enrolled -> create a
          // isEnrolled fun in viewModel
          Button(
              onClick = onEnrollClick,
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(top = 8.dp)
                      .testTag(EventDetailsTestTags.ENROLL_BUTTON),
              shape = RoundedCornerShape(6.dp),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = LifeRed,
                      contentColor = Color.White,
                  ),
          ) {
            Text("Enrol in event", style = MaterialTheme.typography.titleMedium)
          }
        }
      }
}

// ------------- Use this for Preview ------------------

@Preview(showBackground = true)
@Composable
fun EventDetailsPreview() {
  val sampleEvent =
      Event(
          id = "1",
          title = "Drone Workshop",
          description =
              "The Drone Workshop is a multi-evening workshop organized by AéroPoly, where you can build your own 3-inch FPV drone...",
          location = Location(46.5191, 6.5668, "Centre Sport et Santé"),
          time = "2025-10-12 18:00",
          association =
              Association(
                  id = "dkjaend38rh",
                  name = "AeroPoly",
                  description = "AéroPoly is the EPFL drone club.",
                  eventCategory = EventCategory.ACADEMIC,
              ),
          tags = listOf("workshop"),
          price = Price(10u),
          pictureUrl =
              "https://www.shutterstock.com/image-photo/engineer-working-on-racing-fpv-600nw-2278353271.jpg",
      )

  Theme() {
    EventDetailsContent(event = sampleEvent, onOpenMap = {}, onGoBack = {}, onEnrollClick = {})
  }
}
