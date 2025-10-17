package ch.epfllife.model.enums

import org.junit.Assert.*
import org.junit.Test

class CategoryTests {

  // --- Core behaviour --------------------------------------------------------

  @Test
  fun displayString_capitalizes_only_first_letter() {
    assertEquals("Culture", Category.CULTURE.displayString())
    assertEquals("Sports", Category.SPORTS.displayString())
    assertEquals("Tech", Category.TECH.displayString())
    assertEquals("Social", Category.SOCIAL.displayString())
    assertEquals("Academic", Category.ACADEMIC.displayString())
    assertEquals("Career", Category.CAREER.displayString())
    assertEquals("Other", Category.OTHER.displayString())
  }

  @Test
  fun displayString_does_not_overcapitalize_when_already_proper() {
    assertEquals("Tech", Category.TECH.displayString())
  }

  @Test
  fun displayString_outputs_are_unique_and_non_empty() {
    val outputs = Category.entries.map { it.displayString() }
    assertEquals(outputs.size, outputs.distinct().size)
    outputs.forEach { assertTrue(it.isNotEmpty()) }
  }

  @Test
  fun displayString_is_Titlecase_formatted() {
    Category.entries.forEach {
      val display = it.displayString()
      assertTrue(display.first().isUpperCase())
      assertTrue(display.drop(1).all { ch -> ch.isLowerCase() })
    }
  }

  // --- Enum structure --------------------------------------------------------

  @Test
  fun enum_contains_all_expected_constants_in_correct_order() {
    val names = Category.entries.map { it.name }
    val expected = listOf("CULTURE", "SPORTS", "TECH", "SOCIAL", "ACADEMIC", "CAREER", "OTHER")
    assertEquals(expected, names)
  }

  @Test
  fun ordinals_are_in_increasing_sequence() {
    val ordinals = Category.entries.map { it.ordinal }
    assertEquals(ordinals, ordinals.sorted())
  }

  @Test
  fun toString_returns_enum_name() {
    Category.entries.forEach { c -> assertEquals(c.name, c.toString()) }
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_throws_for_invalid_name() {
    Category.valueOf("NOT_A_REAL_CATEGORY")
  }

  // --- Reversibility & round-trip -------------------------------------------

  @Test
  fun displayString_is_reversible_to_enum() {
    Category.entries.forEach { cat ->
      val reconstructed = Category.valueOf(cat.displayString().uppercase())
      assertEquals(cat, reconstructed)
    }
  }

  @Test
  fun lowercase_and_mixedcase_names_resolve_correctly_via_custom_lookup() {
    fun findByName(name: String): Category? =
        Category.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

    assertEquals(Category.CULTURE, findByName("culture"))
    assertEquals(Category.SPORTS, findByName("Sports"))
    assertNull(findByName("invalid"))
  }

  @Test
  fun enum_can_be_iterated_and_mapped_without_errors() {
    val mapping = Category.entries.associateWith { it.displayString() }
    assertEquals(Category.entries.size, mapping.size)
  }

  // --- Defensive guards for future changes ----------------------------------

  @Test
  fun adding_new_constants_should_break_this_guard() {
    val expected =
        setOf(
            Category.CULTURE,
            Category.SPORTS,
            Category.TECH,
            Category.SOCIAL,
            Category.ACADEMIC,
            Category.CAREER,
            Category.OTHER)
    val actual = Category.entries.toSet()
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
    Category.entries.forEach {
      val display = it.displayString()
      assertTrue(display.first().isLetter())
    }
  }

  @Test
  fun none_of_the_displayStrings_contain_underscores_or_numbers() {
    Category.entries.forEach {
      val s = it.displayString()
      assertFalse(s.contains("_"))
      assertTrue(s.all { ch -> !ch.isDigit() })
    }
  }
}
