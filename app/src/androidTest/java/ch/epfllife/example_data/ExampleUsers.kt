package ch.epfllife.example_data

import ch.epfllife.model.user.User
import ch.epfllife.model.user.UserSettings

/** A singleton object holding sample user data for testing and previews. */
object ExampleUsers {

  val user1 =
      User(
          id = "user101", name = "Alex"
          // subscriptions null by default
          // userSettings null by default
          )

  val user2 =
      User(
          id = "user202",
          name = "Bella",
          subscriptions = listOf("asso-cs", "asso-belec", "asso-sat")
          // userSettings null by default
          )

  val user3 =
      User(
          id = "user303",
          name = "Charlie",
          subscriptions = listOf("asso-robotics"),
          enrolledEvents = listOf("2"),
          userSettings = UserSettings())

  // A list of all sample users for convenience
  val allUsers = listOf(user1, user2, user3)
}
