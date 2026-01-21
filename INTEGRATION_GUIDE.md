# Integration Guide for All 20 Features

This guide provides step-by-step instructions for integrating all implemented features into the ReadAloud Books app.

## Prerequisites

All database entities, DAOs, repositories, UI components, and utilities have been created. The infrastructure is complete and ready for integration.

## Phase 1: Highlights, Annotations & Bookmarks (Features 1-5)

### 1.1 & 1.3: User-Created Highlights with Notes

**Integration Steps:**

1. **Update ReaderViewModel** to include HighlightRepository:
```kotlin
class ReaderViewModel(
    private val repository: UserPreferencesRepository,
    private val highlightRepository: HighlightRepository, // ADD THIS
    private val bookmarkRepository: BookmarkRepository // ADD THIS
) : ViewModel()
```

2. **Add JavaScript handlers** in ReaderScreen.kt `wrapHtml` function:
```javascript
// Add to JavaScript interface
Android.onTextSelected = function(text, elementId, startOffset, endOffset) {
    // Show highlight dialog
};
```

3. **Add highlight menu** to ReaderScreen:
```kotlin
var showHighlightDialog by remember { mutableStateOf(false) }
if (showHighlightDialog) {
    HighlightDialog(
        selectedText = selectedText,
        onSave = { color, note ->
            // Save highlight via repository
        },
        onDismiss = { showHighlightDialog = false }
    )
}
```

4. **Display user highlights** in WebView by modifying CSS to show user highlights alongside sync highlights.

### 1.2: Export Highlights to Markdown

**Integration Steps:**

1. **Add export button** to reader top bar:
```kotlin
IconButton(onClick = { showExportMenu = true }) {
    Icon("📤")
}
```

2. **Create export menu**:
```kotlin
DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
    DropdownMenuItem(
        text = { Text("Export Highlights (Markdown)") },
        onClick = {
            scope.launch {
                highlightExporter.saveAndShareMarkdown(context, book, highlights, chapterTitles)
            }
        }
    )
    DropdownMenuItem(
        text = { Text("Export Highlights (CSV)") },
        onClick = {
            scope.launch {
                highlightExporter.saveAndShareCsv(context, book, highlights, chapterTitles)
            }
        }
    )
}
```

3. **Add FileProvider** to AndroidManifest.xml:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

4. **Create file_paths.xml** in `res/xml/`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="cache" path="." />
</paths>
```

### 1.4: Bookmarks System

**Integration Steps:**

1. **Add bookmark button** to reader top bar:
```kotlin
BookmarkButton(
    isBookmarked = isCurrentPositionBookmarked,
    onClick = { showBookmarkDialog = true }
)
```

2. **Add bookmark dialog**:
```kotlin
if (showBookmarkDialog) {
    BookmarkDialog(
        currentChapter = "Chapter ${currentChapter + 1}",
        onSave = { label ->
            viewModel.addBookmark(
                chapterIndex = currentChapter,
                scrollPercent = currentScrollPercent,
                elementId = currentElementId,
                label = label
            )
        },
        onDismiss = { showBookmarkDialog = false }
    )
}
```

3. **Show bookmarks list** in chapter menu:
```kotlin
LazyColumn {
    item { Text("Bookmarks") }
    items(bookmarks) { bookmark ->
        // Bookmark list item
    }
}
```

### 1.5: Text Selection & Copy

**Integration Steps:**

1. **Remove user-select: none** from WebView CSS in `wrapHtml` function:
```css
/* Remove or comment out these lines */
/* -webkit-user-select: none; */
/* user-select: none; */
```

2. **Add text selection handler** in JavaScript:
```javascript
document.addEventListener('selectionchange', function() {
    const selection = window.getSelection();
    if (selection.toString().length > 0) {
        const text = selection.toString();
        const range = selection.getRangeAt(0);
        const element = range.commonAncestorContainer.parentElement;
        if (element && element.id) {
            window.Android.onTextSelected(text, element.id, range.startOffset, range.endOffset);
        }
    }
});
```

3. **Add context menu** for selected text:
```kotlin
if (showTextContextMenu) {
    WordContextMenu(
        word = selectedText,
        onDefine = { showDictionaryDialog = true },
        onHighlight = { showHighlightDialog = true },
        onCopy = {
            clipboardManager.setText(AnnotatedString(selectedText))
        },
        onSearchInBook = { /* Search functionality */ },
        onDismiss = { showTextContextMenu = false }
    )
}
```

## Phase 2: Reader Experience (Features 6-10)

### 2.1: Dictionary Integration

**Integration Steps:**

1. **Show dictionary dialog** when user selects "Define":
```kotlin
if (showDictionaryDialog) {
    DictionaryDialog(
        word = selectedWord,
        onDismiss = { showDictionaryDialog = false },
        onSearchInBook = {
            viewModel.searchInBook(selectedWord)
        }
    )
}
```

2. **Add INTERNET permission** to AndroidManifest.xml:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 2.2: Reading Statistics

**Integration Steps:**

1. **Initialize database** in MainActivity:
```kotlin
val database = AppDatabase.getDatabase(applicationContext)
val statsRepository = ReadingStatisticsRepository(
    database.readingSessionDao(),
    database.bookMetadataDao()
)
```

2. **Track reading sessions** in ReaderViewModel:
```kotlin
private var sessionStartTime: Long? = null

