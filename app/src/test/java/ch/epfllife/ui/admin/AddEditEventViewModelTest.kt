package ch.epfllife.ui.admin

import ch.epfllife.R
import ch.epfllife.model.db.Db
import ch.epfllife.model.map.Location
import ch.epfllife.model.map.LocationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * These tests were hard to implement and could cause issues in the future. They can be adjusted or
 * even removed if needed.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(val testDispatcher: TestDispatcher = StandardTestDispatcher()) :
    TestWatcher() {
  override fun starting(description: Description) {
    Dispatchers.setMain(testDispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditEventViewModelTest {

  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private val locationRepository = mockk<LocationRepository>(relaxed = true)
  private val db = Db.freshLocal()
  private val associationId = "assoc1"

  // Verifies that updating the location name clears previous resolution data.
  @Test
  fun updateLocationName_clearsResolvedFieldsAndSchedulesLookup() =
      runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val viewModel =
            AddEditEventViewModel(db, associationId, locationRepository = locationRepository)

        // Mock a successful search to potentially set a resolved state (though updateLocationName
        // clears it)
        val location = Location(1.0, 2.0, "Resolved Name")
        coEvery { locationRepository.search("Query") } returns listOf(location)

        // Simulate initial state setup
        viewModel.updateLocationName("Query")
        viewModel.onManualLocationLookup()
        advanceTimeBy(1000) // Wait for search to complete

        // Act: Update location name, which should clear resolved fields
        viewModel.updateLocationName("New Name")

        // Assert
        val state = viewModel.formState.value
        assertEquals("New Name", state.locationName)
        assertNull(state.locationLatitude)
        assertNull(state.locationLongitude)
        assertNull(state.resolvedLocationName)
        assertNull(state.lastResolvedQuery)
        assertNull(state.locationErrorRes)
      }

  // Verifies that a search is triggered after the debounce delay.
  @Test
  fun scheduleLocationLookup_triggersSearchAfterDebounce() =
      runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val viewModel =
            AddEditEventViewModel(db, associationId, locationRepository = locationRepository)
        val query = "Valid Query"
        coEvery { locationRepository.search(query) } returns emptyList()

        // Act
        viewModel.updateLocationName(query)

        // Assert - initially no search
        coVerify(exactly = 0) { locationRepository.search(any()) }

        // Act - advance time past debounce (700ms)
        advanceTimeBy(701)

        // Assert - search triggered
        coVerify(exactly = 1) { locationRepository.search(query) }
      }

  // Verifies that short queries do not trigger a search.
  @Test
  fun scheduleLocationLookup_doesNotTriggerSearchIfQueryTooShort() =
      runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val viewModel =
            AddEditEventViewModel(db, associationId, locationRepository = locationRepository)
        val query = "Hi" // < 3 chars

        // Act
        viewModel.updateLocationName(query)
        advanceTimeBy(1000)

        // Assert
        coVerify(exactly = 0) { locationRepository.search(any()) }
      }

  // Verifies that redundant searches are avoided if the location is already resolved manually.
  @Test
  fun scheduleLocationLookup_doesNotTriggerSearchIfAlreadyResolved() =
      runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val viewModel =
            AddEditEventViewModel(db, associationId, locationRepository = locationRepository)
        val query = "Resolved"
        val location = Location(1.0, 1.0, "Resolved")
        val queries = mutableListOf<String>()
        coEvery { locationRepository.search(capture(queries)) } returns listOf(location)

        // First, get it resolved
        viewModel.updateLocationName(query)
        advanceTimeBy(1000)
        assertEquals(1, queries.size)
        queries.clear()

        // Now update with same name
        viewModel.updateLocationName(query) // schedules search in 700ms
        viewModel.onManualLocationLookup() // immediate search

        // Advance time enough for manual search to complete
        advanceTimeBy(100)

        // Verify manual search ran
        assertEquals("Manual search should have run", 1, queries.size)

        // Now advance past the debounce time
        advanceTimeBy(1000)

        // Verify no additional search ran
        assertEquals("Scheduled search should not have run", 1, queries.size)
      }

  // Verifies that a successful search updates the state with coordinates.
  @Test
  fun performLocationSearch_success_updatesState() =
      runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val viewModel =
            AddEditEventViewModel(db, associationId, locationRepository = locationRepository)
        val query = "Success"
        val location = Location(10.0, 20.0, "Resolved Success")
        coEvery { locationRepository.search(query) } returns listOf(location)

        // Act
        viewModel.updateLocationName(query)
        advanceTimeBy(1000)

        // Assert
        val state = viewModel.formState.value
        assertEquals(10.0, state.locationLatitude)
        assertEquals(20.0, state.locationLongitude)
        assertEquals("Resolved Success", state.resolvedLocationName)
        assertEquals(query, state.lastResolvedQuery)
        assertFalse(state.isLocationSearching)
        assertNull(state.locationErrorRes)
      }

  // Verifies that no match sets the appropriate error message.
  @Test
  fun performLocationSearch_noMatch_setsError() =
      runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val viewModel =
            AddEditEventViewModel(db, associationId, locationRepository = locationRepository)
        val query = "NoMatch"
        coEvery { locationRepository.search(query) } returns emptyList()

        // Act
        viewModel.updateLocationName(query)
        advanceTimeBy(1000)

        // Assert
        val state = viewModel.formState.value
        assertNull(state.locationLatitude)
        assertEquals(R.string.event_location_no_match, state.locationErrorRes)
        assertFalse(state.isLocationSearching)
      }

  // Verifies that repository exceptions set the error message.
  @Test
  fun performLocationSearch_failure_setsError() =
      runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val viewModel =
            AddEditEventViewModel(db, associationId, locationRepository = locationRepository)
        val query = "Error"
        coEvery { locationRepository.search(query) } throws RuntimeException("Network error")

        // Act
        viewModel.updateLocationName(query)
        advanceTimeBy(1000)

        // Assert
        val state = viewModel.formState.value
        assertNull(state.locationLatitude)
        assertEquals(R.string.event_location_lookup_failed, state.locationErrorRes)
        assertFalse(state.isLocationSearching)
      }
}
