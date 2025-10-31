package ch.epfllife.ui.composables

object PriceFormatter {

  fun formatPrice(priceInRappen: UInt): String {
    return if (priceInRappen == 0u) {
      "Free"
    } else {
      val francs = priceInRappen.toInt() / 100
      val rappen = priceInRappen.toInt() % 100
      String.format("CHF %d.%02d", francs, rappen)
    }
  }
}