fun onReaderVisible() {
    sessionStartTime = System.currentTimeMillis()
}

fun onReaderHidden() {
    sessionStartTime?.let { startTime ->
        viewModelScope.launch {
            readingSessionDao.insertSession(
                ReadingSession(
                    bookId = currentBookId,
                    bookTitle = bookTitle,
                    startTime = startTime,
                    endTime = System.currentTimeMillis(),
                    durationMillis = System.currentTimeMillis() - startTime,
                    pagesRead = estimatePagesRead(),
                    chapterIndex = currentChapterIndex,
                    isAudio = false
                )
            )
        }
    }
    sessionStartTime = null
}
```

3. **Add statistics screen** to navigation:
```kotlin
composable("statistics") {
    ReadingStatisticsScreen(
        viewModel = ReadingStatisticsViewModel(statsRepository),
        onBack = { navController.popBackStack() }
    )
}
```

4. **Add button** in SettingsScreen:
```kotlin
SettingsItem(
    title = "Reading Statistics",
    icon = "📊",
    onClick = { navController.navigate("statistics") }
)
```

### 2.3: Brightness Controls

**Integration Steps:**

1. **Add brightness control** to enhanced reader controls:
```kotlin
EnhancedReaderControlsSheet(
    userSettings = userSettings,
    onBrightnessChange = { brightness ->
        repository.updateReaderBrightness(brightness)
        // Update window brightness
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness
        window.attributes = layoutParams
    },
    ...
)
```

2. **Apply brightness** when reader loads:
```kotlin
LaunchedEffect(userSettings.readerBrightness) {
    val layoutParams = window.attributes
    layoutParams.screenBrightness = userSettings.readerBrightness
    window.attributes = layoutParams
}
```

### 2.4: Line Spacing & Margin Controls

**Integration Steps:**

1. **Add controls** to EnhancedReaderControlsSheet (already implemented).

2. **Apply settings** in `wrapHtml` CSS:
```css
body {
    line-height: ${userSettings.readerLineSpacing} !important;
    padding-left: ${getMarginPadding(userSettings.readerMarginSize)};
    padding-right: ${getMarginPadding(userSettings.readerMarginSize)};
    text-align: ${userSettings.readerTextAlignment};
}
```

3. **Add helper function**:
```kotlin
fun getMarginPadding(size: Int): String {
    return when (size) {
        0 -> "8px"  // Compact
        1 -> "16px" // Normal
        2 -> "24px" // Wide
        else -> "16px"
    }
}
```

### 2.5: Immersive Fullscreen Mode

**Integration Steps:**

1. **Toggle fullscreen** based on setting:
```kotlin
LaunchedEffect(userSettings.readerFullscreenMode) {
    if (userSettings.readerFullscreenMode) {
        // Enable fullscreen
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        // Disable fullscreen
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}
```

## Phase 3: Library & Organization (Features 11-15)

### 3.1: Advanced Search with Filters

**Integration Steps:**

1. **Add search bar** to LibraryScreen with filter button:
```kotlin
Row {
    SearchBar(
        query = searchQuery,
        onQueryChange = { viewModel.updateSearch(it) }
    )
    IconButton(onClick = { showFilterSheet = true }) {
        Icon("🔍")
    }
}
```

2. **Create filter sheet**:
```kotlin
if (showFilterSheet) {
    ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
        Column {
            FilterChip(label = "Audiobooks", selected = showAudiobooks, onClick = {})
            FilterChip(label = "eBooks", selected = showEbooks, onClick = {})
            FilterChip(label = "Downloaded", selected = showDownloaded, onClick = {})
            // More filters...
        }
    }
}
```

### 3.2: Custom Collections

**Integration Steps:**

1. **Add Collections tab** to HomeScreen:
```kotlin
Tab(
    selected = selectedTab == 5,
    onClick = { selectedTab = 5 },
    text = { Text("Collections") }
)
```

2. **Show collections screen** when tab selected:
```kotlin
if (selectedTab == 5) {
    CollectionsScreen(
        viewModel = CollectionsViewModel(collectionRepository),
        onBack = { },
        onCollectionClick = { collection ->
            navController.navigate("collection/${collection.id}")
        }
    )
}
```

3. **Add "Add to Collection"** to book detail menu:
```kotlin
DropdownMenuItem(
    text = { Text("Add to Collection") },
    onClick = { showCollectionPicker = true }
)
```

### 3.3: Reading Status Tracking

**Integration Steps:**

1. **Add reading status** to BookDetailScreen:
```kotlin
ReadingStatusSelector(
    currentStatus = bookMetadata?.readingStatus?.name ?: "NONE",
    onStatusChange = { status ->
        viewModel.updateReadingStatus(bookId, ReadingStatus.valueOf(status))
    }
)
```

2. **Add rating stars**:
```kotlin
RatingStars(
    rating = bookMetadata?.rating,
    onRatingChange = { rating ->
        viewModel.setBookRating(bookId, rating)
    }
)
```

3. **Track date started/finished automatically**:
```kotlin
// In ReaderViewModel when opening book
if (metadata.readingStatus == ReadingStatus.WANT_TO_READ) {
    metadataRepository.updateReadingStatus(bookId, ReadingStatus.READING)
}
```

### 3.4: Enhanced Library Views

**Integration Steps:**

1. **Add view mode selector** to LibraryScreen:
```kotlin
LibraryViewModeSelector(
    currentMode = userSettings.libraryViewMode,
    onModeChange = { mode ->
        repository.updateLibraryViewMode(mode)
    }
)
```

2. **Render different layouts** based on mode:
```kotlin
when (userSettings.libraryViewMode) {
    "grid" -> GridBookLayout(books, columns = userSettings.libraryGridColumns)
    "list" -> ListBookLayout(books)
    "compact" -> CompactGridBookLayout(books, columns = 3)
    "table" -> TableBookLayout(books)
}
```

### 3.5: Narrator Browse View

**Integration Steps:**

1. **Add Narrator tab** to HomeScreen (similar to Authors/Series):
```kotlin
Tab(
    selected = selectedTab == 6,
    onClick = { selectedTab = 6 },
    text = { Text("Narrators") }
)
```

2. **Group books by narrator**:
```kotlin
val booksByNarrator = books.groupBy { it.narrator ?: "Unknown" }
LazyColumn {
    booksByNarrator.forEach { (narrator, narratorBooks) ->
        item {
            NarratorSection(
                narrator = narrator,
                books = narratorBooks,
                onBookClick = onBookClick
            )
        }
    }
}
```

## Phase 4: Audio & Smart Features (Features 16-20)

### 4.1: Advanced Playback Controls

**Integration Steps:**

1. **Add settings button** to AudiobookPlayer:
```kotlin
IconButton(onClick = { showAudioSettings = true }) {
    Icon("⚙️")
}
```

2. **Show advanced audio controls**:
```kotlin
if (showAudioSettings) {
    AdvancedAudioControlsSheet(
        userSettings = userSettings,
        onSkipBackSecondsChange = { repository.updateSkipBackSeconds(it) },
        onSkipForwardSecondsChange = { repository.updateSkipForwardSeconds(it) },
        onVolumeBoostChange = { enabled, level ->
            repository.updateVolumeBoost(enabled, level)
        },
        onDismiss = { showAudioSettings = false }
    )
}
```

3. **Apply skip intervals**:
```kotlin
IconButton(onClick = {
    viewModel.skipBackward(userSettings.skipBackSeconds * 1000L)
}) {
    Text("↶ ${userSettings.skipBackSeconds}s")
}
```

4. **Apply volume boost** in ExoPlayer:
```kotlin
if (userSettings.enableVolumeBoost) {
    player.volume = userSettings.volumeBoostLevel
}
```

### 4.2: Audio Bookmarks

**Integration Steps:**

1. **Add bookmark button** to player:
```kotlin
AudioBookmarkButton(
    onClick = { showAudioBookmarkDialog = true }
)
```

2. **Show bookmark dialog**:
```kotlin
if (showAudioBookmarkDialog) {
    AudioBookmarkDialog(
        currentTimestamp = formatTimestamp(currentPosition),
        onSave = { label, note ->
            viewModel.addAudioBookmark(
                timestampMillis = currentPosition,
                chapterIndex = currentChapterIndex,
                label = label,
                note = note
            )
        },
        onDismiss = { showAudioBookmarkDialog = false }
    )
}
```

3. **Show bookmarks list** in player sheet:
```kotlin
LazyColumn {
    items(audioBookmarks) { bookmark ->
        AudioBookmarkItem(
            bookmark = bookmark,
            onClick = { viewModel.seekTo(bookmark.timestampMillis) }
        )
    }
}
```

### 4.3: Enhanced Sleep Timer

**Integration Steps:**

1. **Replace existing sleep timer** with enhanced version:
```kotlin
EnhancedSleepTimerDialog(
    currentMinutes = sleepTimerMinutes,
    finishChapter = finishChapter,
    onSet = { minutes, finishChapter ->
        viewModel.setSleepTimer(minutes, finishChapter)
    },
    onDismiss = { showSleepTimerDialog = false }
)
```

2. **Add shake detection** when timer is active:
```kotlin
val shakeDetector = remember {
    ShakeDetector(context) {
        // Extend timer by 5 minutes
        viewModel.extendSleepTimer(5)
        Toast.makeText(context, "Sleep timer extended by 5 minutes", Toast.LENGTH_SHORT).show()
    }
}

