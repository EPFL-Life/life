package ch.epfllife.model.enums

import ch.epfllife.model.event.EventCategory
import ch.epfllife.model.event.displayString
import org.junit.Assert.*
import org.junit.Test

class CategoryTests {

  // --- Core behaviour --------------------------------------------------------

  @Test
  fun displayString_capitalizes_only_first_letter() {
    assertEquals("Culture", EventCategory.CULTURE.displayString())
    assertEquals("Sports", EventCategory.SPORTS.displayString())
    assertEquals("Tech", EventCategory.TECH.displayString())
    assertEquals("Social", EventCategory.SOCIAL.displayString())
    assertEquals("Academic", EventCategory.ACADEMIC.displayString())
    assertEquals("Career", EventCategory.CAREER.displayString())
    assertEquals("Other", EventCategory.OTHER.displayString())
  }

  @Test
  fun displayString_does_not_overcapitalize_when_already_proper() {
    assertEquals("Tech", EventCategory.TECH.displayString())
  }

  @Test
  fun displayString_outputs_are_unique_and_non_empty() {
    val outputs = EventCategory.entries.map { it.displayString() }
    assertEquals(outputs.size, outputs.distinct().size)
    outputs.forEach { assertTrue(it.isNotEmpty()) }
  }

  @Test
  fun displayString_is_Titlecase_formatted() {
    EventCategory.entries.forEach {
      val display = it.displayString()
      assertTrue(display.first().isUpperCase())
      assertTrue(display.drop(1).all { ch -> ch.isLowerCase() })
    }
  }

  // --- Enum structure --------------------------------------------------------

  @Test
  fun enum_contains_all_expected_constants_in_correct_order() {
    val names = EventCategory.entries.map { it.name }
    val expected = listOf("CULTURE", "SPORTS", "TECH", "SOCIAL", "ACADEMIC", "CAREER", "OTHER")
    assertEquals(expected, names)
  }

  @Test
  fun ordinals_are_in_increasing_sequence() {
    val ordinals = EventCategory.entries.map { it.ordinal }
    assertEquals(ordinals, ordinals.sorted())
  }

  @Test
  fun toString_returns_enum_name() {
    EventCategory.entries.forEach { c -> assertEquals(c.name, c.toString()) }
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_throws_for_invalid_name() {
    EventCategory.valueOf("NOT_A_REAL_CATEGORY")
  }

  // --- Reversibility & round-trip -------------------------------------------

  @Test
  fun displayString_is_reversible_to_enum() {
    EventCategory.entries.forEach { cat ->
      val reconstructed = EventCategory.valueOf(cat.displayString().uppercase())
      assertEquals(cat, reconstructed)
    }
  }

  @Test
  fun lowercase_and_mixedcase_names_resolve_correctly_via_custom_lookup() {
    fun findByName(name: String): EventCategory? =
        EventCategory.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

    assertEquals(EventCategory.CULTURE, findByName("culture"))
    assertEquals(EventCategory.SPORTS, findByName("Sports"))
    assertNull(findByName("invalid"))
  }

  @Test
  fun enum_can_be_iterated_and_mapped_without_errors() {
    val mapping = EventCategory.entries.associateWith { it.displayString() }
    assertEquals(EventCategory.entries.size, mapping.size)
  }

  // --- Defensive guards for future changes ----------------------------------

  @Test
  fun adding_new_constants_should_break_this_guard() {
    val expected =
        setOf(
            EventCategory.CULTURE,
            EventCategory.SPORTS,
            EventCategory.TECH,
            EventCategory.SOCIAL,
            EventCategory.ACADEMIC,
            EventCategory.CAREER,
            EventCategory.OTHER)
    val actual = EventCategory.entries.toSet()
    assertEquals("If this fails, update tests and UI for new Category entries.", expected, actual)
  }

  // --- Edge / synthetic scenarios -------------------------------------------

  @Test
  fun lowercase_conversion_and_replaceFirstChar_behaviour_consistent() {
    val raw = "EXAMPLE"
    val result = raw.lowercase().replaceFirstChar { it.titlecase() }
    assertEquals("Example", result)
  }

  @Test
  fun all_displayStrings_have_first_character_alpha() {
    EventCategory.entries.forEach {
      val display = it.displayString()
      assertTrue(display.first().isLetter())
    }
  }

  @Test
  fun none_of_the_displayStrings_contain_underscores_or_numbers() {
    EventCategory.entries.forEach {
      val s = it.displayString()
      assertFalse(s.contains("_"))
      assertTrue(s.all { ch -> !ch.isDigit() })
    }
  }
}
