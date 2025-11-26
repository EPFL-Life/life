package ch.epfllife.model.db

import ch.epfllife.model.association.AssociationRepository
import ch.epfllife.model.association.AssociationRepositoryFirestore
import ch.epfllife.model.association.AssociationRepositoryLocal
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.event.EventRepositoryFirestore
import ch.epfllife.model.event.EventRepositoryLocal
import ch.epfllife.model.user.UserRepository
import ch.epfllife.model.user.UserRepositoryFirestore
import ch.epfllife.model.user.UserRepositoryLocal
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

data class Db(
    val userRepo: UserRepository,
    val eventRepo: EventRepository,
    val assocRepo: AssociationRepository,
) {
  companion object {
    val firestore: Db =
        Db(
            userRepo = UserRepositoryFirestore(Firebase.firestore),
            eventRepo = EventRepositoryFirestore(Firebase.firestore),
            assocRepo = AssociationRepositoryFirestore(Firebase.firestore),
        )

    fun freshLocal(): Db {
      val eventRepo = EventRepositoryLocal()
      return Db(
          userRepo = UserRepositoryLocal(),
          eventRepo = eventRepo,
          assocRepo = AssociationRepositoryLocal(eventRepo),
      )
    }
  }
}