LaunchedEffect(sleepTimerActive) {
    if (sleepTimerActive) {
        shakeDetector.start()
    } else {
        shakeDetector.stop()
    }
}
```

3. **Implement fade-out**:
```kotlin
// In AudiobookViewModel
private fun startFadeOut() {
    viewModelScope.launch {
        val fadeSteps = 30
        val fadeInterval = 1000L // 30 seconds total
        repeat(fadeSteps) { step ->
            val volume = 1.0f - (step.toFloat() / fadeSteps)
            player.volume = volume
            delay(fadeInterval)
        }
        player.pause()
        player.volume = 1.0f
    }
}
```

### 4.4: Reading Recommendations

**Integration Steps:**

1. **Create recommendations generator**:
```kotlin
class RecommendationsGenerator(
    private val bookMetadataRepository: BookMetadataRepository
) {
    suspend fun getRecommendations(userId: String): List<String> {
        // Get user's reading history
        val metadata = bookMetadataRepository.getAllBookMetadata().first()

        // Get favorite authors
        val favoriteAuthors = metadata
            .filter { it.rating != null && it.rating >= 4 }
            .map { /* Get author from book */ }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }

        // Return book IDs from favorite authors
        return emptyList() // Implement with API call
    }
}
```

2. **Show recommendations** in HomeScreen:
```kotlin
Section(title = "Recommended for You") {
    LazyRow {
        items(recommendations) { bookId ->
            BookCard(book = getBook(bookId))
        }
    }
}
```

### 4.5: Smart Notifications

**Integration Steps:**

1. **Create notification worker**:
```kotlin
class ReadingReminderWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Check if user has read today
        val statsRepository = ReadingStatisticsRepository(/* DAOs */)
        val todayTime = statsRepository.getTotalReadingTimeToday()

        if (todayTime == 0L) {
            // Send notification
            showNotification("Keep your streak going!", "You haven't read today yet")
        }

        return Result.success()
    }
}
```

2. **Schedule daily reminder**:
```kotlin
val workRequest = PeriodicWorkRequestBuilder<ReadingReminderWorker>(
    1, TimeUnit.DAYS
).setInitialDelay(calculateDelayToTargetTime(20, 0), TimeUnit.MILLISECONDS)
    .build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "reading_reminder",
    ExistingPeriodicWorkPolicy.KEEP,
    workRequest
)
```

3. **Add notification channel**:
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
        "reading_reminders",
        "Reading Reminders",
        NotificationManager.IMPORTANCE_DEFAULT
    )
    notificationManager.createNotificationChannel(channel)
}
```

