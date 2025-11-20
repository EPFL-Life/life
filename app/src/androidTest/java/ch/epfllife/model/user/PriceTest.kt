package ch.epfllife.model.user

import org.junit.Assert
import org.junit.Test

class PriceTest {

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

  // Just ensure it doesn't crash with extremely large values
  @Test
  fun edgeCase_maxUIntValue() {
    val price = Price(UInt.MAX_VALUE)
    price.formatPrice()
  }

  @Test
  fun toCents_returnsCorrectValue() {
    val price = Price(150u)
    Assert.assertEquals(150u, price.toCents())
  }

  @Test
  fun toCentsLong_returnsCorrectValue() {
    val price = Price(150u)
    Assert.assertEquals(150L, price.toCentsLong())
  }
}
