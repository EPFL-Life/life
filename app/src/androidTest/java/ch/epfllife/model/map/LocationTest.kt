package ch.epfllife.model.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LocationTest {

  @Test
  fun number_types_are_converted_to_double() {
    val loc = Location(10, 20f) // Int and Float
    assertEquals(10.0, loc.latitude, 1e-9)
    assertEquals(20.0, loc.longitude, 1e-9)
    assertEquals("", loc.name)
  }

  @Test
  fun double_values_are_preserved_and_name() {
    val loc = Location(1.23, 4.56, "here")
    assertEquals(1.23, loc.latitude, 1e-9)
    assertEquals(4.56, loc.longitude, 1e-9)
    assertEquals("here", loc.name)
  }

  @Test
  fun unsupported_types_throw_IllegalArgumentException() {
    assertThrows(IllegalArgumentException::class.java) {
      Location("1.0", "2.0") // String is unsupported in current implementation
    }
    assertThrows(IllegalArgumentException::class.java) { Location(Any(), Any()) }
  }

  @Test
  fun boxed_double_values_via_Any_are_handled() {
    val loc = Location(1.23 as Any, 4.56 as Any)
    assertEquals(1.23, loc.latitude, 1e-9)
    assertEquals(4.56, loc.longitude, 1e-9)
  }
}
