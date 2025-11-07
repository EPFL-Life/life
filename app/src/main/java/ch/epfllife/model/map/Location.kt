package ch.epfllife.model.map

data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String,
) {
  constructor(
      latitude: Any,
      longitude: Any,
      name: String = ""
  ) : this(parseDouble(latitude, "latitude"), parseDouble(longitude, "longitude"), name)

  companion object {
    private fun parseDouble(value: Any, fieldName: String): Double {
      return when (value) {
        is Double -> value
        is Number -> value.toDouble()
        else -> throw IllegalArgumentException("$fieldName has unsupported type: ${value::class}")
      }
    }
  }
}
