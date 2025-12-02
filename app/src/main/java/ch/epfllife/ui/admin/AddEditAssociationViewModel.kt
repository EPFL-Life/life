package ch.epfllife.ui.admin

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ch.epfllife.model.association.Association
import ch.epfllife.ui.association.SocialIcons
import java.net.URI

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
    var bannerUrl: String = ""
)

class AddEditAssociationViewModel(private val existingAssociation: Association? = null) :
    ViewModel() {

  var formState by mutableStateOf(AssociationFormState())
    private set

  val isEditing: Boolean = existingAssociation != null

  val initialAssociationName: String = existingAssociation?.name ?: ""

  init {
    existingAssociation?.let { assoc ->
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
              bannerUrl = assoc.pictureUrl ?: "")
    }
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
    // TODO: send data to Firebase
    onSuccess()
  }
}
