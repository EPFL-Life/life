package ch.epfllife.utils

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import ch.epfllife.HttpClientProvider
import ch.epfllife.model.map.Location
import ch.epfllife.model.todo.ToDo
import ch.epfllife.model.todo.ToDoStatus
import ch.epfllife.model.todo.ToDosRepository
import ch.epfllife.model.todo.ToDosRepositoryProvider
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.ui.overview.OverviewScreenTestTags
import ch.epfllife.utils.FakeHttpClient.FakeLocation
import ch.epfllife.utils.FakeHttpClient.locationSuggestions
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert
import org.junit.Before

const val UI_WAIT_TIMEOUT = 5_000L

/** Base class for all EPFL Life tests, providing common setup and utility functions. */
abstract class LifeTest() {

  abstract fun createInitializedRepository(): ToDosRepository

  open fun initializeHTTPClient(): OkHttpClient = FakeHttpClient.getClient()

  val repository: ToDosRepository
    get() = ToDosRepositoryProvider.repository

  val httpClient
    get() = HttpClientProvider.client

  val shouldSignInAnounymously: Boolean = true

  val currentUser: FirebaseUser
    get() {
      return FirebaseEmulator.auth.currentUser!!
    }

  open val todo1 =
      ToDo(
          uid = "0",
          name = "Buy groceries",
          description = "Milk, eggs, bread, and butter",
          assigneeName = "Alice",
          dueDate = Timestamp.Companion.fromDate(2025, Calendar.SEPTEMBER, 1),
          location = Location(46.5191, 6.5668, "Lausanne Coop"),
          status = ToDoStatus.CREATED,
          ownerId = "user",
      )

  open val todo2 =
      ToDo(
          uid = "1",
          name = "Walk the dog",
          description = "Take Fido for a walk in the park",
          assigneeName = "Bob",
          dueDate = Timestamp.Companion.fromDate(2025, Calendar.OCTOBER, 15),
          location = Location(46.5210, 6.5790, "Parc de Mon Repos"),
          status = ToDoStatus.STARTED,
          ownerId = "user",
      )

  open val todo3 =
      ToDo(
          uid = "2",
          name = "Read a book",
          description = "Finish reading 'Clean Code'",
          assigneeName = "Charlie",
          dueDate = Timestamp.Companion.fromDate(2025, Calendar.NOVEMBER, 10),
          location = Location(46.5200, 6.5800, "City Library"),
          status = ToDoStatus.ARCHIVED,
          ownerId = "user",
      )

  @Before
  open fun setUp() {
    ToDosRepositoryProvider.repository = createInitializedRepository()
    HttpClientProvider.client = initializeHTTPClient()
    if (shouldSignInAnounymously) {
      runTest { FirebaseEmulator.auth.signInAnonymously().await() }
    }
  }

  @After
  open fun tearDown() {
    if (FirebaseEmulator.isRunning) {
      FirebaseEmulator.auth.signOut()
      FirebaseEmulator.clearAuthEmulator()
    }
  }

  private fun ComposeTestRule.waitUntilTodoIsDisplayed(todo: ToDo): SemanticsNodeInteraction {
    checkOverviewScreenIsDisplayed()
    waitUntil(UI_WAIT_TIMEOUT) {
      onAllNodesWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todo))
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    return checkTodoItemIsDisplayed(todo)
  }

  fun ComposeTestRule.clickOnTodoItem(todo: ToDo) {
    waitUntilTodoIsDisplayed(todo).performClick()
  }

  fun ComposeTestRule.checkTodoItemIsDisplayed(todo: ToDo): SemanticsNodeInteraction =
      onNodeWithTag(OverviewScreenTestTags.getTestTagForTodoItem(todo)).assertIsDisplayed()

  fun ComposeTestRule.navigateBack() {
    onNodeWithTag(NavigationTestTags.GO_BACK_BUTTON).assertIsDisplayed().performClick()
  }

  fun ComposeTestRule.checkOverviewScreenIsNotDisplayed() {
    onNodeWithTag(OverviewScreenTestTags.TODO_LIST).assertDoesNotExist()
  }

  fun ComposeTestRule.checkOverviewScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertIsDisplayed()
        .assertTextContains("overview", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.onTodoItem(todo: ToDo, matcher: SemanticsMatcher) {
    onNode(
            hasTestTag(OverviewScreenTestTags.getTestTagForTodoItem(todo))
                .and(hasAnyDescendant(matcher)),
            useUnmergedTree = true,
        )
        .assertIsDisplayed()
  }

  fun ComposeTestRule.checkMapScreenIsDisplayed() {
    onNodeWithTag(NavigationTestTags.TOP_BAR_TITLE)
        .assertIsDisplayed()
        .assertTextContains("map", substring = true, ignoreCase = true)
  }

  fun ComposeTestRule.onLocationSuggestion(location: Location): SemanticsNodeInteraction {
    val hasTextLocation = hasText(location.name)
    val containsTextLocation = hasTextLocation.or(hasAnyDescendant(hasTextLocation))
    return onNode(
        hasTestTag("locationSuggestion").and(containsTextLocation),
        useUnmergedTree = true,
    )
  }

  fun ComposeTestRule.assertAllLocationSuggestionsAreDisplayed(fakeLocation: FakeLocation) {
    for (location in fakeLocation.locationSuggestions) {
      onLocationSuggestion(location).assertIsDisplayed()
    }
  }

  fun <A : ComponentActivity> AndroidComposeTestRule<ActivityScenarioRule<A>, A>
      .checkActivityStateOnPressBack(shouldFinish: Boolean) {
    activityRule.scenario.onActivity { activity ->
      activity.onBackPressedDispatcher.onBackPressed()
    }
    waitUntil { activity.isFinishing == shouldFinish }
    assertEquals(shouldFinish, activity.isFinishing)
  }

  fun ToDo.b2Equals(other: ToDo): Boolean =
      name == other.name &&
          description == other.description &&
          assigneeName == other.assigneeName &&
          dueDate.toDateString() == other.dueDate.toDateString() &&
          status == other.status

  fun ToDosRepository.getTodoByName(name: String): ToDo = runBlocking {
    getAllTodos().first { it.name == name }
  }

  companion object {

    const val MAX_LOCATION_SUGGESTIONS_DISPLAYED = 10

    fun Timestamp.toDateString(): String {
      val date = this.toDate()
      val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
      return dateFormat.format(date)
    }

    fun Timestamp.Companion.fromDate(year: Int, month: Int, day: Int): Timestamp {
      val calendar = Calendar.getInstance()
      calendar.set(year, month, day, 0, 0, 0)
      return Timestamp(calendar.time)
    }
  }
}

/**
 * Assert that the composable triggers the given callback when the node with the given tag is
 * clicked.
 *
 * This ensures that the composable is wired correctly and the UI can respond to user interactions.
 */
fun ComposeContentTestRule.assertClickable(
    composable: @Composable ((callback: () -> Unit) -> Unit),
    tag: String,
) {
  var clicked = false
  this.setContent { composable { clicked = true } }
  this.onNodeWithTag(tag).performClick()

  Assert.assertTrue("$tag should be clickable", clicked)
}
