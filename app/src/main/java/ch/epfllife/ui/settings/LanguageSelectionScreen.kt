package ch.epfllife.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.epfllife.R
import ch.epfllife.model.enums.AppLanguage
import ch.epfllife.model.user.LanguageRepository
import ch.epfllife.ui.composables.BackButton
import ch.epfllife.ui.theme.LifeRed
import kotlinx.coroutines.launch

object LanguageSelectionTags {
  const val SYSTEM = "language-system"
  const val ENGLISH = "language-english"
  const val FRENCH = "language-french"
}

@Composable
fun LanguageSelectionScreen(languageRepository: LanguageRepository, onBack: () -> Unit) {
  val currentLanguage by
      languageRepository.languageFlow.collectAsState(initial = AppLanguage.SYSTEM)
  val scope = rememberCoroutineScope()
  val scrollState = rememberScrollState()

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier =
            Modifier.fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {

          // --- Header ---
          Text(
              text = stringResource(R.string.select_language),
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

          HorizontalDivider()

          // --- Language options ---
          AppLanguage.values().forEach { lang ->
            val isSelected = currentLanguage == lang

            Button(
                onClick = {
                  scope.launch {
                    languageRepository.setLanguage(lang)
                    onBack()
                  }
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .height(50.dp)
                        .testTag(
                            when (lang) {
                              AppLanguage.SYSTEM -> LanguageSelectionTags.SYSTEM
                              AppLanguage.ENGLISH -> LanguageSelectionTags.ENGLISH
                              AppLanguage.FRENCH -> LanguageSelectionTags.FRENCH
                            }),
                shape = RoundedCornerShape(6.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isSelected) LifeRed else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor =
                            if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)) {
                  Text(
                      text =
                          when (lang) {
                            AppLanguage.SYSTEM -> stringResource(R.string.system_default)
                            AppLanguage.ENGLISH -> stringResource(R.string.english)
                            AppLanguage.FRENCH -> stringResource(R.string.french)
                          },
                      style =
                          MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                }
          }
        }

    BackButton(modifier = Modifier.align(Alignment.TopStart), onGoBack = onBack)
  }
}
