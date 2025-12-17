package ch.epfllife.model.event

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EventRepositoryFirestoreTest {

  @MockK private lateinit var mockFirestore: FirebaseFirestore
  @MockK private lateinit var mockStorage: FirebaseStorage
  @MockK private lateinit var mockStorageRef: StorageReference
  @MockK private lateinit var mockImageRef: StorageReference
  @MockK private lateinit var mockUploadTask: UploadTask
  @MockK private lateinit var mockDownloadUrlTask: Task<Uri>
  @MockK private lateinit var mockUri: Uri

  private lateinit var repository: EventRepositoryFirestore

  @Before
  fun setUp() {
    MockKAnnotations.init(this)
    repository = EventRepositoryFirestore(mockFirestore, mockStorage)

    // Mock generic storage behavior
    every { mockStorage.reference } returns mockStorageRef
    every { mockStorageRef.child(any()) } returns mockImageRef
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun uploadEventImage_success_returnsDownloadUrl() = runTest {
    // Arrange
    val eventId = "test-event-id"
    val downloadUrlString = "https://example.com/image.jpg"
    val mockDownloadUri = mockk<Uri>()
    every { mockDownloadUri.toString() } returns downloadUrlString

    // Mock the Kotlin coroutines await extension for Tasks
    mockkStatic("kotlinx.coroutines.tasks.TasksKt")

    // Chain: putFile -> returns UploadTask (which is a Task) -> await()
    every { mockImageRef.putFile(any(), any()) } returns mockUploadTask
    coEvery { mockUploadTask.await() } returns
        mockk() // The result of putFile await is UploadTask.TaskSnapshot

    // Chain: downloadUrl -> returns Task<Uri> -> await() -> returns Uri
    every { mockImageRef.downloadUrl } returns mockDownloadUrlTask
    coEvery { mockDownloadUrlTask.await() } returns mockDownloadUri

    // Act
    val result = repository.uploadEventImage(eventId, mockUri)

    // Assert
    assertTrue(result.isSuccess)
    assertEquals(downloadUrlString, result.getOrNull())

    verify { mockStorageRef.child("events/$eventId/image.jpg") }
    verify { mockImageRef.putFile(mockUri, any()) }
  }

  @Test
  fun uploadEventImage_failure_returnsFailure() = runTest {
    // Arrange
    val eventId = "test-event-id"
    val exception = RuntimeException("Upload failed")

    mockkStatic("kotlinx.coroutines.tasks.TasksKt")

    every { mockImageRef.putFile(any(), any()) } throws exception

    // Act
    val result = repository.uploadEventImage(eventId, mockUri)

    // Assert
    assertTrue(result.isFailure)
    assertEquals(exception, result.exceptionOrNull())
  }
}
