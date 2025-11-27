package ch.epfllife.ui.association

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
    refresh()
    // TODO: Also load subscribed associations when user data is available
  }

  /** Fetches all associations from the repository and updates the [allAssociations] state. */
  fun refresh(signalFinished: () -> Unit = {}) {
    viewModelScope.launch {
      _allAssociations.value =
          try {
            db.assocRepo.getAllAssociations()
          } catch (e: Exception) {
            // Log the error or handle it as needed
            // e.g., Log.e("AssociationBrowserVM", "Failed to load associations", e)
            emptyList() // Return an empty list on failure
          }
      signalFinished()
    }
  }

  // TODO: Add a function to load subscribed associations
  // This will likely require a UserRepository to know which associations
  // the current user is subscribed to. For now, it remains an empty list,
  // matching the original hardcoded behavior for "Subscribed".
}
