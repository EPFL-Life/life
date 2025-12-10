package ch.epfllife.ui.calendar

import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.user.UserRepository
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarGridViewModelTest {

  private lateinit var viewModel: CalendarGridViewModel

  // Using mocks for repositories to ensure isolation
  // @Mock lateinit var mockDb: Db // Cannot mock final class
  @Mock lateinit var mockEventRepo: EventRepository
  @Mock lateinit var mockUserRepo: UserRepository
  @Mock lateinit var mockAssocRepo: ch.epfllife.model.association.AssociationRepository

  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    MockitoAnnotations.openMocks(this)

    // Instantiate Db with mocks
    // mockDb = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun loadEvents_initialState() = runTest {
    // Arrange
    val events = listOf(ExampleEvents.event1, ExampleEvents.event2)
    val user = ExampleUsers.user1
    whenever(mockEventRepo.getAllEvents()).thenReturn(events)
    whenever(mockUserRepo.getCurrentUser()).thenReturn(user)

    val db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
    // Act
    viewModel = CalendarGridViewModel(db)

    // Assert
    assertEquals(events, viewModel.events.first())
    assertEquals(user.enrolledEvents.toSet(), viewModel.enrolledEventIds.first())
  }

  @Test
  fun navigation_nextMonth() = runTest {
    val db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
    viewModel = CalendarGridViewModel(db)
    val initialMonth = viewModel.currentMonth.first()

    viewModel.nextMonth()

    val newMonth = viewModel.currentMonth.value
    assertEquals(initialMonth.plusMonths(1), newMonth)
  }

  @Test
  fun navigation_previousMonth() = runTest {
    val db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
    viewModel = CalendarGridViewModel(db)
    val initialMonth = viewModel.currentMonth.first()

    viewModel.previousMonth()

    val newMonth = viewModel.currentMonth.value
    assertEquals(initialMonth.minusMonths(1), newMonth)
  }

  @Test
  fun selectDate() = runTest {
    val db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
    viewModel = CalendarGridViewModel(db)
    val newDate = LocalDate.of(2023, 10, 10) // Specific date

    viewModel.selectDate(newDate)

    assertEquals(newDate, viewModel.selectedDate.value)
  }

  @Test
  fun getEventsForDate() = runTest {
    // Arrange
    // ExampleEvents.event1 date: 2024-11-20
    // ExampleEvents.event2 date: 2024-11-21
    val event1 = ExampleEvents.event1
    val event2 = ExampleEvents.event2
    val events = listOf(event1, event2)

    whenever(mockEventRepo.getAllEvents()).thenReturn(events)
    whenever(mockUserRepo.getCurrentUser()).thenReturn(null) // No user logged in

    val db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
    viewModel = CalendarGridViewModel(db)

    // Act
    // Create LocalDate matching event1
    // Parse the date string from event1 to be sure
    val event1DateString = event1.time.substring(0, 10)
    val date1 = LocalDate.parse(event1DateString)

    val eventsOnDate1 = viewModel.getEventsForDate(date1)

    // Assert
    assertEquals(1, eventsOnDate1.size)
    assertEquals(event1, eventsOnDate1[0])

    // Check a date with no events
    val noEventDate = date1.plusDays(5)
    assertTrue(viewModel.getEventsForDate(noEventDate).isEmpty())
  }

  @Test
  fun hasEnrolledEventsOnDate() = runTest {
    // Arrange
    val event = ExampleEvents.event1
    val user = ExampleUsers.user1.copy(enrolledEvents = listOf(event.id))

    whenever(mockEventRepo.getAllEvents()).thenReturn(listOf(event))
    whenever(mockUserRepo.getCurrentUser()).thenReturn(user)

    val db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
    viewModel = CalendarGridViewModel(db)

    val eventDateString = event.time.substring(0, 10)
    val date = LocalDate.parse(eventDateString)

    // Act & Assert
    assertTrue(viewModel.hasEnrolledEventsOnDate(date))
    assertFalse(viewModel.hasEnrolledEventsOnDate(date.plusDays(1)))
  }
}
