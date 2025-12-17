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
import com.google.firebase.storage.FirebaseStorage

data class Db(
    val userRepo: UserRepository,
    val eventRepo: EventRepository,
    val assocRepo: AssociationRepository,
) {
  companion object {
    // https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/lazy.html
    val firestore: Db by lazy {
      Db(
          userRepo = UserRepositoryFirestore(Firebase.firestore, FirebaseStorage.getInstance()),
          eventRepo = EventRepositoryFirestore(Firebase.firestore, FirebaseStorage.getInstance()),
          assocRepo =
              AssociationRepositoryFirestore(Firebase.firestore, FirebaseStorage.getInstance()),
      )
    }

    fun freshLocal(): Db {
      val eventRepo = EventRepositoryLocal()
      return Db(
          userRepo = UserRepositoryLocal(eventRepo),
          eventRepo = eventRepo,
          assocRepo = AssociationRepositoryLocal(eventRepo),
      )
    }

    // we need this because the storage coroutine takes too long(>1min) so we mock this to reduce
    // test flakiness
    // Problem: normally we use FirebaseStorage.getInstance() but this connects to the real storage
    // (for unit tests this is not optimal as are not testing the connection)
    // -> here we just inject a mock storage whenever needed
    fun forTest(storage: FirebaseStorage): Db {
      return Db(
          userRepo = UserRepositoryFirestore(Firebase.firestore, storage),
          eventRepo = EventRepositoryFirestore(Firebase.firestore, storage),
          assocRepo = AssociationRepositoryFirestore(Firebase.firestore, storage),
      )
    }
  }
}
