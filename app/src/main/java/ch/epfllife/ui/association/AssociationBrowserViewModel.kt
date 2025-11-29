package ch.epfllife.ui.association

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.association.Association
import ch.epfllife.model.db.Db
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel for the AssociationBrowser screen. */
class AssociationBrowserViewModel(private val db: Db) : ViewModel() {

  private val _allAssociations = MutableStateFlow<List<Association>>(emptyList())
  val allAssociations: StateFlow<List<Association>> = _allAssociations.asStateFlow()

  // TODO: Implement logic to load *only* subscribed associations (e.g., from UserRepository)
  private val _subscribedAssociations = MutableStateFlow<List<Association>>(emptyList())
  val subscribedAssociations: StateFlow<List<Association>> = _subscribedAssociations.asStateFlow()

  init {
    db.assocRepo.listenAll { _allAssociations.value = it }
    // TODO: Also load subscribed associations when user data is available
  }

  /** Fetches all associations from the repository and updates the [allAssociations] state. */
  fun refresh(signalFinished: () -> Unit = {}) {
    viewModelScope.launch {
      try {
        val allAssociations = db.assocRepo.getAllAssociations()
        _allAssociations.value = allAssociations

        val currentUser = db.userRepo.getCurrentUser()
        if (currentUser != null) {
          _subscribedAssociations.value =
              allAssociations.filter { currentUser.subscriptions.contains(it.id) }
        } else {
          _subscribedAssociations.value = emptyList()
        }
      } catch (e: Exception) {
        Log.e("AssociationBrowserViewModel", "Failed to refresh associations", e)
      }

      signalFinished()
    }
  }
}
