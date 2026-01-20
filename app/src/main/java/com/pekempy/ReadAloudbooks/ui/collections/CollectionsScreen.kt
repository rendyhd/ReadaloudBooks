package com.pekempy.ReadAloudbooks.ui.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pekempy.ReadAloudbooks.data.local.entities.BookCollection
import com.pekempy.ReadAloudbooks.data.repository.CollectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for Collections Management
 */
class CollectionsViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    val collections: kotlinx.coroutines.flow.Flow<List<BookCollection>> = repository.getAllCollections()

    var showCreateDialog by mutableStateOf(false)
    var editingCollection by mutableStateOf<BookCollection?>(null)

    fun createCollection(name: String, description: String?, colorHex: String?) {
        viewModelScope.launch {
            repository.createCollection(
                BookCollection(
                    name = name,
                    description = description,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateCollection(collection: BookCollection) {
        viewModelScope.launch {
            repository.updateCollection(collection)
        }
    }

    fun deleteCollection(collection: BookCollection) {
        viewModelScope.launch {
            repository.deleteCollection(collection)
        }
    }

    suspend fun getBookCount(collectionId: Long): Int {
        return repository.getBookCountInCollection(collectionId)
    }

    fun toggleBookInCollection(collectionId: Long, bookId: String) {
        viewModelScope.launch {
            if (repository.isBookInCollection(collectionId, bookId)) {
                repository.removeBookFromCollection(collectionId, bookId)
            } else {
                repository.addBookToCollection(collectionId, bookId)
            }
        }
    }
}

/**
 * Collections Management Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    viewModel: CollectionsViewModel,
    onBack: () -> Unit,
    onCollectionClick: (BookCollection) -> Unit
) {
    val collections by viewModel.collections.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog = true }
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(collections) { collection ->
                CollectionCard(
                    collection = collection,
                    onClick = { onCollectionClick(collection) },
                    onEdit = {
                        viewModel.editingCollection = collection
                        viewModel.showCreateDialog = true
                    },
                    onDelete = { viewModel.deleteCollection(collection) }
                )
            }
        }
    }

    // Create/Edit Collection Dialog
    if (viewModel.showCreateDialog) {
        CollectionDialog(
            collection = viewModel.editingCollection,
            onDismiss = {
                viewModel.showCreateDialog = false
                viewModel.editingCollection = null
            },
            onSave = { name, description, color ->
                if (viewModel.editingCollection != null) {
                    viewModel.updateCollection(
                        viewModel.editingCollection!!.copy(
                            name = name,
                            description = description,
                            colorHex = color,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    viewModel.createCollection(name, description, color)
                }
                viewModel.showCreateDialog = false
                viewModel.editingCollection = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionCard(
    collection: BookCollection,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (collection.colorHex != null) {
                            Color(android.graphics.Color.parseColor(collection.colorHex))
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = collection.name.first().uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (collection.colorHex != null) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!collection.description.isNullOrBlank()) {
                    Text(
                        text = collection.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                Text(
                    text = "Created ${dateFormat.format(Date(collection.createdAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Menu button
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Text("⋮", style = MaterialTheme.typography.headlineSmall)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CollectionDialog(
    collection: BookCollection? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, color: String?) -> Unit
) {
    var name by remember { mutableStateOf(collection?.name ?: "") }
    var description by remember { mutableStateOf(collection?.description ?: "") }
    var selectedColor by remember { mutableStateOf(collection?.colorHex) }

    val collectionColors = listOf(
        "#E53935" to "Red",
        "#D81B60" to "Pink",
        "#8E24AA" to "Purple",
        "#5E35B1" to "Deep Purple",
        "#3949AB" to "Indigo",
        "#1E88E5" to "Blue",
        "#00ACC1" to "Cyan",
        "#00897B" to "Teal",
        "#43A047" to "Green",
        "#7CB342" to "Light Green",
        "#FDD835" to "Yellow",
        "#FFB300" to "Amber",
        "#FB8C00" to "Orange",
        "#F4511E" to "Deep Orange"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (collection != null) "Edit Collection" else "New Collection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Collection Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3
                )

                // Color picker
                Text("Color", style = MaterialTheme.typography.labelLarge)
                Column {
                    collectionColors.chunked(7).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { (hex, _) ->
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Color(android.graphics.Color.parseColor(hex)),
                                            CircleShape
                                        )
                                        .clickable { selectedColor = hex }
                                        .then(
                                            if (selectedColor == hex) {
                                                Modifier.padding(2.dp)
                                            } else Modifier
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    name,
                                    description.ifBlank { null },
                                    selectedColor
                                )
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
