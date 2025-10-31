package ch.epfllife.ui.composables

import android.annotation.SuppressLint

class Price {

  val price: UInt

  constructor(price: UInt) {
    this.price = price
  }

  @SuppressLint("DefaultLocale")
  fun formatPrice(): String {
    return if (price == 0u) {
      "Free"
    } else {
      val francs = price.toInt() / 100
      val rappen = price.toInt() % 100
      String.format("CHF %d.%02d", francs, rappen)
    }
  }
}