## Database Initialization

Add to MainActivity or Application class:

```kotlin
class ReadAloudApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize database
        val database = AppDatabase.getDatabase(this)

        // Initialize repositories
        RepositoryProvider.initialize(
            highlightDao = database.highlightDao(),
            bookmarkDao = database.bookmarkDao(),
            readingSessionDao = database.readingSessionDao(),
            collectionDao = database.collectionDao(),
            audioBookmarkDao = database.audioBookmarkDao(),
            bookMetadataDao = database.bookMetadataDao(),
            readingGoalDao = database.readingGoalDao()
        )
    }
}
```

## Testing Checklist

- [ ] Highlights can be created and displayed
- [ ] Highlights can be exported to Markdown and CSV
- [ ] Notes can be added to highlights
- [ ] Bookmarks can be created and navigated to
- [ ] Text can be selected and copied
- [ ] Dictionary lookups work for selected words
- [ ] Reading statistics are tracked and displayed
- [ ] Brightness controls work in reader
- [ ] Line spacing and margins can be adjusted
- [ ] Fullscreen mode toggles correctly
- [ ] Search filters work in library
- [ ] Collections can be created and managed
- [ ] Reading status can be updated
- [ ] Library view modes switch correctly
- [ ] Narrator browse view displays books
- [ ] Skip intervals are customizable
- [ ] Audio bookmarks can be created
- [ ] Sleep timer has presets and shake-to-extend
- [ ] Recommendations are displayed
- [ ] Notifications are sent at correct times

## Performance Considerations

1. **Database queries** use indexes - already configured in entities
2. **Flow-based updates** prevent unnecessary recompositions
3. **Lazy loading** for large lists
4. **Background processing** for exports and statistics
5. **Efficient highlighting** in WebView using CSS classes

## Next Steps After Integration

1. Test all features thoroughly
2. Add analytics to track feature usage
3. Implement server sync for all entities
4. Add unit and UI tests
5. Document user-facing features
6. Create onboarding tutorial
7. Optimize performance based on profiling
8. Add accessibility improvements
9. Localize new strings
10. Submit app update

---

All infrastructure is complete. Integration is straightforward with this guide.
