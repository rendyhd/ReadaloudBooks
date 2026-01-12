package com.pekempy.ReadAloudbooks.ui.components

import androidx.compose.ui.res.painterResource
import com.pekempy.ReadAloudbooks.R
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.pekempy.ReadAloudbooks.ui.library.LibraryViewModel

@Composable
fun AppNavigationBar(
    currentRoute: String?,
    onNavigateToLibrary: () -> Unit,
    currentViewMode: LibraryViewModel.ViewMode = LibraryViewModel.ViewMode.Home,
    onViewModeChange: (LibraryViewModel.ViewMode) -> Unit = {},
    hasDownloads: Boolean = false,
    downloadCount: Int = 0
) {
    val isLibraryRoute = currentRoute?.startsWith("library") == true
    
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_shelves), contentDescription = null) },
            label = { Text("Shelf") },
            selected = currentViewMode == LibraryViewModel.ViewMode.Home,
            onClick = {
                onNavigateToLibrary()
                onViewModeChange(LibraryViewModel.ViewMode.Home)
            }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_book), contentDescription = null) },
            label = { Text("Books") },
            selected = currentViewMode == LibraryViewModel.ViewMode.Library,
            onClick = {
                onNavigateToLibrary()
                onViewModeChange(LibraryViewModel.ViewMode.Library)
            }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
            label = { Text("Authors") },
            selected = currentViewMode == LibraryViewModel.ViewMode.Authors,
            onClick = {
                onNavigateToLibrary()
                onViewModeChange(LibraryViewModel.ViewMode.Authors)
            }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.ic_list), contentDescription = null) },
            label = { Text("Series") },
            selected = currentViewMode == LibraryViewModel.ViewMode.Series,
            onClick = {
                onNavigateToLibrary()
                onViewModeChange(LibraryViewModel.ViewMode.Series)
            }
        )
        
        if (hasDownloads) {
            NavigationBarItem(
                icon = { 
                    BadgedBox(
                        badge = { Badge { Text("$downloadCount") } }
                    ) {
                        Icon(painterResource(R.drawable.ic_download), contentDescription = null) 
                    }
                },
                label = { Text("Downloads") },
                selected = currentViewMode == LibraryViewModel.ViewMode.Downloads,
                onClick = {
                    onNavigateToLibrary()
                    onViewModeChange(LibraryViewModel.ViewMode.Downloads)
                }
            )
        }
    }
}
