package ch.epfllife.ui.association

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import ch.epfllife.R
import ch.epfllife.example_data.ExampleAssociations
import ch.epfllife.example_data.ExampleEvents
import ch.epfllife.model.association.Association
import ch.epfllife.model.association.AssociationRepository
import ch.epfllife.model.db.Db
import ch.epfllife.model.event.Event
import ch.epfllife.ui.theme.Theme
import ch.epfllife.utils.assertClickable
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class AssociationDetailsScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ---------- Helper functions ----------

  // Helper to set up AssociationDetailsContent inside a Theme.
  private fun setAssociationDetailsContent(
      association: Association,
      onGoBack: () -> Unit = {},
  ) {
    composeTestRule.setContent {
      Theme {
        AssociationDetailsContent(
            association = association,
            onGoBack = onGoBack,
            onEventClick = {},
            events = emptyList(),
        )
      }
    }
  }

  // Helper to set up AssociationDetailsScreen inside a Theme.
  private fun setAssociationDetailsScreen(
      associationId: String,
      db: Db = Db.freshLocal(),
      onGoBack: () -> Unit = {},
      onEventClick: (String) -> Unit = {},
  ) {
    composeTestRule.setContent {
      Theme {
        AssociationDetailsScreen(
            associationId = associationId,
            onGoBack = onGoBack,
            onEventClick = onEventClick,
            db = db,
        )
      }
    }
  }

  private fun dbWithFakeAssoc(assocRepo: AssociationRepository): Db {
    return Db.freshLocal().copy(assocRepo = assocRepo)
  }

  private fun getString(resId: Int, vararg args: Any): String {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return context.getString(resId, *args)
  }

  // ============ AssociationDetailsContent Tests ============

  @Test
  fun contentDisplaysAllMainSectionsCorrectly() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)

    // ---------- Main visible elements ----------
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ABOUT_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN)
        .assertIsDisplayed()

    // ---------- Section labels ----------
    composeTestRule.onNodeWithText(getString(R.string.about_section_title)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getString(R.string.social_pages_title)).assertIsDisplayed()
    composeTestRule.onNodeWithText(getString(R.string.upcoming_events_title)).assertIsDisplayed()

    // ---------- Subscribe button ----------
    // Initially, the subscribe button should be in "Subscribe" state
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assert(hasText(getString(R.string.subscribe_to, association.name)))

    // ---------- Name and description ----------
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT)
        .assert(hasText(association.name))
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT)
        .assert(hasText(association.description))
  }

  // ============ Interaction Tests ============

  @Test
  fun contentBackButtonTriggersCallback() {
    val association = ExampleAssociations.association1
    composeTestRule.assertClickable(
        composable = { onClick ->
          Theme {
            AssociationDetailsContent(
                association = association,
                onGoBack = onClick,
                onEventClick = {},
                events = emptyList(),
            )
          }
        },
        tag = AssociationDetailsTestTags.BACK_BUTTON,
    )
  }

  @Test
  fun contentSubscribeButtonIsClickable() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun contentSubscribeButtonTogglesState() {
    val association = ExampleAssociations.association2
    setAssociationDetailsContent(association)

    // Initially should show subscribe button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()

    // Click subscribe button -> should switch to unsubscribe
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun contentSubscribeButtonTogglesBackAndForth() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)

    // Initially unsubscribed
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()

    // Click to subscribe -> should show unsubscribe button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON).assertIsDisplayed()

    // Click to unsubscribe -> should show subscribe button again
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
  }

  // ============ Integration Tests (Content) ============

  @Test
  fun integrationAllContentDisplayedTogether() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)

    // Verify all major components are present
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.DESCRIPTION_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ABOUT_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.SOCIAL_LINKS_ROW).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN)
        .assertIsDisplayed()
  }

  @Test
  fun integrationMultipleClicksOnSubscribeButton() {
    val association = ExampleAssociations.association2
    setAssociationDetailsContent(association)

    // Click subscribe button multiple times and assert the state toggles each time
    repeat(5) { i ->
      // Locate whichever button is currently visible
      val buttonTag =
          if (i % 2 == 0) AssociationDetailsTestTags.SUBSCRIBE_BUTTON
          else AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON

      composeTestRule.onNodeWithTag(buttonTag).performClick()
      composeTestRule.waitForIdle()

      // Verify that the other tag is now visible
      val expectedTag =
          if ((i + 1) % 2 == 1) AssociationDetailsTestTags.UNSUBSCRIBE_BUTTON
          else AssociationDetailsTestTags.SUBSCRIBE_BUTTON

      composeTestRule.onNodeWithTag(expectedTag).assertIsDisplayed()
    }
  }

  @Test
  fun integrationNavigationBetweenStates() {
    val association = ExampleAssociations.association1
    var goBackCalled = false
    setAssociationDetailsContent(association) { goBackCalled = true }

    // Verify content is displayed
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()

    // Click back button
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).performClick()

    assertTrue("Navigation callback should be triggered", goBackCalled)
  }

  @Test
  fun contentAllButtonsAreAccessible() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)

    // All interactive elements should be present and accessible
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.BACK_BUTTON).assertHasClickAction()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.SUBSCRIBE_BUTTON)
        .assertHasClickAction()
  }

  @Test
  fun contentAboutSectionLabelDisplayed() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)
    composeTestRule.onNodeWithText(getString(R.string.about_section_title)).assertIsDisplayed()
  }

  @Test
  fun contentSocialPagesSectionLabelDisplayed() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)
    composeTestRule.onNodeWithText(getString(R.string.social_pages_title)).assertIsDisplayed()
  }

  @Test
  fun contentUpcomingEventsSectionLabelDisplayed() {
    val association = ExampleAssociations.association1
    setAssociationDetailsContent(association)
    composeTestRule.onNodeWithText(getString(R.string.upcoming_events_title)).assertIsDisplayed()
  }

  @Test
  fun screenDisplaysErrorMessageOnFailure() {
    setAssociationDetailsScreen(
        associationId = "any",
        db = dbWithFakeAssoc(FakeAssociationRepository(throwError = true)),
    )

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun screenDisplaysContentOnSuccessWhenEventsNull() {
    val association = ExampleAssociations.association1

    setAssociationDetailsScreen(
        associationId = association.id,
        db =
            dbWithFakeAssoc(
                FakeAssociationRepository(
                    successAssociation = association,
                    eventsResult = Result.failure(Exception("test")),
                )),
    )

    composeTestRule.waitForIdle()

    // Ensure we're not in loading or error state
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.LOADING_INDICATOR).assertDoesNotExist()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ERROR_MESSAGE).assertDoesNotExist()

    // Content should be displayed even when events are null (handled as empty list)
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.NAME_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
    composeTestRule
        .onNodeWithTag(AssociationDetailsTestTags.UPCOMING_EVENTS_COLUMN)
        .assertIsDisplayed()
  }

  // ============ Edge Case Tests ============

  @Test
  fun contentHandlesNullPictureUrl() {
    val association = ExampleAssociations.association3
    setAssociationDetailsContent(association)
    // Should still display image component
    composeTestRule.onNodeWithTag(AssociationDetailsTestTags.ASSOCIATION_IMAGE).assertExists()
  }

  // ============ ViewModel Tests (AssociationDetailsViewModel) ============

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun viewModelEmitsErrorWhenAssociationNotFound() = runTest {
    val viewModel = AssociationDetailsViewModel(db = Db.freshLocal())
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    viewModel.loadAssociation("non_existing_id", context)

    val state = viewModel.uiState.first { it !is AssociationDetailsUIState.Loading }

    assertTrue(state is AssociationDetailsUIState.Error)
    val errorState = state as AssociationDetailsUIState.Error
    assertEquals("Association not found", errorState.message)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun viewModelEmitsErrorOnException() = runTest {
    val viewModel =
        AssociationDetailsViewModel(
            db = dbWithFakeAssoc(FakeAssociationRepository(throwError = true)))
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    viewModel.loadAssociation("any", context)

    val state = viewModel.uiState.first { it !is AssociationDetailsUIState.Loading }

    assertTrue(state is AssociationDetailsUIState.Error)
    val errorState = state as AssociationDetailsUIState.Error
    assertTrue(errorState.message.startsWith("Failed to load association"))
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun viewModelEmitsSuccessWithAssociationAndEventsList() = runTest {
    val association = ExampleAssociations.association1
    val eventsList = listOf(ExampleEvents.event1)

    val viewModel =
        AssociationDetailsViewModel(
            db =
                dbWithFakeAssoc(
                    FakeAssociationRepository(
                        successAssociation = association,
                        eventsResult = Result.success(eventsList),
                    )))
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initial state should be Loading due to default initialization of _uiState/uiState
    val initialState = viewModel.uiState.value
    assertTrue(initialState is AssociationDetailsUIState.Loading)

    viewModel.loadAssociation(association.id, context)

    val state = viewModel.uiState.first { it !is AssociationDetailsUIState.Loading }

    assertTrue(state is AssociationDetailsUIState.Success)
    val successState = state as AssociationDetailsUIState.Success
    assertEquals(association, successState.association)
    assertEquals(eventsList, successState.events)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun viewModelSuccessWhenEventsResultIsFailureYieldsNullEvents() = runTest {
    val association = ExampleAssociations.association1
    val viewModel =
        AssociationDetailsViewModel(
            db =
                dbWithFakeAssoc(
                    FakeAssociationRepository(
                        successAssociation = association,
                        eventsResult = Result.failure(Exception("test")),
                    )))

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initial state should be Loading
    assertTrue(viewModel.uiState.value is AssociationDetailsUIState.Loading)

    viewModel.loadAssociation(association.id, context)

    val state = viewModel.uiState.first { it !is AssociationDetailsUIState.Loading }

    assertTrue(state is AssociationDetailsUIState.Success)
    val successState = state as AssociationDetailsUIState.Success
    assertEquals(association, successState.association)
    assertEquals(emptyList<Event>(), successState.events)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun viewModelSuccessWhenEventsResultIsEmptyList() = runTest {
    val association = ExampleAssociations.association1
    val emptyEvents: List<Event> = emptyList()
    val viewModel =
        AssociationDetailsViewModel(
            db =
                dbWithFakeAssoc(
                    FakeAssociationRepository(
                        successAssociation = association,
                        eventsResult = Result.success(emptyEvents),
                    )))
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initial state should be Loading
    assertTrue(viewModel.uiState.value is AssociationDetailsUIState.Loading)

    viewModel.loadAssociation(association.id, context)

    val state = viewModel.uiState.first { it !is AssociationDetailsUIState.Loading }

    assertTrue(state is AssociationDetailsUIState.Success)
    val successState = state as AssociationDetailsUIState.Success
    assertEquals(association, successState.association)
    assertEquals(emptyEvents, successState.events)
  }
}

private class FakeAssociationRepository(
    private val successAssociation: Association? = null,
    private val returnNull: Boolean = false,
    private val throwError: Boolean = false,
    private val eventsResult: Result<List<Event>>? = null,
) : AssociationRepository {

  override suspend fun getAllAssociations(): List<Association> {
    throw UnsupportedOperationException("Not used in these tests")
  }

  override suspend fun getAssociation(associationId: String): Association? {
    if (throwError) throw IOException("Test exception")
    if (returnNull) return null
    return successAssociation
  }

  override fun getNewUid(): String {
    throw UnsupportedOperationException("Not used in these tests")
  }

  override suspend fun createAssociation(association: Association): Result<Unit> {
    throw UnsupportedOperationException("Not used in these tests")
  }

  override suspend fun updateAssociation(
      associationId: String,
      newAssociation: Association,
  ): Result<Unit> {
    throw UnsupportedOperationException("Not used in these tests")
  }

  override suspend fun deleteAssociation(associationId: String): Result<Unit> {
    throw UnsupportedOperationException("Not used in these tests")
  }

  override suspend fun getEventsForAssociation(associationId: String): Result<List<Event>> {
    return eventsResult ?: throw UnsupportedOperationException("Not used in these tests.")
  }
}
