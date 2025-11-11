package ch.epfllife.model.user

import com.google.firebase.firestore.FirebaseFirestore

class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  override suspend fun getCurrentUser(): User? {
    TODO("Not yet implemented")
  }

  override suspend fun getAllUsers(): List<User> {
    TODO("Not yet implemented")
  }

  override suspend fun getUser(userId: String): User? {
    TODO("Not yet implemented")
  }

  override suspend fun createUser(newUser: User): Result<Unit> {
    TODO("Not yet implemented")
  }

  override suspend fun updateUser(userId: String, newUser: User): Result<Unit> {
    TODO("Not yet implemented")
  }

  override suspend fun deleteUser(userId: String): Result<Unit> {
    TODO("Not yet implemented")
  }

  override suspend fun subscribeToEvent(eventId: String): Result<Unit> {
    TODO("Not yet implemented")
  }

  override suspend fun unsubscribeFromEvent(eventId: String): Result<Unit> {
    TODO("Not yet implemented")
  }
}
