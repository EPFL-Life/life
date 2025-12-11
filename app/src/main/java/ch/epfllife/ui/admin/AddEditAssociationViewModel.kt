package ch.epfllife.ui.admin

import android.util.Log
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.R
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.EventCategory
import ch.epfllife.ui.association.SocialIcons
import java.net.URI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SocialMediaEntry(
    val platform: String,
    var enabled: Boolean = false,
    var link: String = ""
)

data class AssociationFormState(
    var name: String = "",
    var description: String = "", // short description
    var about: String = "", // long description
    var socialMedia: List<SocialMediaEntry> =
        SocialIcons.platformOrder.map { SocialMediaEntry(it) },
    var logoUrl: String = "",
    var bannerUrl: String = "",
    var eventCategory: EventCategory = EventCategory.OTHER
)

sealed interface AddEditAssociationUIState {
  object Loading : AddEditAssociationUIState

  data class Error(val messageRes: Int) : AddEditAssociationUIState

  object Success : AddEditAssociationUIState
}

class AddEditAssociationViewModel(
    private val db: Db,
    private val associationId: String? = null,
) : ViewModel() {

  private val repo = db.assocRepo

  var formState by mutableStateOf(AssociationFormState())
    private set

  private val _uiState =
      MutableStateFlow<AddEditAssociationUIState>(AddEditAssociationUIState.Loading)
  val uiState: StateFlow<AddEditAssociationUIState> = _uiState

  val isEditing: Boolean = associationId != null

  var initialAssociationName: String = ""
    private set

  private var associationSnapshot: Association? = null

  init {
    if (isEditing) {
      loadData(associationId!!)
    } else {
      _uiState.value = AddEditAssociationUIState.Success
    }
  }

  private fun loadData(id: String) {
    viewModelScope.launch {
      _uiState.value = AddEditAssociationUIState.Loading
      try {
        val assoc = repo.getAssociation(id) ?: throw IllegalStateException("Association not found")
        populateForm(assoc)
        _uiState.value = AddEditAssociationUIState.Success
      } catch (e: Exception) {
        Log.e("AddEditAssociationVM", "Failed to load association", e)
        _uiState.value = AddEditAssociationUIState.Error(R.string.error_loading_association)
      }
    }
  }

  private fun populateForm(assoc: Association) {
    associationSnapshot = assoc
    initialAssociationName = assoc.name

    val socialList =
        SocialIcons.platformOrder.map { platform ->
          SocialMediaEntry(
              platform = platform,
              enabled = assoc.socialLinks?.containsKey(platform) == true,
              link = assoc.socialLinks?.get(platform) ?: "")
        }

    formState =
        AssociationFormState(
            name = assoc.name,
            description = assoc.description,
            about = assoc.about ?: "",
            socialMedia = socialList,
            logoUrl = assoc.logoUrl ?: "",
            bannerUrl = assoc.pictureUrl ?: "",
            eventCategory = assoc.eventCategory)
  }

  // URL Validation
  private fun isValidUrl(url: String): Boolean {
    if (url.isBlank()) return false

    // Require http/https to avoid malformed schemes
    if (!url.startsWith("http://") && !url.startsWith("https://")) return false

    if (!Patterns.WEB_URL.matcher(url).matches()) return false

    return try {
      URI(url)
      true
    } catch (_: Exception) {
      false
    }
  }

  // main functions

  fun updateName(value: String) {
    formState = formState.copy(name = value)
  }

  fun updateDescription(value: String) {
    formState = formState.copy(description = value)
  }

  fun updateAbout(value: String) {
    formState = formState.copy(about = value)
  }

  fun updateSocialMedia(platform: String, enabled: Boolean) {
    val updatedList =
        formState.socialMedia.map {
          if (it.platform == platform) it.copy(enabled = enabled) else it
        }
    formState = formState.copy(socialMedia = updatedList)
  }

  fun updateSocialMediaLink(platform: String, link: String) {
    val sanitised = link.trim()
    val updatedList =
        formState.socialMedia.map { if (it.platform == platform) it.copy(link = sanitised) else it }
    formState = formState.copy(socialMedia = updatedList)
  }

  fun updateEventCategory(category: EventCategory) {
    formState = formState.copy(eventCategory = category)
  }

  fun updateLogoUrl(url: String) {
    formState = formState.copy(logoUrl = url.trim())
  }

  fun updateBannerUrl(url: String) {
    formState = formState.copy(bannerUrl = url.trim())
  }

  // form validation
  fun isFormValid(): Boolean {
    val hasRequiredFields =
        formState.name.isNotBlank() &&
            formState.description.isNotBlank() &&
            formState.about.isNotBlank()
    val urlsAreValid =
        (formState.logoUrl.isBlank() || isValidUrl(formState.logoUrl)) &&
            (formState.bannerUrl.isBlank() || isValidUrl(formState.bannerUrl)) &&
            formState.socialMedia.all { entry -> !entry.enabled || isValidUrl(entry.link) }

    return hasRequiredFields && urlsAreValid
  }

  fun submit(onSuccess: () -> Unit) {
    if (!isFormValid()) return

    viewModelScope.launch() {
      val association = buildAssociation()
      val result =
          if (isEditing) {
            repo.updateAssociation(associationId!!, association)
          } else {
            repo.createAssociation(association)
          }

      result
          .onSuccess {
            _uiState.value = AddEditAssociationUIState.Success
            onSuccess()
          }
          .onFailure {
            Log.e("AddEditAssociationVM", "Failed to submit association", it)
            _uiState.value = AddEditAssociationUIState.Error(R.string.error_loading_association)
          }
    }
  }

  private fun buildAssociation(): Association {
    val socialLinks =
        formState.socialMedia
            .filter { it.enabled && it.link.isNotBlank() }
            .associate { it.platform to it.link.trim() }

    val associationUid = associationId ?: repo.getNewUid()

    return Association(
        id = associationUid,
        name = formState.name.trim(),
        description = formState.description.trim(),
        pictureUrl = formState.bannerUrl.ifBlank { null },
        logoUrl = formState.logoUrl.ifBlank { null },
        eventCategory = formState.eventCategory,
        about = formState.about.trim().ifBlank { null },
        socialLinks = if (socialLinks.isEmpty()) null else socialLinks)
  }
}
