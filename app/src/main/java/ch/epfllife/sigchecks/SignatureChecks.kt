package ch.epfllife.sigchecks

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import ch.epfllife.model.todo.displayString
import ch.epfllife.ui.overview.OverviewScreenTestTags
import com.google.firebase.firestore.ktx.firestore
import kotlin.properties.Delegates
import kotlinx.coroutines.runBlocking

// ************************************************************************* //
// ******                                                             ****** //
// ******  THIS FILE SHOULD NOT BE MODIFIED. IT SHOULD BE LOCATED IN  ****** //
// ******  `app/src/main/java/com/github/se/bootcamp/sigchecks`.      ****** //
// ******  DO **NOT** CHANGE ANY SIGNATURE IN THIS FILE               ****** //
// ******                                                             ****** //
// ************************************************************************* //

/**
 * SignatureChecks is a utility class designed for ensuring the consistency and correctness of the
 * app's architecture and data models. It's structured to validate the implementation of the main
 * components used within the Bootcamp's ToDo app. This class is intended for educational purposes,
 * providing a blueprint for students to understand and implement the required components and their
 * interactions within a Jetpack Compose single activity application. You can add more parameters to
 * the classes and methods as long as the following signature checks are correct (e.g. adding an
 * optional parameter).
 */
@SuppressLint("ComposableNaming")
@SuppressWarnings
class SignatureChecks {
  @Composable
  fun checkGreetingScreen() {
    ch.epfllife.ui.GreetingScreen()
  }

  fun checkGreetingScreenTestTags() {
    ch.epfllife.ui.GreetingScreenTestTags.BUTTON
    ch.epfllife.ui.GreetingScreenTestTags.NAME_INPUT
    ch.epfllife.ui.GreetingScreenTestTags.GREETING_MESSAGE
  }

  @Composable
  fun checkOverviewScreen() {
    ch.epfllife.ui.overview.OverviewScreen(
        overviewViewModel = overviewViewModel,
    )

    ch.epfllife.ui.overview.OverviewScreen(
        overviewViewModel,
    )

    ch.epfllife.ui.overview.OverviewScreen()
  }

  fun checkOverviewScreenTestTags() {
    OverviewScreenTestTags.CREATE_TODO_BUTTON
    OverviewScreenTestTags.LOGOUT_BUTTON
    OverviewScreenTestTags.EMPTY_TODO_LIST_MSG
    OverviewScreenTestTags.TODO_LIST

    val todo =
        ch.epfllife.model.todo.ToDo(
            uid = "1",
            name = "title",
            description = "description",
            assigneeName = "assignee",
            dueDate = com.google.firebase.Timestamp.now(),
            location = null,
            status = ch.epfllife.model.todo.ToDoStatus.CREATED,
            ownerId = "ownerId",
        )

    OverviewScreenTestTags.getTestTagForTodoItem(todo)
  }

  @Composable
  fun checkAddToDoScreen() {
    ch.epfllife.ui.overview.AddTodoScreen()
  }

  fun checkAddToDoScreenTestTags() {
    ch.epfllife.ui.overview.AddToDoScreenTestTags.INPUT_TODO_TITLE
    ch.epfllife.ui.overview.AddToDoScreenTestTags.INPUT_TODO_DESCRIPTION
    ch.epfllife.ui.overview.AddToDoScreenTestTags.INPUT_TODO_ASSIGNEE
    ch.epfllife.ui.overview.AddToDoScreenTestTags.INPUT_TODO_DATE
    ch.epfllife.ui.overview.AddToDoScreenTestTags.INPUT_TODO_LOCATION
    ch.epfllife.ui.overview.AddToDoScreenTestTags.TODO_SAVE
    ch.epfllife.ui.overview.AddToDoScreenTestTags.ERROR_MESSAGE
    ch.epfllife.ui.overview.AddToDoScreenTestTags.LOCATION_SUGGESTION
  }

  @Composable
  fun checkEditToDoScreen() {
    ch.epfllife.ui.overview.EditToDoScreen(
        todoUid = todoID,
    )

    ch.epfllife.ui.overview.EditToDoScreen(
        todoID,
    )
  }

  fun checkEditToDoScreenTestTags() {
    ch.epfllife.ui.overview.EditToDoScreenTestTags.INPUT_TODO_TITLE
    ch.epfllife.ui.overview.EditToDoScreenTestTags.INPUT_TODO_DESCRIPTION
    ch.epfllife.ui.overview.EditToDoScreenTestTags.INPUT_TODO_ASSIGNEE
    ch.epfllife.ui.overview.EditToDoScreenTestTags.INPUT_TODO_DATE
    ch.epfllife.ui.overview.EditToDoScreenTestTags.INPUT_TODO_LOCATION
    ch.epfllife.ui.overview.EditToDoScreenTestTags.INPUT_TODO_STATUS
    ch.epfllife.ui.overview.EditToDoScreenTestTags.TODO_SAVE
    ch.epfllife.ui.overview.EditToDoScreenTestTags.TODO_DELETE
    ch.epfllife.ui.overview.EditToDoScreenTestTags.ERROR_MESSAGE
    ch.epfllife.ui.overview.EditToDoScreenTestTags.LOCATION_SUGGESTION
  }

  @Composable
  fun checkMapToDoScreen() {
    ch.epfllife.ui.map.MapScreen()
  }

