package ch.epfllife.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import ch.epfllife.model.association.Association
import ch.epfllife.ui.association.SocialIcons

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

class AddEditAssociationViewModel(existingAssociation: Association? = null) : ViewModel() {

  var formState by mutableStateOf(AssociationFormState())
    private set

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
    val updatedList =
        formState.socialMedia.map { if (it.platform == platform) it.copy(link = link) else it }
    formState = formState.copy(socialMedia = updatedList)
  }

  fun updateLogoUrl(url: String) {
    formState = formState.copy(logoUrl = url)
  }

  fun updateBannerUrl(url: String) {
    formState = formState.copy(bannerUrl = url)
  }

  fun isFormValid(): Boolean {
    return formState.name.isNotBlank() &&
        formState.description.isNotBlank() &&
        formState.about.isNotBlank()
  }

  fun submit(onSuccess: () -> Unit) {
    if (!isFormValid()) return
    // TODO: send data to Firebase
    onSuccess()
  }
}
