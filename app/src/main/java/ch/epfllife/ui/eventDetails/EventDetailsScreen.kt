package ch.epfllife.ui.eventDetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.model.entities.Event
import ch.epfllife.model.map.Location
import ch.epfllife.ui.navigation.BottomNavigationMenu
import ch.epfllife.ui.navigation.NavigationActions
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.navigation.Tab
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(
    eventId: String,
    viewModel: EventDetailsViewModel = viewModel(),
    onGoBack: () -> Unit = {},
    navigationActions: NavigationActions? = null,
) {
  val uiState by viewModel.uiState.collectAsState()
  LaunchedEffect(eventId) {
    viewModel.loadEvent(eventId)
  } // this is triggered once the screen opens

  Scaffold(
      // use this hardcoded bottom bar for now
      bottomBar = {
        BottomNavigationMenu(
            selectedTab = Tab.Settings,
            onTabSelected = { tab -> navigationActions?.navigateTo(tab.destination) },
            modifier = Modifier.testTag(NavigationTestTags.BOTTOM_NAVIGATION_MENU))
      }) { paddingValues ->
        when (val state = uiState) {
          is EventDetailsUIState.Loading -> {
            // Show loading spinner
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator(
                      modifier = Modifier.testTag(EventDetailsTestTags.LOADING_INDICATOR))
                }
          }
          is EventDetailsUIState.Error -> {
            // Show error message
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Text(
                      text = state.message,
                      color = MaterialTheme.colorScheme.error,
                      modifier = Modifier.testTag(EventDetailsTestTags.ERROR_MESSAGE))
                }
          }
          is EventDetailsUIState.Success -> {
            // Show event content
            EventDetailsContent(
                event = state.event,
                modifier = Modifier.padding(paddingValues),
                onGoBack = onGoBack,
                viewModel = viewModel)
          }
        }
      }
}

@Composable
fun EventDetailsContent(
    event: Event,
    modifier: Modifier = Modifier,
    onGoBack: () -> Unit = {},
    viewModel: EventDetailsViewModel
) {
  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {

    // Header with image and overlayed back button
    Box(modifier = Modifier.fillMaxWidth()) {
      AsyncImage(
          model =
              ImageRequest.Builder(LocalContext.current)
                  .data(
                      event.imageUrl
                          ?: "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png")
                  .crossfade(true)
                  .build(),
          contentDescription = "Event Image",
          modifier =
              Modifier.fillMaxWidth()
                  .height(260.dp)
                  .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                  .testTag(EventDetailsTestTags.EVENT_IMAGE),
          contentScale = ContentScale.Crop)

      // Back Arrow on top of the picture (as in Mockup)
      IconButton(
          onClick = onGoBack,
          modifier =
              Modifier.padding(16.dp)
                  .align(Alignment.TopStart)
                  .size(40.dp)
                  .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                  .testTag(EventDetailsTestTags.BACK_BUTTON)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White // maybe also use a theme here
                )
          }
    }

    // Start of Text Information
    Column(
        modifier =
            Modifier.fillMaxSize().padding(16.dp).background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                      modifier = Modifier.testTag(EventDetailsTestTags.EVENT_TITLE))
                  Text(
                      text = event.associationId,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.testTag(EventDetailsTestTags.EVENT_ASSOCIATION))
                }
                Text(
                    text = event.price?.let { "CHF $it" } ?: "",
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(EventDetailsTestTags.EVENT_PRICE))
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
              verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.CalendarToday,
                      contentDescription = "Date",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                        text = event.time,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier.testTag(
                                EventDetailsTestTags
                                    .EVENT_TIME)) // TODO we need some proper time to time-text
                    // formating
                    // (implement in repository)
                    Text(
                        text = event.location.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag(EventDetailsTestTags.EVENT_LOCATION))
                  }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.AccessTime,
                      contentDescription = "Time",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(text = event.time, style = MaterialTheme.typography.bodyMedium)
                }
              }

          // Description
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag(EventDetailsTestTags.EVENT_DESCRIPTION))
          }

          // View Location
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                      .clickable {} // TODO implement navigation to map with params from Event
                      .padding(horizontal = 16.dp, vertical = 12.dp)
                      .testTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "View Location on Map",
                    style =
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary)
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Arrow",
                    tint = MaterialTheme.colorScheme.primary)
              }

          // Enroll Button
          // TODO: button should be gray and say "Enrolled" if user already enrolled -> create a
          // isEnrolled fun in viewModel
          Button(
              onClick = { viewModel.enrollInEvent(event) },
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(top = 8.dp)
                      .testTag(EventDetailsTestTags.ENROLL_BUTTON),
              shape = RoundedCornerShape(6.dp),
              colors =
                  ButtonDefaults.buttonColors(
                      containerColor = Color(0xFFDC2626), contentColor = Color.White)) {
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
          associationId = "AeroPoly",
          tags = setOf("workshop"),
          price = 10u,
          imageUrl =
              "https://www.shutterstock.com/image-photo/engineer-working-on-racing-fpv-600nw-2278353271.jpg")

  Theme() { EventDetailsContent(event = sampleEvent, viewModel = viewModel()) }
}
