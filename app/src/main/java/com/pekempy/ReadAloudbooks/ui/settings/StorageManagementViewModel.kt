package com.pekempy.ReadAloudbooks.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.pekempy.ReadAloudbooks.data.Book
import com.pekempy.ReadAloudbooks.data.api.AppContainer
import java.io.File
import java.util.Date

data class StorageItem(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long
)

data class BookStorageItem(
    val book: Book?,
    val directory: File,
    val items: List<StorageItem>,
    val totalSize: Long,
    val lastModified: Long
)

class StorageManagementViewModel(private val repository: com.pekempy.ReadAloudbooks.data.UserPreferencesRepository) : ViewModel() {
    var bookItems by mutableStateOf<List<BookStorageItem>>(emptyList())
    var isLoading by mutableStateOf(false)
    var selectedBookItem by mutableStateOf<BookStorageItem?>(null)

    enum class SortOption { RecentAsc, RecentDesc, SizeAsc, SizeDesc }
    var currentSort by mutableStateOf(SortOption.RecentDesc)

    fun loadFiles(filesDir: java.io.File) {
        viewModelScope.launch {
            isLoading = true
            
            val allBooks = try {
                val credentials = repository.userCredentials.first()
                if (credentials != null) {
                    val apiManager = AppContainer.apiClientManager
                    apiManager.getApi().listBooks().map { apiBook ->
                        val apiSeries = apiBook.series?.firstOrNull()
                        val apiCollection = apiBook.collections?.firstOrNull()
                        Book(
                            id = apiBook.uuid,
                            title = apiBook.title,
                            author = apiBook.authors.joinToString(", ") { it.name },
                            coverUrl = apiManager.getCoverUrl(apiBook.uuid),
                            series = apiSeries?.name ?: apiCollection?.name,
                            seriesIndex = apiBook.series?.firstNotNullOfOrNull { it.seriesIndex } ?: apiBook.collections?.firstNotNullOfOrNull { it.seriesIndex },
                            addedDate = 0L
                        )
                    }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }

            val itemsByDir = mutableMapOf<java.io.File, MutableList<StorageItem>>()
            
            fun scanDir(dir: File) {
                val files = dir.listFiles() ?: return
                val localItems = mutableListOf<StorageItem>()
                files.forEach { file ->
                    if (file.isDirectory) {
                        scanDir(file)
                    } else if (file.name.endsWith(".epub") || file.name.endsWith(".m4b")) {
                        localItems.add(
                            StorageItem(
                                file = file,
                                name = file.name,
                                sizeBytes = file.length(),
                                lastModified = file.lastModified()
                            )
                        )
                    }
                }
                if (localItems.isNotEmpty()) {
                    itemsByDir[dir] = localItems
                }
            }
            
            scanDir(filesDir)
            
            val finalBookItems = itemsByDir.map { (dir, items) ->
                val matchingBook = allBooks.find { book -> 
                    com.pekempy.ReadAloudbooks.util.DownloadUtils.getBookDir(filesDir, book).absolutePath == dir.absolutePath
                }
                BookStorageItem(
                    book = matchingBook,
                    directory = dir,
                    items = items,
                    totalSize = items.sumOf { it.sizeBytes },
                    lastModified = items.maxOf { it.lastModified }
                )
            }
            
            bookItems = sortItems(finalBookItems, currentSort)
            isLoading = false
        }
    }

    fun setSort(sort: SortOption) {
        currentSort = sort
        bookItems = sortItems(bookItems, sort)
    }

    private fun sortItems(items: List<BookStorageItem>, sort: SortOption): List<BookStorageItem> {
        return when (sort) {
            SortOption.RecentAsc -> items.sortedBy { it.lastModified }
            SortOption.RecentDesc -> items.sortedByDescending { it.lastModified }
            SortOption.SizeAsc -> items.sortedBy { it.totalSize }
            SortOption.SizeDesc -> items.sortedByDescending { it.totalSize }
        }
    }

    fun deleteBook(item: BookStorageItem) {
        viewModelScope.launch {
            item.items.forEach { it.file.delete() }
            var currentDir = item.directory
            while (currentDir.listFiles()?.isEmpty() == true) {
                val parent = currentDir.parentFile
                currentDir.delete()
                if (parent == null) break
                currentDir = parent
            }
            bookItems = bookItems.filter { it.directory != item.directory }
            if (selectedBookItem?.directory == item.directory) {
                selectedBookItem = null
            }
        }
    }

    fun deleteFile(bookItem: BookStorageItem, fileItem: StorageItem) {
        viewModelScope.launch {
            if (fileItem.file.delete()) {
                val newFiles = bookItem.items.filter { it.file != fileItem.file }
                if (newFiles.isEmpty()) {
                    deleteBook(bookItem)
                } else {
                    val updatedBook = bookItem.copy(
                        items = newFiles,
                        totalSize = newFiles.sumOf { f -> f.sizeBytes },
                        lastModified = newFiles.maxOf { f -> f.lastModified }
                    )
                    bookItems = bookItems.map { 
                        if (it.directory == bookItem.directory) updatedBook else it
                    }
                    if (selectedBookItem?.directory == bookItem.directory) {
                        selectedBookItem = updatedBook
                    }
                }
            }
        }
    }
}
