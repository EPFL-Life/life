package ch.epfllife.model.authentication

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.credentials.GetCredentialException
import android.os.Bundle
import android.util.Log
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

sealed class SignInResult {
  data class Success(val user: FirebaseUser) : SignInResult()

  data object Failure : SignInResult()

  data object Cancelled : SignInResult()
}

/** A Firebase implementation of [Auth]. */
class Auth(val credentialManager: CredentialManager, val auth: FirebaseAuth = Firebase.auth) {
  suspend fun signInFromContext(context: Context): SignInResult {
    try {
      val activity =
          context.getActivity()
              ?: run {
                Log.e("Auth", "Context is not an Activity context")
                return SignInResult.Failure
              }
      val credential = getCredential(credentialManager, activity)
      return signInWithCredential(credential)
    } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
      Log.e("Auth", "Sign in cancelled", e)
      return SignInResult.Cancelled
    } catch (e: androidx.credentials.exceptions.GetCredentialException) {
      Log.e("Auth", "Sign in failed with credential exception", e)
      return SignInResult.Failure
    }
  }

  suspend fun signInWithCredential(credential: Credential): SignInResult {
    return if (credential is CustomCredential &&
        credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
      val idToken = extractIdTokenCredential(credential.data).idToken
      val firebaseCred = toFirebaseCredential(idToken)

      // Sign in with Firebase
      try {
        val user =
            this.auth.signInWithCredential(firebaseCred).await().user ?: return SignInResult.Failure
        SignInResult.Success(user)
      } catch (e: FirebaseAuthInvalidUserException) {
        Log.e("Auth", "Invalid user", e)
        SignInResult.Failure
      } catch (e: FirebaseAuthInvalidCredentialsException) {
        Log.e("Auth", "Invalid credentials", e)
        SignInResult.Failure
      } catch (e: FirebaseAuthUserCollisionException) {
        Log.e("Auth", "User collision", e)
        SignInResult.Failure
      } catch (e: CancellationException) {
        Log.e("Auth", "Sign in cancelled", e)
        SignInResult.Failure
      }
    } else {
      error("Cannot handle credentials of type ${credential.type}")
    }
  }

  fun signOut() = this.auth.signOut()
}

private fun extractIdTokenCredential(bundle: Bundle) = GoogleIdTokenCredential.createFrom(bundle)

private fun toFirebaseCredential(idToken: String) = GoogleAuthProvider.getCredential(idToken, null)

/**
 * This will launch a sign-in menu in the given context, if no credentials are present in the
 * `credentialManager`.
 *
 * @throws GetCredentialException If the request fails
 */
private suspend fun getCredential(credentialManager: CredentialManager, context: Context) =
    credentialManager
        .getCredential(context, signInRequest(signInOptions = getSignInOptions(context)))
        .credential

private fun getSignInOptions(context: Context) =
    GetSignInWithGoogleOption.Builder(
            serverClientId = context.getString(ch.epfllife.R.string.default_web_client_id))
        .build()

private fun signInRequest(signInOptions: GetSignInWithGoogleOption) =
    GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()

private fun Context.getActivity(): Activity? {
  var context = this
  while (context is ContextWrapper) {
    if (context is Activity) {
      return context
    }
    context = context.baseContext
  }
  if (context is Activity) {
    return context
  }
  return null
}
