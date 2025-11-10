package ch.epfllife.example_data

import ch.epfllife.model.association.Association
import ch.epfllife.model.event.EventCategory

/** A singleton object holding sample association data for testing purposes. */
object ExampleAssociations {

  val association1 =
      Association(
          id = "asso-sat",
          name = "Satellite",
          description = "A student association focused on space and satellite technology at EPFL.",
          pictureUrl = "https://www.example.com/images/satellite.jpg",
          eventCategory = EventCategory.ACADEMIC)

  val association2 =
      Association(
          id = "asso-belec",
          name = "Balélec",
          description =
              "The organizing committee for the largest student-run music festival in Europe.",
          pictureUrl = "https://www.example.com/images/balelec_logo.png",
          eventCategory = EventCategory.SOCIAL)

  val association3 =
      Association(
          id = "asso-cs",
          name = "AGEPoly",
          description =
              "The General Students' Association of EPFL, representing all students and organizing various events.",
          pictureUrl = null, // Example with no picture
          eventCategory = EventCategory.TECH)

  // A list of all sample associations for convenience
  val allAssociations = listOf(association1, association2, association3)
}
