package ch.epfllife.ui.admin

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
    val tagsText: String = ""
)

class AddEditEventViewModel(
    private val db: Db,
    private val association: Association,
    private val initialEvent: Event? = null,
    private val locationRepository: LocationRepository
) : ViewModel() {

  companion object {
    private const val LOCATION_QUERY_MIN_CHARS = 3
    private const val LOCATION_SEARCH_DEBOUNCE_MS = 700L
  }

  private val _formState = MutableStateFlow(AddEditEventFormState())
  val formState: StateFlow<AddEditEventFormState> = _formState

  private var locationSearchJob: Job? = null

  init {
    initialEvent?.let { populateFromEvent(it) }
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

      val price = Price(s.priceText.toUIntOrNull() ?: 0u)

      val lat = s.locationLatitude ?: 0.0
      val lon = s.locationLongitude ?: 0.0
      val locationName = s.resolvedLocationName ?: s.locationName
      val location = Location(latitude = lat, longitude = lon, name = locationName)

      val tags = s.tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

      val event =
          (initialEvent?.copy(
              title = s.title,
              description = s.description,
              location = location,
              time = s.time,
              price = price,
              pictureUrl = s.pictureUrl.ifBlank { null },
              tags = tags)
              ?: Event(
                  id = "local-${System.currentTimeMillis()}",
                  title = s.title,
                  description = s.description,
                  location = location,
                  time = s.time,
                  association = association,
                  price = price,
                  pictureUrl = s.pictureUrl.ifBlank { null },
                  tags = tags))

      // TODO: implement repo calls once backend ready:
      // if (initialEvent == null) db.eventRepo.createEvent(event)
      // else db.eventRepo.updateEvent(event)

      onComplete()
    }
  }
}
