package ch.epfllife.example_data

import ch.epfllife.model.event.Event
import ch.epfllife.model.map.Location

/**
 * A singleton object holding sample event data for testing purposes. We localize these event to
 * minimize code duplication across the codebase
 */
object ExampleEvents {

  // A sample location to be reused in the events
  val epflCampusLocation = Location(latitude = 46.5186, longitude = 6.5683, name = "EPFL Campus")

  val satelliteLocation = Location(latitude = 46.5222, longitude = 6.5658, name = "BC Building")

  val event1 =
      Event(
          id = "1",
          title = "Satellite Symposium",
          description = "Join us for an exciting symposium on satellite technology.",
          location = satelliteLocation,
          time = "2025-11-20 09:00",
          associationId = "asso-sat",
          tags = setOf("tech", "symposium", "space"),
          price = 25u, // Using UInt
          imageUrl = "https://actu.epfl.ch/image/76257/original/5616x3744.jpg")

  val event2 =
      Event(
          id = "2",
          title = "Balélec Festival",
          description = "Annual music festival at EPFL. Don't miss out!",
          location = epflCampusLocation,
          time = "2026-05-08 18:00",
          associationId = "asso-belec",
          tags = setOf("music", "festival", "party"),
          price = 45u,
          imageUrl =
              "https://www.jambase.com/wp-content/uploads/2023/04/332066973_940895027072259_250410373047721675_n-e1680722695547-1480x832.jpg")

  val event3 =
      Event(
          id = "3",
          title = "Free Pizza & Networking",
          description =
              "Come for the pizza, stay for the networking. Hosted by the CS student association.",
          location =
              Location(latitude = 46.5193, longitude = 6.5656, name = "Rolex Learning Center"),
          time = "2025-12-05 12:30",
          associationId = "asso-cs",
          tags = setOf("food", "networking", "free"),
          price = 0u, // Free event
          imageUrl = null // No image
          )

  // A list of all sample events for convenience
  val allEvents = listOf(event1, event2, event3)
}
