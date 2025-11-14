package ch.epfllife.model.user

import android.annotation.SuppressLint

@JvmInline
value class Price(val cents: UInt) {

  /** Return the underlying amount in rappen/cents. */
  fun toCents(): UInt = cents

  /** Return the underlying amount as a Long (useful for arithmetic without overflow on Int). */
  fun toCentsLong(): Long = cents.toLong()

  /**
   * Actual formating logic Examples:
   * - Price(0u).formatPrice() -> "Free"
   * - Price(300u).formatPrice() -> "CHF 3.00"
   */
  @SuppressLint("DefaultLocale")
  fun formatPrice(): String {
    return if (cents == 0u) {
      "Free"
    } else {
      val francs = cents.toInt() / 100
      val rappen = cents.toInt() % 100
      String.format("CHF %d.%02d", francs, rappen)
    }
  }

  /** Friendly toString delegating to formatPrice() so debugging output is readable. */
  override fun toString(): String = formatPrice()
}
