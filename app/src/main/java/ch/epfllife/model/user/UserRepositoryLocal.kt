package ch.epfllife.model.user

class UserRepositoryLocal : UserRepository {

  override suspend fun getUser(userId: String): User? {
    TODO("Not yet implemented")
  }

  override suspend fun createUser(user: User) {
    TODO("Not yet implemented")
  }

  override suspend fun updateUser(userId: String, newUser: User) {
    TODO("Not yet implemented")
  }

  override suspend fun getCurrentUser(): User? {
    TODO("Not yet implemented")
  }
}
