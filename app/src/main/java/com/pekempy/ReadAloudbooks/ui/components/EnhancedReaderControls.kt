package com.pekempy.ReadAloudbooks.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pekempy.ReadAloudbooks.data.UserSettings

/**
 * Enhanced Reader Controls with new settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedReaderControlsSheet(
    userSettings: UserSettings,
    onBrightnessChange: (Float) -> Unit,
    onLineSpacingChange: (Float) -> Unit,
    onMarginSizeChange: (Int) -> Unit,
    onTextAlignmentChange: (String) -> Unit,
    onFullscreenToggle: (Boolean) -> Unit,
    onAutoHideToolbarToggle: (Boolean) -> Unit = {},
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                "Reader Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Brightness Control
            ReaderSettingSection(title = "Brightness", value = "${(userSettings.readerBrightness * 100).toInt()}%") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🌑", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = userSettings.readerBrightness,
                        onValueChange = onBrightnessChange,
                        valueRange = 0.1f..1.0f,
                        modifier = Modifier.weight(1f)
                    )
                    Text("☀️", style = MaterialTheme.typography.titleMedium)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Line Spacing Control
            ReaderSettingSection(title = "Line Spacing", value = "${String.format("%.1f", userSettings.readerLineSpacing)}x") {
                Slider(
                    value = userSettings.readerLineSpacing,
                    onValueChange = onLineSpacingChange,
                    valueRange = 1.0f..2.5f,
                    steps = 5
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tight", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Loose", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Margin Size
            val marginLabel = when(userSettings.readerMarginSize) { 0 -> "Compact"; 1 -> "Normal"; else -> "Wide" }
            ReaderSettingSection(title = "Margins", value = marginLabel) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MarginButton(
                        label = "Compact",
                        isSelected = userSettings.readerMarginSize == 0,
                        onClick = { onMarginSizeChange(0) }
                    )
                    MarginButton(
                        label = "Normal",
                        isSelected = userSettings.readerMarginSize == 1,
                        onClick = { onMarginSizeChange(1) }
                    )
                    MarginButton(
                        label = "Wide",
                        isSelected = userSettings.readerMarginSize == 2,
                        onClick = { onMarginSizeChange(2) }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Text Alignment
            val alignLabel = userSettings.readerTextAlignment.replaceFirstChar { it.uppercase() }
            ReaderSettingSection(title = "Text Alignment", value = alignLabel) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AlignmentButton(
                        label = "Left",
                        isSelected = userSettings.readerTextAlignment == "left",
                        onClick = { onTextAlignmentChange("left") }
                    )
                    AlignmentButton(
                        label = "Justify",
                        isSelected = userSettings.readerTextAlignment == "justify",
                        onClick = { onTextAlignmentChange("justify") }
                    )
                    AlignmentButton(
                        label = "Center",
                        isSelected = userSettings.readerTextAlignment == "center",
                        onClick = { onTextAlignmentChange("center") }
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Fullscreen Mode
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Fullscreen Mode",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Hide status and navigation bars",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = userSettings.readerFullscreenMode,
                    onCheckedChange = onFullscreenToggle
                )
            }

            // Auto-hide Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Auto-hide Toolbar",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Hide toolbar after 4 seconds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = userSettings.readerAutoHideToolbar,
                    onCheckedChange = onAutoHideToolbarToggle
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ReaderSettingSection(
    title: String,
    value: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        content()
    }
}

@Composable
fun MarginButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = if (isSelected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.outlinedButtonColors()
        },
        modifier = Modifier.width(100.dp)
    ) {
        Text(label)
    }
}

@Composable
fun AlignmentButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = if (isSelected) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.outlinedButtonColors()
        },
        modifier = Modifier.width(90.dp)
    ) {
        Text(label)
    }
}

/**
 * Bookmark UI Components
 */
@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Text(
            text = if (isBookmarked) "🔖" else "📑",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun BookmarkDialog(
    currentChapter: String,
    onSave: (label: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bookmark") },
        text = {
            Column {
                Text("Add a bookmark at:")
                Text(
                    currentChapter,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(label.ifBlank { null })
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

