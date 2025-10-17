package ch.epfllife.model.enums

import org.junit.Assert.*
import org.junit.Test

class EventsFilterTest {

  @Test
  fun enum_contains_Subscribed_and_All() {
    val expected = setOf("Subscribed", "All")
    val actual = SubscriptionFilter.entries.map { it.name }.toSet()
    assertEquals(expected, actual)
  }

  @Test
  fun names_match_expected() {
    assertEquals("Subscribed", SubscriptionFilter.Subscribed.name)
    assertEquals("All", SubscriptionFilter.All.name)
  }

  @Test
  fun entries_size_is_two_and_order_is_stable() {
    val entries = SubscriptionFilter.entries
    assertEquals(2, entries.size)
    // Order matters for ordinals and any persisted values
    assertEquals(SubscriptionFilter.Subscribed, entries[0])
    assertEquals(SubscriptionFilter.All, entries[1])
  }

  @Test
  fun ordinals_are_stable() {
    assertEquals(0, SubscriptionFilter.Subscribed.ordinal)
    assertEquals(1, SubscriptionFilter.All.ordinal)
  }

  @Test
  fun toString_defaults_to_name() {
    assertEquals("Subscribed", SubscriptionFilter.Subscribed.toString())
    assertEquals("All", SubscriptionFilter.All.toString())
  }

  @Test
  fun valueOf_roundtrip_matches_name() {
    SubscriptionFilter.entries.forEach { f -> assertEquals(f, SubscriptionFilter.valueOf(f.name)) }
  }

  @Test
  fun valueOf_accepts_uppercase_name() {
    assertEquals(SubscriptionFilter.Subscribed, SubscriptionFilter.valueOf("Subscribed"))
    assertEquals(SubscriptionFilter.All, SubscriptionFilter.valueOf("All"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_throws_for_invalid() {
    SubscriptionFilter.valueOf("NotAFilter")
  }

  @Test
  fun names_are_unique() {
    val names = SubscriptionFilter.entries.map { it.name }
    assertEquals(names.size, names.distinct().size)
  }

  @Test
  fun enum_constants_are_unique_instances() {
    assertNotSame(SubscriptionFilter.Subscribed, SubscriptionFilter.All)
  }

  private fun dbKeyFor(filter: SubscriptionFilter): String =
      when (filter) {
        SubscriptionFilter.Subscribed -> "only_subscribed"
        SubscriptionFilter.All -> "all_events"
      }

  @Test
  fun when_exhaustive_branching_maps_expected_keys() {
    assertEquals("only_subscribed", dbKeyFor(SubscriptionFilter.Subscribed))
    assertEquals("all_events", dbKeyFor(SubscriptionFilter.All))
  }

  // --- Collections and lookup helpers ---

  private fun findByNameOrNull(name: String): SubscriptionFilter? =
      SubscriptionFilter.entries.firstOrNull { it.name.equals(name, ignoreCase = true) }

  @Test
  fun findByNameOrNull_finds_case_insensitively() {
    assertEquals(SubscriptionFilter.Subscribed, findByNameOrNull("subscribed"))
    assertEquals(SubscriptionFilter.All, findByNameOrNull("AlL"))
  }

  @Test
  fun findByNameOrNull_returns_null_for_unknown() {
    assertNull(findByNameOrNull("whatever"))
  }

  // --- Defensive checks for future changes ---

  @Test
  fun adding_new_constants_should_break_this_guard() {
    val guard = setOf(SubscriptionFilter.Subscribed, SubscriptionFilter.All)
    val actual = SubscriptionFilter.entries.toSet()
    assertEquals("If this fails, update tests & business rules for new filters.", guard, actual)
  }
}
