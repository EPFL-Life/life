package ch.epfllife.model.map

interface LocationRepository {
  suspend fun search(query: String): List<Location>
}
