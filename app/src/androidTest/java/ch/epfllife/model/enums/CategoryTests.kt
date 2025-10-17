package ch.epfllife.model.enums

import org.junit.Assert.*
import org.junit.Test

class CategoryTest {

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
    fun enum_contains_all_expected_categories() {
        val expected = setOf(
            "CULTURE", "SPORTS", "TECH", "SOCIAL", "ACADEMIC", "CAREER", "OTHER"
        )
        val actual = Category.entries.map { it.name }.toSet()
        assertEquals(expected, actual)
    }

    @Test
    fun displayString_outputs_are_unique() {
        val displayStrings = Category.entries.map { it.displayString() }
        assertEquals(displayStrings.size, displayStrings.distinct().size)
    }

    @Test
    fun displayString_format_is_Titlecase() {
        Category.entries.forEach { c ->
            val s = c.displayString()
            assertTrue(s.first().isUpperCase())
            assertTrue(s.drop(1).all { it.isLowerCase() })
        }
    }

    @Test
    fun displayString_is_reversible_via_valueOf_uppercase() {
        Category.entries.forEach { c ->
            val upper = c.displayString().uppercase()
            assertEquals(c, Category.valueOf(upper))
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun valueOf_throws_for_invalid_name() {
        Category.valueOf("INVALID")
    }
}
