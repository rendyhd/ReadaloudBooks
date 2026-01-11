package com.pekempy.ReadAloudbooks.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.pekempy.ReadAloudbooks.ui.library.LibraryViewModel

@Composable
fun AppNavigationBar(
    currentViewMode: LibraryViewModel.ViewMode,
    onViewModeChange: (LibraryViewModel.ViewMode) -> Unit,
    hasDownloads: Boolean,
    downloadCount: Int
) {
    NavigationBar(
        containerColor = Color.Transparent
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") },
            selected = currentViewMode == LibraryViewModel.ViewMode.Home,
            onClick = { onViewModeChange(LibraryViewModel.ViewMode.Home) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Book, contentDescription = null) },
            label = { Text("Books") },
            selected = currentViewMode == LibraryViewModel.ViewMode.Library,
            onClick = { onViewModeChange(LibraryViewModel.ViewMode.Library) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("Authors") },
            selected = currentViewMode == LibraryViewModel.ViewMode.Authors,
            onClick = { onViewModeChange(LibraryViewModel.ViewMode.Authors) }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
            label = { Text("Series") },
            selected = currentViewMode == LibraryViewModel.ViewMode.Series,
            onClick = { onViewModeChange(LibraryViewModel.ViewMode.Series) }
        )
        
        if (hasDownloads) {
            NavigationBarItem(
                icon = { 
                    BadgedBox(
                        badge = { Badge { Text("$downloadCount") } }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null) 
                    }
                },
                label = { Text("Downloads") },
                selected = currentViewMode == LibraryViewModel.ViewMode.Downloads,
                onClick = { onViewModeChange(LibraryViewModel.ViewMode.Downloads) }
            )
        }
    }
}
