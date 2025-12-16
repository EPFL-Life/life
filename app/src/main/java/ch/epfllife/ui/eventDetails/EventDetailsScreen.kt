package ch.epfllife.ui.eventDetails

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location
import ch.epfllife.model.user.User
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.Map
import ch.epfllife.ui.composables.ShareButton
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

private const val DEFAULT_EVENT_IMAGE_URL =
    "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png"

internal fun resolveShareImageUrl(pictureUrl: String?): String =
    pictureUrl?.trim().takeUnless { it.isNullOrEmpty() } ?: DEFAULT_EVENT_IMAGE_URL

internal fun buildShareMessage(
    context: Context,
    eventTitle: String,
    senderName: String,
    pictureUrl: String?,
): String {
  val imageUrl = resolveShareImageUrl(pictureUrl)
  val message = context.getString(R.string.share_invite_message, eventTitle, senderName)
  return "$message\n\n$imageUrl"
}

internal fun buildShareChooserIntent(context: Context, shareText: String): Intent {
  val sendIntent =
      Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
      }

  return Intent.createChooser(sendIntent, context.getString(R.string.share)).apply {
    if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
  }
}

@Composable
fun EventDetailsScreen(
    eventId: String,
    db: Db,
    onGoBack: () -> Unit = {},
    onOpenMap: (Location) -> Unit,
    onAssociationClick: (String) -> Unit,
    onOpenAttendees: (List<User>) -> Unit,
    viewModel: EventDetailsViewModel = viewModel { EventDetailsViewModel(db) },
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(eventId) { viewModel.loadEvent(eventId, context) }

  when (val state = uiState) {
    is EventDetailsUIState.Loading -> {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.testTag(EventDetailsTestTags.LOADING_INDICATOR))
      }
    }

    is EventDetailsUIState.Error -> {
      Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = state.message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.testTag(EventDetailsTestTags.ERROR_MESSAGE))
      }
    }

    is EventDetailsUIState.Success -> {
      val onShareEvent =
          remember(state.event.title, state.senderName, state.event.pictureUrl, context) {
            {
              val messageWithImage =
                  buildShareMessage(
                      context = context,
                      eventTitle = state.event.title,
                      senderName = state.senderName,
                      pictureUrl = state.event.pictureUrl,
                  )
              val chooser = buildShareChooserIntent(context, messageWithImage)

              try {
                context.startActivity(chooser)
              } catch (_: ActivityNotFoundException) {
                // No compatible activity to handle sharing on this device.
              }
            }
          }

      EventDetailsContent(
          event = state.event,
          isEnrolled = state.isEnrolled,
          attendees = state.attendees,
          onAttendeesClick = { onOpenAttendees(state.attendees) },
          onGoBack = onGoBack,
          onOpenMap = onOpenMap,
          onAssociationClick = onAssociationClick,
          onEnrollClick = { viewModel.enrollInEvent(state.event, context) },
          onUnenrollClick = { viewModel.unenrollFromEvent(state.event, context) },
          onShare = onShareEvent)
    }
  }
}

@Composable
fun EventDetailsContent(
    modifier: Modifier = Modifier,
    event: Event,
    isEnrolled: Boolean = false,
    attendees: List<User>,
    onAttendeesClick: () -> Unit,
    onGoBack: () -> Unit,
    onOpenMap: (Location) -> Unit,
    onAssociationClick: (String) -> Unit,
    onEnrollClick: () -> Unit,
    onShare: () -> Unit = {},
    onUnenrollClick: () -> Unit = {},
) {

  val (formattedDate, formattedTime) =
      remember(event.time) {
        val trimmed = event.time.trim()
        val parts = trimmed.split(" ", limit = 2)
        val datePart = parts.getOrNull(0) ?: trimmed
        val timePart = parts.getOrNull(1).orEmpty()
        datePart to timePart
      }

  val formattedLocation =
      remember(event.location.name) {
        event.location.name.lines().joinToString(", ") { it.trim() }.trim()
      }

  Box(
      modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.surface)
          .testTag(EventDetailsTestTags.CONTENT)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
          EventImage(event.pictureUrl) {
            ShareButton(modifier = Modifier.align(Alignment.TopEnd), onShare = onShare)
          }

          Column(
              Modifier.fillMaxWidth().padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
                EventHeader(
                    title = event.title,
                    associationName = event.association.name,
                    price = event.price.toString(),
                    onAssociationClick = { onAssociationClick(event.association.id) })

                EventDateTimeLocation(
                    formattedDate = formattedDate,
                    formattedTime = formattedTime,
                    formattedLocation = formattedLocation)

                EventAttendanceRow(attendeeCount = attendees.size, onClick = onAttendeesClick)

                EventDescription(event.description)

                EventMapSection(
                    location = event.location, onOpenMap = { onOpenMap(event.location) })

                EventEnrollmentSection(
                    isEnrolled = isEnrolled,
                    onEnrollClick = onEnrollClick,
                    onUnenrollClick = onUnenrollClick)
              }
        }

        BackButton(
            modifier = Modifier.align(Alignment.TopStart).testTag(EventDetailsTestTags.BACK_BUTTON),
            onGoBack = onGoBack)
      }
}

