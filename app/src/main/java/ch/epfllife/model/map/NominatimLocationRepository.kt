package ch.epfllife.model.map

import android.util.Log
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class NominatimLocationRepository(
    private val client: OkHttpClient,
    private val userAgent: String = DEFAULT_USER_AGENT,
    private val referer: String = DEFAULT_REFERER,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocationRepository {

  private fun parseBody(body: String): List<Location> {
    val jsonArray = JSONArray(body)

    return List(jsonArray.length()) { i ->
      val jsonObject = jsonArray.getJSONObject(i)
      val lat = jsonObject.getDouble("lat")
      val lon = jsonObject.getDouble("lon")
      val name = jsonObject.getString("display_name")
      Location(lat, lon, name)
    }
  }

  override suspend fun search(query: String): List<Location> =
      withContext(dispatcher) {
        // Using HttpUrl.Builder to properly construct the URL with query parameters.
        val url =
            HttpUrl.Builder()
                .scheme("https")
                .host("nominatim.openstreetmap.org")
                .addPathSegment("search")
                .addQueryParameter("q", query)
                .addQueryParameter("format", "json")
                .build()

        // Create the request with a custom User-Agent and optional Referer
        val request =
            Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Referer", referer)
                .build()

        try {
          val response = client.newCall(request).execute()
          response.use {
            if (!response.isSuccessful) {
              Log.d("NominatimLocationRepository", "Unexpected code $response")
              throw Exception("Unexpected code $response")
            }

            val body = response.body?.string()
            return@withContext if (body != null) {
              Log.d("NominatimLocationRepository", "Body: $body")
              parseBody(body)
            } else {
              Log.d("NominatimLocationRepository", "Empty body")
              emptyList()
            }
          }
        } catch (e: IOException) {
          Log.e("NominatimLocationRepository", "Failed to execute request", e)
          throw e
        }
      }
}

private const val DEFAULT_USER_AGENT = "EPFLLife/1.0 (contact@epfllife.app)"
private const val DEFAULT_REFERER = "https://epfllife.app"
