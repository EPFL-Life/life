package ch.epfllife.model.enums

import org.junit.Assert.*
import org.junit.Test

class EventsFilterTest {

  @Test
  fun enum_contains_Subscribed_and_All() {
    val expected = setOf("Subscribed", "All")
    val actual = EventsFilter.entries.map { it.name }.toSet()
    assertEquals(expected, actual)
  }

  @Test
  fun names_match_expected() {
    assertEquals("Subscribed", EventsFilter.Subscribed.name)
    assertEquals("All", EventsFilter.All.name)
  }

  @Test
  fun entries_size_is_two_and_order_is_stable() {
    val entries = EventsFilter.entries
    assertEquals(2, entries.size)
    // Order matters for ordinals and any persisted values
    assertEquals(EventsFilter.Subscribed, entries[0])
    assertEquals(EventsFilter.All, entries[1])
  }

  @Test
  fun ordinals_are_stable() {
    assertEquals(0, EventsFilter.Subscribed.ordinal)
    assertEquals(1, EventsFilter.All.ordinal)
  }

  @Test
  fun toString_defaults_to_name() {
    assertEquals("Subscribed", EventsFilter.Subscribed.toString())
    assertEquals("All", EventsFilter.All.toString())
  }

  @Test
  fun valueOf_roundtrip_matches_name() {
    EventsFilter.entries.forEach { f -> assertEquals(f, EventsFilter.valueOf(f.name)) }
  }

  @Test
  fun valueOf_accepts_uppercase_name() {
    assertEquals(EventsFilter.Subscribed, EventsFilter.valueOf("Subscribed"))
    assertEquals(EventsFilter.All, EventsFilter.valueOf("All"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_throws_for_invalid() {
    EventsFilter.valueOf("NotAFilter")
  }

  @Test
  fun names_are_unique() {
    val names = EventsFilter.entries.map { it.name }
    assertEquals(names.size, names.distinct().size)
  }

  @Test
  fun enum_constants_are_unique_instances() {
    assertNotSame(EventsFilter.Subscribed, EventsFilter.All)
  }

  private fun dbKeyFor(filter: EventsFilter): String =
      when (filter) {
        EventsFilter.Subscribed -> "only_subscribed"
        EventsFilter.All -> "all_events"
      }

  @Test
  fun when_exhaustive_branching_maps_expected_keys() {
    assertEquals("only_subscribed", dbKeyFor(EventsFilter.Subscribed))
    assertEquals("all_events", dbKeyFor(EventsFilter.All))
  }

  // --- Collections and lookup helpers ---

  private fun findByNameOrNull(name: String): EventsFilter? =
      EventsFilter.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

  @Test
  fun findByNameOrNull_finds_case_insensitively() {
    assertEquals(EventsFilter.Subscribed, findByNameOrNull("subscribed"))
    assertEquals(EventsFilter.All, findByNameOrNull("AlL"))
  }

  @Test
  fun findByNameOrNull_returns_null_for_unknown() {
    assertNull(findByNameOrNull("whatever"))
  }

  // --- Defensive checks for future changes ---

  @Test
  fun adding_new_constants_should_break_this_guard() {
    val guard = setOf(EventsFilter.Subscribed, EventsFilter.All)
    val actual = EventsFilter.entries.toSet()
    assertEquals("If this fails, update tests & business rules for new filters.", guard, actual)
  }
}