@Composable
private fun EventImage(
    pictureUrl: String?,
    modifier: Modifier = Modifier,
    overlayContent: @Composable BoxScope.() -> Unit
) {
  val context = LocalContext.current

  Box(modifier = modifier.fillMaxWidth().height(260.dp)) {
    AsyncImage(
        model =
            ImageRequest.Builder(context)
                .data(
                    pictureUrl
                        ?: "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png")
                .crossfade(true)
                .build(),
        contentDescription = "Event Image",
        modifier =
            Modifier.matchParentSize()
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .testTag(EventDetailsTestTags.EVENT_IMAGE),
        contentScale = ContentScale.Crop)

    overlayContent()
  }
}

@Composable
private fun EventHeader(
    title: String,
    associationName: String,
    price: String,
    onAssociationClick: () -> Unit
) {
  Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Column(Modifier.weight(1f, fill = false)) {
      Text(
          title,
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          modifier = Modifier.testTag(EventDetailsTestTags.EVENT_TITLE),
          maxLines = Int.MAX_VALUE,
      )
      Text(
          associationName,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier =
              Modifier.testTag(EventDetailsTestTags.EVENT_ASSOCIATION).clickable {
                onAssociationClick()
              })
    }
    Spacer(Modifier.width(8.dp))
    Text(
        price,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
        modifier = Modifier.testTag(EventDetailsTestTags.EVENT_PRICE))
  }
}

@Composable
private fun EventDateTimeLocation(
    formattedDate: String,
    formattedTime: String,
    formattedLocation: String
) {
  Row(
      Modifier.fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(12.dp)) {
        Row(Modifier.weight(1f)) {
          Icon(
              Icons.Default.CalendarToday,
              contentDescription = "Date",
          )
          Spacer(Modifier.width(8.dp))
          Column {
            Text(
                formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.testTag(EventDetailsTestTags.EVENT_TIME))
            Text(
                formattedLocation,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag(EventDetailsTestTags.EVENT_LOCATION))
          }
        }

        Spacer(Modifier.width(16.dp))

        Row {
          Icon(Icons.Default.AccessTime, contentDescription = "Time")
          Spacer(Modifier.width(8.dp))
          Text(formattedTime, style = MaterialTheme.typography.bodyMedium)
        }
      }
}

@Composable
private fun EventAttendanceRow(attendeeCount: Int, onClick: () -> Unit) {
  Row(
      modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Group,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$attendeeCount attending",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

@Composable
private fun EventDescription(description: String) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
        "Description",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
    var isExpanded by remember { mutableStateOf(false) }
    var showShowMore by remember { mutableStateOf(false) }

    Text(
        description,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
          if (textLayoutResult.hasVisualOverflow) {
            showShowMore = true
          }
        },
        modifier = Modifier.testTag(EventDetailsTestTags.EVENT_DESCRIPTION))

    if (showShowMore) {
      Text(
          text =
              if (isExpanded) stringResource(id = R.string.show_less)
              else stringResource(id = R.string.show_more),
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray,
          modifier = Modifier.clickable { isExpanded = !isExpanded })
    }
  }
}

@Composable
private fun EventMapSection(location: Location, onOpenMap: () -> Unit) {
  val context = LocalContext.current
  Box(Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(8.dp))) {
    Map(
        target = location,
        enableControls = false,
        locationPermissionRequest = {
          val granted =
              ContextCompat.checkSelfPermission(
                  context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                  PackageManager.PERMISSION_GRANTED
          it(granted)
        },
    )

    Spacer(
        Modifier.matchParentSize()
            .clickable { onOpenMap() }
            .testTag(EventDetailsTestTags.VIEW_LOCATION_BUTTON))
  }
}

@Composable
private fun EventEnrollmentSection(
    isEnrolled: Boolean,
    onEnrollClick: () -> Unit,
    onUnenrollClick: () -> Unit
) {
  Button(
      onClick = if (isEnrolled) onUnenrollClick else onEnrollClick,
      modifier =
          Modifier.fillMaxWidth().padding(top = 8.dp).testTag(EventDetailsTestTags.ENROLL_BUTTON),
      shape = RoundedCornerShape(6.dp),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = if (isEnrolled) Color.Gray else LifeRed,
              contentColor = Color.White)) {
        Text(if (isEnrolled) "Unenroll" else "Enrol in event")
      }
}
