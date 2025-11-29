package ch.epfllife.model.firestore

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot

// This object holds all collection path constants for Firestore
object FirestoreCollections {
  const val EVENTS = "events"
  const val ASSOCIATIONS = "associations"
  const val USERS = "users"
}

fun <T> createListenAll(
    collection: CollectionReference,
    parser: (DocumentSnapshot) -> T?,
    onChange: (List<T>) -> Unit,
) {
  collection.addSnapshotListener { snapshot, error ->
    if (error != null) {
      Log.e("FirebaseListener", "Listening on ${collection.id} failed", error)
      return@addSnapshotListener
    }

    snapshot?.mapNotNull(parser)?.let(onChange)
  }
}
