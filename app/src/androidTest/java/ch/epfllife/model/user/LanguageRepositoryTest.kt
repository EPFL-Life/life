package ch.epfllife.model.user

import ch.epfllife.example_data.ExampleUsers
import ch.epfllife.model.enums.AppLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LanguageRepositoryTest {

  private class NoUserRepository : UserRepository {
    override suspend fun getCurrentUser(): User? = null

    override suspend fun getAllUsers(): List<User> = emptyList()

    override suspend fun getUser(userId: String): User? = null

    override suspend fun createUser(newUser: User): Result<Unit> = Result.success(Unit)

    override suspend fun updateUser(userId: String, newUser: User): Result<Unit> =
        Result.success(Unit)

    override suspend fun getUsersEnrolledInEvent(eventId: String): List<User> = emptyList()

    override suspend fun deleteUser(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun subscribeToEvent(eventId: String): Result<Unit> = Result.success(Unit)

    override suspend fun unsubscribeFromEvent(eventId: String): Result<Unit> = Result.success(Unit)

    override suspend fun subscribeToAssociation(associationId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun unsubscribeFromAssociation(associationId: String): Result<Unit> =
        Result.success(Unit)
  }

  private class FakeUserRepository(initial: User) : UserRepository {
    var currentUser: User? = initial
    var lastUpdatedUser: User? = null

    override suspend fun getCurrentUser(): User? = currentUser

    override suspend fun getAllUsers(): List<User> = listOfNotNull(currentUser)

    override suspend fun getUser(userId: String): User? = currentUser.takeIf { it?.id == userId }

    override suspend fun createUser(newUser: User): Result<Unit> = Result.success(Unit)

    override suspend fun updateUser(userId: String, newUser: User): Result<Unit> {
      currentUser = newUser
      lastUpdatedUser = newUser
      return Result.success(Unit)
    }

    override suspend fun getUsersEnrolledInEvent(eventId: String): List<User> = emptyList()

    override suspend fun deleteUser(userId: String): Result<Unit> = Result.success(Unit)

    override suspend fun subscribeToEvent(eventId: String): Result<Unit> = Result.success(Unit)

    override suspend fun unsubscribeFromEvent(eventId: String): Result<Unit> = Result.success(Unit)

    override suspend fun subscribeToAssociation(associationId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun unsubscribeFromAssociation(associationId: String): Result<Unit> =
        Result.success(Unit)
  }

  @Test
  fun languageFlowDefaultsToSystemWhenNoUser() = runTest {
    val repo = LanguageRepository(NoUserRepository())
    val emitted = withTimeout(1_000) { repo.languageFlow.first() }
    assertEquals(AppLanguage.SYSTEM, emitted)
  }

  @Test
  fun setLanguagePersistsChangeOnUser() = runTest {
    val initialUser =
        ExampleUsers.user1.copy(userSettings = UserSettings(language = AppLanguage.ENGLISH))
    val fakeRepo = FakeUserRepository(initialUser)
    val repo = LanguageRepository(fakeRepo)

    repo.setLanguage(AppLanguage.FRENCH)

    val updated = fakeRepo.lastUpdatedUser
    assertNotNull(updated)
    assertEquals(AppLanguage.FRENCH, updated!!.userSettings.language)

    val emissions = withTimeout(2_000) { repo.languageFlow.take(1).first() }
    assertEquals(AppLanguage.FRENCH, emissions)
  }
}
