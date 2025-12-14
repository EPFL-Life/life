package ch.epfllife.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.model.enums.AppLanguage
import ch.epfllife.model.user.LanguageRepository
import kotlinx.coroutines.launch

@Composable
fun LanguageSelectionScreen(languageRepository: LanguageRepository, onBack: () -> Unit) {
  val currentLanguage by
      languageRepository.languageFlow.collectAsState(initial = AppLanguage.SYSTEM)
  val scope = rememberCoroutineScope()

  Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.Start) {
    Text(
        text = stringResource(R.string.select_language),
        style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(24.dp))

    AppLanguage.values().forEach { lang ->
      val isSelected = currentLanguage == lang
      Button(
          onClick = {
            scope.launch {
              languageRepository.setLanguage(lang)
              onBack()
            }
          },
          colors =
              ButtonDefaults.buttonColors(
                  containerColor =
                      if (isSelected) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(
                text =
                    when (lang) {
                      AppLanguage.SYSTEM -> "System Default"
                      AppLanguage.ENGLISH -> "English"
                      AppLanguage.FRENCH -> "Français"
                    },
                color =
                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface)
          }
    }
  }
}
