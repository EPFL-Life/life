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
          logoUrl = "https://pbs.twimg.com/profile_images/966365519770275840/MbRv228G_400x400.jpg",
          eventCategory = EventCategory.ACADEMIC,
          about =
              "Satellite is a student association dedicated to space exploration and satellite technology. We organize workshops, conferences, and hands-on projects to inspire students.",
          socialLinks =
              mapOf(
                  "instagram" to "https://www.instagram.com/satellite",
                  "telegram" to "https://t.me/satellite",
                  "website" to "https://satellite.epfl.ch"))

  val association2 =
      Association(
          id = "asso-belec",
          name = "Balélec",
          description =
              "The organizing committee for the largest student-run music festival in Europe.",
          pictureUrl = "https://www.example.com/images/balelec_logo.png",
          logoUrl = "https://www.balelec.ch/uploads/CROCHE_6280b58b96.png",
          eventCategory = EventCategory.SOCIAL,
          about =
              "Balélec is the largest student-run music festival in Europe, held annually at EPFL. We bring world-class artists and create unforgettable experiences for thousands of festival-goers.",
          socialLinks =
              mapOf(
                  "instagram" to "https://www.instagram.com/balelec",
                  "facebook" to "https://www.facebook.com/balelec",
                  "website" to "https://balelec.ch",
                  "telegram" to "https://t.me/balelec"))

  val association3 =
      Association(
          id = "asso-cs",
          name = "AGEPoly",
          description =
              "The General Students' Association of EPFL, representing all students and organizing various events.",
          pictureUrl = null, // Example with no picture
          logoUrl = "https://old.agepoly.ch/wp-content/uploads/2019/05/ico-agepoly-vertical.png",
          eventCategory = EventCategory.TECH,
          about = null, // Example with no about section
          socialLinks = null) // Example with no social links

  val association4 =
      Association(
          id = "asso-esn",
          name = "ESN Lausanne",
          description = "Erasmus Student Network at EPFL.",
          pictureUrl =
              "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png",
          logoUrl = null,
          eventCategory = EventCategory.CULTURE,
          about =
              "The Erasmus Student Network (ESN) Lausanne is a student association that helps exchange students integrate into life at EPFL and Lausanne through social and cultural activities.",
          socialLinks =
              mapOf(
                  "instagram" to "https://www.instagram.com/esnlausanne",
                  "telegram" to "https://t.me/esnlausanne",
                  "whatsapp" to "https://wa.me/41791234567",
                  "linkedin" to "https://www.linkedin.com/company/esnlausanne",
                  "website" to "https://esnlausanne.ch"))

  val sampleAssociation =
      Association(
          name = "AeroPoly",
          id = "AeroPoly",
          description = "Description",
          eventCategory = EventCategory.ACADEMIC)

  // Association with empty social links for testing

  // A list of all sample associations for convenience
  val allAssociations = listOf(association1, association2, association3, association4)
}
