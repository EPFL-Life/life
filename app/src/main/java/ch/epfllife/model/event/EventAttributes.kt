package ch.epfllife.model.event

enum class EventCategory {
  CULTURE,
  SPORTS,
  TECH,
  SOCIAL,
  ACADEMIC,
  CAREER,
  OTHER
}

enum class EventsStatus {
  Subscribed,
  All
}

/**
 * Converts the [EventCategory] enum value to a more readable display string.
 *
 * For example:
 * - CULTURE → "Culture"
 * - SPORTS → "Sports"
 * - TECH → "Tech"
 *
 * @return A user-friendly string representation of the category, formatted with only the first
 *   letter capitalized.
 */
fun EventCategory.displayString(): String = name.lowercase().replaceFirstChar { it.titlecase() }
