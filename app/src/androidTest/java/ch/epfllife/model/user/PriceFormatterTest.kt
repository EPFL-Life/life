package ch.epfllife.model.user

import org.junit.Assert
import org.junit.Test

class PriceFormatterTest {

  fun assertFormatting(priceInCents: UInt, expected: String) {
    val result = Price(priceInCents).formatPrice()
    Assert.assertEquals(expected, result)
  }

  @Test
  fun returnsFree() {
    assertFormatting(priceInCents = 0u, expected = "Free")
  }

  @Test
  fun noCents() {
    assertFormatting(priceInCents = 300u, expected = "CHF 3.00")
  }

  @Test fun oneNumberCent() = assertFormatting(305u, "CHF 3.05")

  @Test fun multipleFrancs() = assertFormatting(priceInCents = 1250u, expected = "CHF 12.50")

  @Test fun smallPrices() = assertFormatting(priceInCents = 7u, expected = "CHF 0.07")

  @Test fun completePrice() = assertFormatting(priceInCents = 123456u, expected = "CHF 1234.56")
}
