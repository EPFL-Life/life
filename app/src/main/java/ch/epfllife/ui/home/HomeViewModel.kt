// File: app/src/main/java/ch/epfllife/ui/home/HomeViewModel.kt
package ch.epfllife.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.epfllife.model.event.Event
import ch.epfllife.model.event.EventRepositoryFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** UI state for the EventDetails screen. */
sealed class HomeUIState {
    object Loading : HomeUIState()
    data class Error(val message: String) : HomeUIState()
}

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Loading)
    val uiState: StateFlow<HomeUIState> = _uiState.asStateFlow()

    private val repo = EventRepositoryFirestore(com.google.firebase.firestore.FirebaseFirestore.getInstance())

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    val allEvents: StateFlow<List<Event>> = _allEvents.asStateFlow()

    private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
    val myEvents: StateFlow<List<Event>> = _myEvents.asStateFlow()

    init {
        loadAllEvents()
        // loadMyEvents() // implement if you have a source for subscribed events
    }

    private fun loadAllEvents() {
        viewModelScope.launch {
            _allEvents.value = try {
                repo.getAllEvents()
            } catch (_: Exception) {
                emptyList()
            }
            // Optionally update uiState to Loaded or Error here
        }
    }
}
