package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(AppSettingsState.themeMode) }
    var selectedFont by remember { mutableStateOf(AppSettingsState.fontFamily) }
    var selectedSize by remember { mutableStateOf(AppSettingsState.fontSizeMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Text & Color Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Color Theme
                Text(
                    text = "Select App Color Theme",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.values().forEach { theme ->
                        val label = when (theme) {
                            AppThemeMode.CLASSIC_GREEN -> "Classic Leaf Green (Soft Cream & Green)"
                            AppThemeMode.PARROT_GREEN -> "Vibrant Parrot Green (High Visibility / Pure White & Vivid Green)"
                            AppThemeMode.DARK_MODE -> "Dark Mode (Deep Green Vibe)"
                            AppThemeMode.HIGH_CONTRAST -> "High Contrast (Black & White)"
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTheme = theme }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTheme == theme,
                                onClick = { selectedTheme = theme }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Section 2: Font Style
                Text(
                    text = "Select Font Style",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppFontFamily.values().forEach { font ->
                        val label = when (font) {
                            AppFontFamily.DEFAULT -> "Default (System Font)"
                            AppFontFamily.SAN_SERIF -> "Sans-Serif (Modern / Clean)"
                            AppFontFamily.SERIF -> "Serif (Elegant / Bookish)"
                            AppFontFamily.MONOSPACE -> "Monospace (Tech / Fixed-Width)"
                            AppFontFamily.CURSIVE -> "Cursive (Playful / Calligraphy)"
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFont = font }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFont == font,
                                onClick = { selectedFont = font }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Section 3: Font Size
                Text(
                    text = "Select Text Font Size",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppFontSize.values().forEach { size ->
                        val label = when (size) {
                            AppFontSize.SMALL -> "Small Size"
                            AppFontSize.MEDIUM -> "Medium / Default Size"
                            AppFontSize.LARGE -> "Large Size"
                            AppFontSize.EXTRA_LARGE -> "Extra Large (Extremely Legible!)"
                        }
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSize = size }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    AppSettingsState.save(context, selectedTheme, selectedFont, selectedSize)
                    onDismiss()
                }
            ) {
                Text("Apply & Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
