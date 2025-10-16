package ch.epfllife.model.map

fun interface LocationRepository {
  suspend fun search(query: String): List<Location>
}
