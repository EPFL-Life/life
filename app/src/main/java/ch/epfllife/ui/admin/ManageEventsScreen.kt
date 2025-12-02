package ch.epfllife.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.composables.EventCard
import ch.epfllife.ui.composables.Refreshable
import ch.epfllife.ui.composables.SettingsButton

@Composable
fun ManageEventsScreen(
    db: Db,
    associationId: String,
    onGoBack: () -> Unit,
    onAddNewEvent: (String) -> Unit,
    onEditEvent: (String) -> Unit,
    viewModel: ManageEventsViewModel = viewModel { ManageEventsViewModel(db, associationId) }
) {
  val uiState by viewModel.uiState.collectAsState()

  Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
    Refreshable(onRefresh = { finishRefreshing -> viewModel.reload { finishRefreshing() } }) {
      when (uiState) {
        is ManageEventsUIState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }

        is ManageEventsUIState.Error -> {
          val msg = (uiState as ManageEventsUIState.Error).message
          Column(
              modifier = Modifier.fillMaxSize().padding(32.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.error_loading_events, msg),
                    color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { viewModel.reload() }) { Text(stringResource(R.string.retry)) }
              }
        }

        is ManageEventsUIState.Success -> {
          val events = (uiState as ManageEventsUIState.Success).events

          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .verticalScroll(rememberScrollState())
                      .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.manage_events_title),
                    style = MaterialTheme.typography.titleLarge)
                HorizontalDivider()

                SettingsButton(
                    text = stringResource(R.string.add_new_event),
                    onClick = { onAddNewEvent(associationId) })

                if (events.isEmpty()) {
                  Spacer(Modifier.height(12.dp))
                  Text(
                      text = stringResource(R.string.manage_events_empty),
                      style = MaterialTheme.typography.bodyMedium)
                } else {
                  events.forEach { event: Event ->
                    EventCard(event = event, onClick = { onEditEvent(event.id) })
                  }
                }
              }
        }
      }
    }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onGoBack)
  }
}
