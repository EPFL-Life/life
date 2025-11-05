package ch.epfllife.model.authentication

import android.content.Context
import android.os.Bundle
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

/** A Firebase implementation of [Auth]. */
class Auth(val credentialManager: CredentialManager) {
  suspend fun signInFromContext(context: Context): Result<FirebaseUser> {
    val credential = getCredential(credentialManager, context)
    return signInWithCredential(credential)
  }

  suspend fun signInWithCredential(credential: Credential): Result<FirebaseUser> {
    return try {
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val idToken = extractIdTokenCredential(credential.data).idToken
        val firebaseCred = toFirebaseCredential(idToken)

        // Sign in with Firebase
        val user =
            Firebase.auth.signInWithCredential(firebaseCred).await().user
                ?: return Result.failure(
                    IllegalStateException("Login failed : Could not retrieve user information"))
        Result.success(user)
      } else {
        Result.failure(IllegalStateException("Login failed: Credential is not of type Google ID"))
      }
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Login failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }

  fun signOut(): Result<Unit> {
    return try {
      // Firebase sign out
      Firebase.auth.signOut()

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(
          IllegalStateException("Logout failed: ${e.localizedMessage ?: "Unexpected error."}"))
    }
  }
}

private fun extractIdTokenCredential(bundle: Bundle) = GoogleIdTokenCredential.createFrom(bundle)

private fun toFirebaseCredential(idToken: String) = GoogleAuthProvider.getCredential(idToken, null)

private suspend fun getCredential(
    credentialManager: CredentialManager,
    context: Context,
) =
    credentialManager
        .getCredential(context, signInRequest(signInOptions = getSignInOptions(context)))
        .credential

private fun getSignInOptions(context: Context) =
    GetSignInWithGoogleOption.Builder(
            serverClientId = context.getString(ch.epfllife.R.string.default_web_client_id))
        .build()

private fun signInRequest(signInOptions: GetSignInWithGoogleOption) =
    GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()
