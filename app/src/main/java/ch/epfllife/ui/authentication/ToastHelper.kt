package ch.epfllife.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Because testing for the toast message (popup message) is INSANELY annoying we create this helper
 * class to simplify testing
 */

// 1. The Interface
interface ToastHelper {
  fun show(context: Context, @StringRes messageId: Int)

  fun show(context: Context, message: String)
}

// 2. The Real Implementation (Used in the App)
class SystemToastHelper : ToastHelper {
  override fun show(context: Context, @StringRes messageId: Int) {
    Toast.makeText(context, messageId, Toast.LENGTH_SHORT).show()
  }

  override fun show(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
  }
}

// 3. The Fake Implementation (Used in Tests)
class FakeToastHelper : ToastHelper {
  var lastMessage: String? = null

  override fun show(context: Context, @StringRes messageId: Int) {
    lastMessage = context.getString(messageId)
  }

  override fun show(context: Context, message: String) {
    lastMessage = message
  }
}
