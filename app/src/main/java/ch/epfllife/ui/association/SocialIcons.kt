package ch.epfllife.ui.association

import androidx.annotation.DrawableRes
import ch.epfllife.R

object SocialIcons {
  val platformOrder = listOf("website", "instagram", "telegram", "linkedin", "whatsapp")
  private val platformIconMap: Map<String, Int> =
      mapOf(
          "instagram" to R.drawable.ic_instagram, // insta logo
          "telegram" to R.drawable.ic_telegram, // telegram logo
          "whatsapp" to R.drawable.ic_whatsapp, // whatsapp logo
          "linkedin" to R.drawable.ic_linkedin, // linkedin logo
          "website" to R.drawable.ic_google // assuming website uses google logo
          )

  @DrawableRes fun getIcon(platform: String): Int? = platformIconMap[platform.lowercase()]
}
