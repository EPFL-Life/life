package ch.epfllife.model.enums

enum class Category {
  CULTURE,
  SPORTS,
  TECH,
  SOCIAL,
  ACADEMIC,
  CAREER,
  OTHER
}

/**
 * Converts the [Category] enum value to a more readable display string.
 *
 * For example:
 * - CULTURE → "Culture"
 * - SPORTS → "Sports"
 * - TECH → "Tech"
 *
 * @return A user-friendly string representation of the category, formatted with only the first
 *   letter capitalized.
 */
fun Category.displayString(): String = name.lowercase().replaceFirstChar { it.titlecase() }
