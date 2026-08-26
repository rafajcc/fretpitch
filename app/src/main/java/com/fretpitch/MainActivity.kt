package com.fretpitch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.fretpitch.presentation.screen.MainScreen
import com.fretpitch.presentation.theme.FretPitchTheme
import com.fretpitch.presentation.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: android.content.Context?) {
        super.attachBaseContext(newBase)
        newBase?.let {
            val language = LocaleHelper.getLanguage(it)
            LocaleHelper.updateLocale(it, language)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentLanguage = remember { mutableStateOf(LocaleHelper.getLanguage(this)) }

            FretPitchTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onLanguageChange = { language ->
                            LocaleHelper.setLanguage(this@MainActivity, language)
                            currentLanguage.value = language
                            LocaleHelper.recreateActivity(this@MainActivity)
                        },
                        currentLanguage = currentLanguage.value
                    )
                }
            }
        }
    }
}
