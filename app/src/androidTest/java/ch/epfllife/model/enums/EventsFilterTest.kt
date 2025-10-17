package ch.epfllife.model.enums

import ch.epfllife.model.event.EventsStatus
import org.junit.Assert.*
import org.junit.Test

class EventsStatusTest {

  @Test
  fun enum_contains_Subscribed_and_All() {
    val expected = setOf("Subscribed", "All")
    val actual = EventsStatus.entries.map { it.name }.toSet()
    assertEquals(expected, actual)
  }

  @Test
  fun names_match_expected() {
    assertEquals("Subscribed", EventsStatus.Subscribed.name)
    assertEquals("All", EventsStatus.All.name)
  }

  @Test
  fun entries_size_is_two_and_order_is_stable() {
    val entries = EventsStatus.entries
    assertEquals(2, entries.size)
    // Order matters for ordinals and any persisted values
    assertEquals(EventsStatus.Subscribed, entries[0])
    assertEquals(EventsStatus.All, entries[1])
  }

  @Test
  fun ordinals_are_stable() {
    assertEquals(0, EventsStatus.Subscribed.ordinal)
    assertEquals(1, EventsStatus.All.ordinal)
  }

  @Test
  fun toString_defaults_to_name() {
    assertEquals("Subscribed", EventsStatus.Subscribed.toString())
    assertEquals("All", EventsStatus.All.toString())
  }

  @Test
  fun valueOf_roundtrip_matches_name() {
    EventsStatus.entries.forEach { f -> assertEquals(f, EventsStatus.valueOf(f.name)) }
  }

  @Test
  fun valueOf_accepts_uppercase_name() {
    assertEquals(EventsStatus.Subscribed, EventsStatus.valueOf("Subscribed"))
    assertEquals(EventsStatus.All, EventsStatus.valueOf("All"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_throws_for_invalid() {
    EventsStatus.valueOf("NotAFilter")
  }

  @Test
  fun names_are_unique() {
    val names = EventsStatus.entries.map { it.name }
    assertEquals(names.size, names.distinct().size)
  }

  @Test
  fun enum_constants_are_unique_instances() {
    assertNotSame(EventsStatus.Subscribed, EventsStatus.All)
  }

  private fun dbKeyFor(filter: EventsStatus): String =
      when (filter) {
        EventsStatus.Subscribed -> "only_subscribed"
        EventsStatus.All -> "all_events"
      }

  @Test
  fun when_exhaustive_branching_maps_expected_keys() {
    assertEquals("only_subscribed", dbKeyFor(EventsStatus.Subscribed))
    assertEquals("all_events", dbKeyFor(EventsStatus.All))
  }

  // --- Collections and lookup helpers ---

  private fun findByNameOrNull(name: String): EventsStatus? =
      EventsStatus.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

  @Test
  fun findByNameOrNull_finds_case_insensitively() {
    assertEquals(EventsStatus.Subscribed, findByNameOrNull("subscribed"))
    assertEquals(EventsStatus.All, findByNameOrNull("AlL"))
  }

  @Test
  fun findByNameOrNull_returns_null_for_unknown() {
    assertNull(findByNameOrNull("whatever"))
  }

  // --- Defensive checks for future changes ---

  @Test
  fun adding_new_constants_should_break_this_guard() {
    val guard = setOf(EventsStatus.Subscribed, EventsStatus.All)
    val actual = EventsStatus.entries.toSet()
    assertEquals("If this fails, update tests & business rules for new filters.", guard, actual)
  }
}
