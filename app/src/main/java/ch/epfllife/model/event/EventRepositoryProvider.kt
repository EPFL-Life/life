package ch.epfllife.model.event

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object EventRepositoryProvider {
  private val _repository: EventRepository by lazy { EventRepositoryFirestore(Firebase.firestore) }

  var repository: EventRepository = _repository
}
