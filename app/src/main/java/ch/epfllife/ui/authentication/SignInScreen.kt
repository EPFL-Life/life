package ch.epfllife.ui.authentication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.epfllife.R
import ch.epfllife.model.authentication.Auth
import ch.epfllife.ui.navigation.NavigationTestTags
import ch.epfllife.utils.SystemToastHelper
import ch.epfllife.utils.ToastHelper

object SignInScreenTestTags {
  const val SIGN_IN_APP_LOGO = "signInAppLogo"
  const val SIGN_IN_TITLE = "signInTitle"
  const val SIGN_IN_BUTTON = "signInButton"
  const val GOOGLE_LOGO = "googleLogo"
}

@Composable
fun SignInScreen(
    auth: Auth,
    authViewModel: SignInViewModel = viewModel { SignInViewModel(auth) },
    onSignedIn: () -> Unit,
    toastHelper: ToastHelper = SystemToastHelper(),
) {

  val context = LocalContext.current
  val uiState by authViewModel.uiState.collectAsState()

  // Show error message if login fails
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      toastHelper.show(context, it)
      authViewModel.clearErrorMsg()
    }
  }

  // Navigate to home screen on successful login
  LaunchedEffect(uiState.user) {
    uiState.user?.let {
      toastHelper.show(context, R.string.signin_success_message)
      onSignedIn()
    }
  }

  // The main container for the screen
  // A surface container using the 'background' color from the theme
  Column(
      modifier = Modifier.fillMaxSize().testTag(NavigationTestTags.SIGN_IN_SCREEN),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
  ) {
    // App Logo Image
    Image(
        painter = painterResource(id = R.drawable.epfl_life_logo), // Ensure this drawable exists
        contentDescription = stringResource(R.string.signin_epfllife_logo_alt_text),
        modifier = Modifier.size(250.dp).testTag(SignInScreenTestTags.SIGN_IN_APP_LOGO),
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Welcome Text
    Text(
        modifier = Modifier.testTag(SignInScreenTestTags.SIGN_IN_TITLE),
        text = stringResource(R.string.signin_welcome),
        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 57.sp, lineHeight = 64.sp),
        fontWeight = FontWeight.Bold,
        // center the text

        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(48.dp))

    // Authenticate With Google Button
    if (uiState.isLoading) {
      CircularProgressIndicator(modifier = Modifier.size(48.dp))
    } else {
      GoogleSignInButton(onSignInClick = { authViewModel.signIn(context) })
    }
  }
}

@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.White), // Button color
      shape = RoundedCornerShape(50), // Circular edges for the button
      border = BorderStroke(1.dp, Color.LightGray),
      modifier =
          Modifier.padding(8.dp)
              .height(48.dp) // Adjust height as needed
              .testTag(SignInScreenTestTags.SIGN_IN_BUTTON),
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
      // Load the Google logo from resources
      Image(
          painter = painterResource(id = R.drawable.google_logo), // Ensure this drawable exists
          contentDescription = stringResource(R.string.signin_google_logo_alt_text),
          modifier =
              Modifier.size(30.dp) // Size of the Google logo
                  .padding(end = 8.dp)
                  .testTag(SignInScreenTestTags.GOOGLE_LOGO),
      )

      // Text for the button
      Text(
          text = stringResource(R.string.signin_with_google),
          color = Color.Gray, // Text color
          fontSize = 16.sp, // Font size
          fontWeight = FontWeight.Medium,
      )
    }
  }
}
