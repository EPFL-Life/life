package ch.epfllife.ui.calendar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.association.AssociationRepository
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.EventRepository
import ch.epfllife.model.user.UserRepository
import ch.epfllife.ui.theme.Theme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class CalendarGridScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // @Mock lateinit var mockDb: Db // Cannot mock final class
  @Mock lateinit var mockEventRepo: EventRepository
  @Mock lateinit var mockUserRepo: UserRepository
  @Mock lateinit var mockAssocRepo: AssociationRepository

  private lateinit var viewModel: CalendarGridViewModel
  private lateinit var db: Db

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    // Instantiate Db with mocks
    db = Db(mockUserRepo, mockEventRepo, mockAssocRepo)
  }

  private fun setContent() {
    // Initialize view model with mocks
    // Ensure initial state is loaded
    runBlocking {
      val events = listOf(ExampleEvents.event1, ExampleEvents.event2)
      val user = ExampleUsers.user1
      whenever(mockEventRepo.getAllEvents()).thenReturn(events)
      whenever(mockUserRepo.getCurrentUser()).thenReturn(user)

      viewModel = CalendarGridViewModel(db)
    }

    composeTestRule.setContent {
      Theme { CalendarGridScreen(db = db, viewModel = viewModel, onEventClick = {}) }
    }
  }

  @Test
  fun displayCurrentMonthHeader() {
    // Arrange
    // Current month
    val currentMonth = YearMonth.now()
    val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val year = currentMonth.year
    val expectedHeader = "$monthName $year"

    // Act
    setContent()

    // Assert
    composeTestRule.onNodeWithText(expectedHeader).assertIsDisplayed()
  }

  @Test
  fun displayWeekdays() {
    // Act
    setContent()

    // Assert
    // Check for a few weekdays
    composeTestRule.onNodeWithText("Mon").assertIsDisplayed()
    composeTestRule.onNodeWithText("Fri").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sun").assertIsDisplayed()
  }

  @Test
  fun selectDate() {
    // Arrange
    setContent()
    val today = LocalDate.now().dayOfMonth.toString()

    // Default selected is Today.
    val todayDate = LocalDate.now()
    // Assuming localized date format "MMM dd, yyyy" as per code
    // SimpleDateFormat("MMM dd, yyyy")
    // java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val expectedTitle = "Events for ${todayDate.format(formatter)}"

    composeTestRule.onNodeWithText(expectedTitle).assertIsDisplayed()
  }

  @Test
  fun displayEventsForSelectedDate() {
    // Arrange
    val today = LocalDate.now()
    val eventForToday =
        ExampleEvents.event1.copy(
            id = "today_event", time = "${today}T12:00:00", title = "Today Event")

    runBlocking {
      val events = listOf(eventForToday)
      whenever(mockEventRepo.getAllEvents()).thenReturn(events)
      whenever(mockUserRepo.getCurrentUser()).thenReturn(ExampleUsers.user1)

      viewModel = CalendarGridViewModel(db)
      // Trigger load manually just in case or depend on init
      // The viewModel.loadEvents() is called in init.
    }

    // Act
    composeTestRule.setContent {
      Theme { CalendarGridScreen(db = db, viewModel = viewModel, onEventClick = {}) }
    }
    composeTestRule.waitForIdle()

    // Assert
    composeTestRule.onNodeWithText("Today Event").assertIsDisplayed()
  }

  @Test
  fun displayEmptyState() {
    // Arrange
    runBlocking {
      whenever(mockEventRepo.getAllEvents()).thenReturn(emptyList())
      whenever(mockUserRepo.getCurrentUser()).thenReturn(ExampleUsers.user1)
      viewModel = CalendarGridViewModel(db)
    }

    // Act
    composeTestRule.setContent {
      Theme { CalendarGridScreen(db = db, viewModel = viewModel, onEventClick = {}) }
    }

    // Assert
    // The text is R.string.calendar_no_events_on_day
    // "No events on this day."
    composeTestRule.onNodeWithText("No events on this day.").assertIsDisplayed()
  }
}