  fun checkNavigationTestTags() {
    ch.epfllife.ui.navigation.NavigationTestTags.TOP_BAR_TITLE
    ch.epfllife.ui.navigation.NavigationTestTags.BOTTOM_NAVIGATION_MENU
    ch.epfllife.ui.navigation.NavigationTestTags.OVERVIEW_TAB
    ch.epfllife.ui.navigation.NavigationTestTags.MAP_TAB
    ch.epfllife.ui.navigation.NavigationTestTags.GO_BACK_BUTTON
  }

  @Composable
  fun checkSignInScreen() {
    ch.epfllife.ui.authentication.SignInScreen()
  }

  fun checkSignInScreenTestTags() {
    ch.epfllife.ui.authentication.SignInScreenTestTags.APP_LOGO
    ch.epfllife.ui.authentication.SignInScreenTestTags.LOGIN_TITLE
    ch.epfllife.ui.authentication.SignInScreenTestTags.LOGIN_BUTTON
  }

  fun checkToDoDataModel() {
    val location: ch.epfllife.model.map.Location =
        ch.epfllife.model.map.Location(1.0, 1.0, "locationName")

    ch.epfllife.model.map.Location(latitude = 1.0, longitude = 1.0, name = "locationName")

    val todo =
        ch.epfllife.model.todo.ToDo(
            "1",
            "title",
            "description",
            "assignee",
            com.google.firebase.Timestamp.now(),
            location,
            ch.epfllife.model.todo.ToDoStatus.CREATED,
            "ownerId",
        )

    ch.epfllife.model.todo.ToDo(
        uid = "1",
        name = "title",
        description = "description",
        assigneeName = "assignee",
        dueDate = com.google.firebase.Timestamp.now(),
        location = location,
        status = ch.epfllife.model.todo.ToDoStatus.CREATED,
        ownerId = "ownerId",
    )

    todo.uid
    todo.name
    todo.description
    todo.assigneeName
    todo.dueDate
    todo.status
    todo.ownerId

    ch.epfllife.model.todo.ToDoStatus.CREATED
    ch.epfllife.model.todo.ToDoStatus.STARTED
    ch.epfllife.model.todo.ToDoStatus.ENDED
    ch.epfllife.model.todo.ToDoStatus.ARCHIVED

    ch.epfllife.model.todo.ToDoStatus.CREATED.displayString()
  }

  fun checkToDosRepository() {
    val repository: ch.epfllife.model.todo.ToDosRepository =
        object : ch.epfllife.model.todo.ToDosRepository {
          override suspend fun addTodo(toDo: ch.epfllife.model.todo.ToDo) {}

          override suspend fun editTodo(todoID: String, newValue: ch.epfllife.model.todo.ToDo) {}

          override suspend fun deleteTodo(todoID: String) {}

          override fun getNewUid(): String {
            return "newId"
          }

          override suspend fun getAllTodos(): List<ch.epfllife.model.todo.ToDo> {
            return listOf()
          }

          override suspend fun getTodo(todoID: String): ch.epfllife.model.todo.ToDo {
            return ch.epfllife.model.todo.ToDo(
                "1",
                "title",
                "description",
                "assignee",
                com.google.firebase.Timestamp.now(),
                null,
                ch.epfllife.model.todo.ToDoStatus.CREATED,
                "ownerId",
            )
          }
        }

    runBlocking {
      val todo: ch.epfllife.model.todo.ToDo = repository.getTodo("1")
      repository.getAllTodos()
      repository.addTodo(todo)
      repository.addTodo(toDo = todo)
      repository.editTodo("1", todo)
      repository.editTodo(todoID = "1", newValue = todo)
      repository.deleteTodo("1")
      repository.deleteTodo(todoID = "1")
      repository.getNewUid()
    }
    ch.epfllife.model.todo.ToDosRepositoryLocal()
    ch.epfllife.model.todo.ToDosRepositoryFirestore(com.google.firebase.ktx.Firebase.firestore)
    ch.epfllife.model.todo.ToDosRepositoryFirestore(db = com.google.firebase.ktx.Firebase.firestore)
    ch.epfllife.model.todo.TODOS_COLLECTION_PATH
  }

  fun checkProviders() {
    val repository: ch.epfllife.model.todo.ToDosRepository =
        ch.epfllife.model.todo.ToDosRepositoryProvider.repository
    ch.epfllife.model.todo.ToDosRepositoryProvider.repository = repository

    val httpClient = ch.epfllife.HttpClientProvider.client
    ch.epfllife.HttpClientProvider.client = httpClient
  }

  @Composable
  fun checkTheme() {
    ch.epfllife.ui.theme.BootcampTheme {
      // No content needed
    }
  }

  @Composable
  fun checkBootcampApp() {
    ch.epfllife.BootcampApp(context, credentialManager)

    ch.epfllife.BootcampApp(context = context, credentialManager = credentialManager)

    ch.epfllife.MainActivity()
  }

  /* ---------------------------------------------------
  -----------  UI RELATED CLASSED/OBJECTS  ----------
  --------------------------------------------------- */

  // ViewModel for the overview screen
  private val overviewViewModel by Delegates.notNull<ch.epfllife.ui.overview.OverviewViewModel>()

  private val todoID by Delegates.notNull<String>()

  private val context by Delegates.notNull<android.content.Context>()
  private val credentialManager by Delegates.notNull<androidx.credentials.CredentialManager>()
}
