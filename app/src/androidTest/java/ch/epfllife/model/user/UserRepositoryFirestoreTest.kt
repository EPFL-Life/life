package ch.epfllife.model.user

import android.net.Uri
import ch.epfllife.utils.FirestoreLifeTest
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class UserRepositoryFirestoreTest : FirestoreLifeTest() {

  @Test
  fun uploadUserImage_success_returnsDownloadUrl() = runTest {
    // Arrange
    val userId = "testUser"
    val imageUri = Uri.parse("content://foo/bar")
    val downloadUrlStr = "https://example.com/photo.jpg"
    val downloadUrl = Uri.parse(downloadUrlStr)

    val mockStorageRef = mock(StorageReference::class.java)
    val mockImageRef = mock(StorageReference::class.java)
    val mockUploadTask = mock(UploadTask::class.java)
    val mockTaskSnapshot = mock(UploadTask.TaskSnapshot::class.java)

    whenever(mockStorage.reference).thenReturn(mockStorageRef)
    whenever(mockStorageRef.child("users/$userId/profile.jpg")).thenReturn(mockImageRef)

    // Mock putFile behavior for await()
    whenever(mockUploadTask.isComplete).thenReturn(true)
    whenever(mockUploadTask.isSuccessful).thenReturn(true)
    whenever(mockUploadTask.exception).thenReturn(null)
    whenever(mockUploadTask.isCanceled).thenReturn(false)
    whenever(mockUploadTask.result).thenReturn(mockTaskSnapshot)
    whenever(mockImageRef.putFile(any(), any())).thenReturn(mockUploadTask)

    // Mock downloadUrl behavior for await()
    // downloadUrl returns Task<Uri>
    val successfulUrlTask = Tasks.forResult(downloadUrl)
    whenever(mockImageRef.downloadUrl).thenReturn(successfulUrlTask)

    // Act
    val result = db.userRepo.uploadUserImage(userId, imageUri)

    // Assert
    assertTrue("Upload should succeed", result.isSuccess)
    assertEquals(downloadUrlStr, result.getOrNull())
  }
}
