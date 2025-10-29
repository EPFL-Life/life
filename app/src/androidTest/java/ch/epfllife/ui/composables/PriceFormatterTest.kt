package ch.epfllife.ui.composables


import org.junit.Test
import org.junit.Assert.assertEquals

class PriceFormatterTest {

    @Test
    fun returnsFree() {
        val result = PriceFormatter.formatPrice(0u)
        assertEquals("Free", result)
    }

    @Test
    fun noCents() {
        val result = PriceFormatter.formatPrice(300u)
        assertEquals("CHF 3.00", result)
    }

    @Test
    fun oneNumberCent() {
        val result = PriceFormatter.formatPrice(305u)
        assertEquals("CHF 3.05", result)
    }

    @Test
    fun multipleFrancs() {
        val result = PriceFormatter.formatPrice(1250u)
        assertEquals("CHF 12.50", result)
    }

    @Test
    fun smallPrices() {
        val result = PriceFormatter.formatPrice(75u)
        assertEquals("CHF 0.75", result)
    }

    @Test
    fun completePrice() {
        val result = PriceFormatter.formatPrice(123456u)
        assertEquals("CHF 1234.56", result)
    }
}
