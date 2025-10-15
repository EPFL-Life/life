package ch.epfllife.model.association

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * Provides a single instance of the repository in the app. `repository` is mutable for testing
 * purposes.
 */
object AssociationRepositoryProvider {

  private val _repositroy: AssociationRepository by lazy {
    AssociationRepositoryFirestore(Firebase.firestore)
  }

  var repository: AssociationRepository = _repositroy
}
