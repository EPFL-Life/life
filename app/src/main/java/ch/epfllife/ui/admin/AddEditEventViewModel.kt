package ch.epfllife.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location
import ch.epfllife.model.user.Price
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddEditEventFormState(
    val title: String = "",
    val description: String = "",
    val locationName: String = "",
    val time: String = "",
    val priceText: String = "",
    val pictureUrl: String = "",
    val tagsText: String = ""
)

class AddEditEventViewModel(
    private val db: Db,
    private val association: Association,
    private val initialEvent: Event? = null
) : ViewModel() {

  private val _formState = MutableStateFlow(AddEditEventFormState())
  val formState: StateFlow<AddEditEventFormState> = _formState

  init {
    initialEvent?.let { populateFromEvent(it) }
  }

  private fun populateFromEvent(e: Event) {
    _formState.value =
        AddEditEventFormState(
            title = e.title,
            description = e.description,
            locationName = e.location.name,
            time = e.time,
            priceText = e.price.toString(),
            pictureUrl = e.pictureUrl ?: "",
            tagsText = e.tags.joinToString(","))
  }

  fun updateTitle(v: String) = update { copy(title = v) }

  fun updateDescription(v: String) = update { copy(description = v) }

  fun updateLocationName(v: String) = update { copy(locationName = v) }

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
        s.time.isNotBlank()
  }

  fun submit(onComplete: () -> Unit) {
    viewModelScope.launch {
      val s = _formState.value

      val price = Price(s.priceText.toUIntOrNull() ?: 0u)

      val location = Location(latitude = 0.0, longitude = 0.0, name = s.locationName)

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
