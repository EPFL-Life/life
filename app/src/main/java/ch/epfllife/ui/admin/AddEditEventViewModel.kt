package ch.epfllife.ui.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.R
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location
import ch.epfllife.model.map.LocationRepository
import ch.epfllife.model.user.Price
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddEditEventFormState(
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val resolvedLocationName: String? = null,
    val isLocationSearching: Boolean = false,
    val locationErrorRes: Int? = null,
    val lastResolvedQuery: String? = null,
    val time: String = "",
    val priceText: String = "",
    val pictureUrl: String = "",
    val tagsText: String = "",
    val isUploadingImage: Boolean = false
)

sealed interface AddEditEventUIState {
  object Loading : AddEditEventUIState

  data class Error(val messageRes: Int) : AddEditEventUIState

  data class Success(val association: Association, val event: Event?) : AddEditEventUIState
}

class AddEditEventViewModel(
    private val db: Db,
    private val associationId: String,
    private val eventId: String? = null,
    private val locationRepository: LocationRepository
) : ViewModel() {

  companion object {
    private const val LOCATION_QUERY_MIN_CHARS = 3
    private const val LOCATION_SEARCH_DEBOUNCE_MS = 700L
  }

  private val _uiState = MutableStateFlow<AddEditEventUIState>(AddEditEventUIState.Loading)
  val uiState: StateFlow<AddEditEventUIState> = _uiState

  private val _formState = MutableStateFlow(AddEditEventFormState())
  val formState: StateFlow<AddEditEventFormState> = _formState

  private var locationSearchJob: Job? = null
  private var loadedAssociation: Association? = null
  private var loadedEvent: Event? = null

  init {
    loadData()
  }

  private fun loadData() {
    viewModelScope.launch {
      _uiState.value = AddEditEventUIState.Loading
      try {
        val association =
            db.assocRepo.getAssociation(associationId)
                ?: throw IllegalStateException("Association not found")
        loadedAssociation = association

        val event =
            if (eventId != null) {
              db.eventRepo.getEvent(eventId) ?: throw IllegalStateException("Event not found")
            } else {
              null
            }
        loadedEvent = event

        event?.let { populateFromEvent(it) }
        _uiState.value = AddEditEventUIState.Success(association, event)
      } catch (e: Exception) {
        Log.e("AddEditEventVM", "Failed to load event", e)
        _uiState.value = AddEditEventUIState.Error(R.string.error_loading_event)
      }
    }
  }

  private fun populateFromEvent(e: Event) {
    _formState.value =
        AddEditEventFormState(
            title = e.title,
            description = e.description,
            locationName = e.location.name,
            locationLatitude = e.location.latitude,
            locationLongitude = e.location.longitude,
            resolvedLocationName = e.location.name,
            lastResolvedQuery = e.location.name,
            time = e.time,
            priceText = e.price.toString(),
            pictureUrl = e.pictureUrl ?: "",
            tagsText = e.tags.joinToString(","))
  }

  fun updateTitle(v: String) = update { copy(title = v) }

  fun updateDescription(v: String) = update { copy(description = v) }

  fun updateLocationName(v: String) {
    update {
      copy(
          locationName = v,
          locationLatitude = null,
          locationLongitude = null,
          resolvedLocationName = null,
          lastResolvedQuery = null,
          locationErrorRes = null)
    }
    scheduleLocationLookup(v)
  }

  private fun scheduleLocationLookup(currentValue: String) {
    locationSearchJob?.cancel()
    val trimmed = currentValue.trim()
    if (trimmed.length < LOCATION_QUERY_MIN_CHARS) return
    locationSearchJob =
        viewModelScope.launch {
          delay(LOCATION_SEARCH_DEBOUNCE_MS)
          val latest = _formState.value.locationName.trim()
          val alreadyResolved =
              _formState.value.lastResolvedQuery == latest &&
                  _formState.value.locationLatitude != null &&
                  _formState.value.locationLongitude != null
          if (!alreadyResolved && latest.length >= LOCATION_QUERY_MIN_CHARS) {
            performLocationSearch(latest, autoTriggered = true)
          }
        }
  }

  fun onManualLocationLookup() {
    performLocationSearch(_formState.value.locationName.trim(), autoTriggered = false)
  }

  fun onImageSelected(uri: android.net.Uri) {
    viewModelScope.launch {
      update { copy(isUploadingImage = true) }
      try {
        val idToUse = loadedEvent?.id ?: eventId ?: stableId

        Log.d("AddEditEventVM", "Uploading image for event $idToUse")
        db.eventRepo
            .uploadEventImage(idToUse, uri)
            .onSuccess { url -> update { copy(pictureUrl = url, isUploadingImage = false) } }
            .onFailure { e ->
              Log.e("AddEditEventVM", "Failed to upload image", e)
              update { copy(isUploadingImage = false) }
            }
      } catch (_: Exception) {
        update { copy(isUploadingImage = false) }
      }
    }
  }

  // Lazy property for new event ID
  private val stableId: String by lazy { db.eventRepo.getNewUid() }

  private fun performLocationSearch(query: String, autoTriggered: Boolean) {
    locationSearchJob?.cancel()
    if (query.length < LOCATION_QUERY_MIN_CHARS) {
      if (!autoTriggered) {
        update { copy(locationErrorRes = R.string.event_location_query_too_short) }
      }
      return
    }

    val alreadyResolved =
        _formState.value.lastResolvedQuery == query &&
            _formState.value.locationLatitude != null &&
            _formState.value.locationLongitude != null
    if (alreadyResolved) return
    if (_formState.value.isLocationSearching) return

    viewModelScope.launch {
      update { copy(isLocationSearching = true, locationErrorRes = null) }
      try {
        val results = locationRepository.search(query)
        val match = results.firstOrNull()
        if (match != null) {
          update {
            copy(
                locationLatitude = match.latitude,
                locationLongitude = match.longitude,
                resolvedLocationName = match.name,
                lastResolvedQuery = query,
                isLocationSearching = false,
                locationErrorRes = null)
          }
        } else {
          update {
            copy(
                isLocationSearching = false,
                locationLatitude = null,
                locationLongitude = null,
                resolvedLocationName = null,
                locationErrorRes = R.string.event_location_no_match)
          }
        }
      } catch (_: Exception) {
        update {
          copy(
              isLocationSearching = false,
              locationLatitude = null,
              locationLongitude = null,
              resolvedLocationName = null,
              locationErrorRes = R.string.event_location_lookup_failed)
        }
      }
    }
  }

  fun updateTime(v: String) = update { copy(time = v) }

  fun updatePriceText(v: String) = update { copy(priceText = v) }

  fun updatePictureUrl(v: String) = update { copy(pictureUrl = v) }

  fun updateTagsText(v: String) = update { copy(tagsText = v) }

  private inline fun update(fn: AddEditEventFormState.() -> AddEditEventFormState) {
    _formState.value = _formState.value.fn()
  }

  fun isFormValid(): Boolean {
    val s = _formState.value
    return s.title.isNotBlank() &&
        s.description.isNotBlank() &&
        s.locationName.isNotBlank() &&
        s.locationLatitude != null &&
        s.locationLongitude != null &&
        s.time.isNotBlank()
  }

  fun submit(onComplete: () -> Unit) {
    viewModelScope.launch {
      val s = _formState.value
      val association = loadedAssociation ?: return@launch

      val price = Price(s.priceText.toUIntOrNull() ?: 0u)

      val lat = s.locationLatitude ?: 0.0
      val lon = s.locationLongitude ?: 0.0
      val locationName = s.resolvedLocationName ?: s.locationName
      val location = Location(latitude = lat, longitude = lon, name = locationName)

      val tags = s.tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

      val eventIdToUse = loadedEvent?.id ?: eventId ?: stableId

      val event =
          (loadedEvent?.copy(
              title = s.title,
              description = s.description,
              location = location,
              time = s.time,
              price = price,
              pictureUrl = s.pictureUrl.ifBlank { null },
              tags = tags)
              ?: Event(
                  id = eventIdToUse,
                  title = s.title,
                  description = s.description,
                  location = location,
                  time = s.time,
                  association = association,
                  price = price,
                  pictureUrl = s.pictureUrl.ifBlank { null },
                  tags = tags))

      if (loadedEvent == null) db.eventRepo.createEvent(event)
      else db.eventRepo.updateEvent(event.id, event)

      onComplete()
    }
  }
}
