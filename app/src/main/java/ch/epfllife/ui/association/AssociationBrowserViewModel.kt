package ch.epfllife.ui.association

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.association.Association
import ch.epfllife.model.association.AssociationRepository
import ch.epfllife.model.association.AssociationRepositoryFirestore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the AssociationBrowser screen.
 *
 * @param repo The repository to fetch associations from.
 */
class AssociationBrowserViewModel(
    private val repo: AssociationRepository =
        AssociationRepositoryFirestore(FirebaseFirestore.getInstance())
) : ViewModel() {

  private val _allAssociations = MutableStateFlow<List<Association>>(emptyList())
  val allAssociations: StateFlow<List<Association>> = _allAssociations.asStateFlow()

  // TODO: Implement logic to load *only* subscribed associations (e.g., from UserRepository)
  private val _subscribedAssociations = MutableStateFlow<List<Association>>(emptyList())
  val subscribedAssociations: StateFlow<List<Association>> = _subscribedAssociations.asStateFlow()

  init {
    loadAllAssociations()
    // TODO: Also load subscribed associations when user data is available
  }

  /** Fetches all associations from the repository and updates the [allAssociations] state. */
  private fun loadAllAssociations() {
    viewModelScope.launch {
      _allAssociations.value =
          try {
            repo.getAllAssociations()
          } catch (e: Exception) {
            // Log the error or handle it as needed
            // e.g., Log.e("AssociationBrowserVM", "Failed to load associations", e)
            emptyList() // Return an empty list on failure
          }
    }
  }

  // TODO: Add a function to load subscribed associations
  // This will likely require a UserRepository to know which associations
  // the current user is subscribed to. For now, it remains an empty list,
  // matching the original hardcoded behavior for "Subscribed".
}
