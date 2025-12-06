package com.example.deadlinedashboard.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.deadlinedashboard.viewmodel.ThemeViewModel

@Composable
fun SettingsScreen(themeViewModel: ThemeViewModel) {
    val currentTheme by themeViewModel.theme.collectAsState()
    val themes = listOf("Default", "Pastel Blue", "Coral Pink", "Mint Green", "Lavender")
    val TAG = "SettingsScreen"

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Select Theme", modifier = Modifier.padding(bottom = 16.dp))
        themes.forEach { theme ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = theme == currentTheme,
                    onClick = { 
                        Log.d(TAG, "Theme selected: $theme")
                        themeViewModel.setTheme(theme) 
                    }
                )
                Text(theme)
            }
        }
    }
}
