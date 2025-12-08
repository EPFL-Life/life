package ch.epfllife.model.firestore

import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// This object holds all collection path constants for Firestore
object FirestoreCollections {
  const val EVENTS = "events"
  const val ASSOCIATIONS = "associations"
  const val USERS = "users"
}

fun <T> createListenAll(
    scope: CoroutineScope,
    collection: CollectionReference,
    parser: suspend (DocumentSnapshot) -> T?,
    onChange: suspend (List<T>) -> Unit,
) {
  collection.addSnapshotListener { snapshot, error ->
    if (error != null) {
      Log.e("FirebaseListener", "Listening on ${collection.id} failed", error)
      return@addSnapshotListener
    }

    scope.launch { snapshot?.mapNotNull { parser(it) }?.let { onChange(it) } }
  }
}
