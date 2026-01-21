# EXECUTION PLAN: Complete Implementation Integration

**Status:** All infrastructure complete, 0% integrated
**Estimated Time:** 17-25 hours for full implementation
**Critical Path:** Phase 1 must complete before all other work

---

## 🚨 CRITICAL BLOCKERS (MUST DO FIRST)

These tasks block ALL other features. App will not function without them.

### ✅ PHASE 1: FOUNDATION (2-3 hours) - **START HERE**

#### Task 1.1: Database & Repository Initialization (45 min) **[BLOCKING]**
**File:** `app/src/main/java/com/pekempy/ReadAloudbooks/ReadAloudApplication.kt`

```kotlin
class ReadAloudApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize database
        val database = AppDatabase.getDatabase(this)

        // Initialize all repositories
        RepositoryProvider.initialize(
            highlightRepository = HighlightRepository(database.highlightDao()),
            bookmarkRepository = BookmarkRepository(database.bookmarkDao()),
            readingStatisticsRepository = ReadingStatisticsRepository(
                database.readingSessionDao(),
                database.bookMetadataDao()
            ),
            collectionRepository = CollectionRepository(database.collectionDao()),
            audioBookmarkRepository = AudioBookmarkRepository(database.audioBookmarkDao()),
            bookMetadataRepository = BookMetadataRepository(database.bookMetadataDao())
        )
    }
}
```

**New File:** `app/src/main/java/com/pekempy/ReadAloudbooks/data/RepositoryProvider.kt`

```kotlin
object RepositoryProvider {
    lateinit var highlightRepository: HighlightRepository
        private set
    lateinit var bookmarkRepository: BookmarkRepository
        private set
    lateinit var readingStatisticsRepository: ReadingStatisticsRepository
        private set
    lateinit var collectionRepository: CollectionRepository
        private set
    lateinit var audioBookmarkRepository: AudioBookmarkRepository
        private set
    lateinit var bookMetadataRepository: BookMetadataRepository
        private set

    fun initialize(
        highlightRepository: HighlightRepository,
        bookmarkRepository: BookmarkRepository,
        readingStatisticsRepository: ReadingStatisticsRepository,
        collectionRepository: CollectionRepository,
        audioBookmarkRepository: AudioBookmarkRepository,
        bookMetadataRepository: BookMetadataRepository
    ) {
        this.highlightRepository = highlightRepository
        this.bookmarkRepository = bookmarkRepository
        this.readingStatisticsRepository = readingStatisticsRepository
        this.collectionRepository = collectionRepository
        this.audioBookmarkRepository = audioBookmarkRepository
        this.bookMetadataRepository = bookMetadataRepository
    }
}
```

**Testing:**
- [ ] App launches without crash
- [ ] Database file exists in app data directory
- [ ] No "lateinit property not initialized" errors

---

#### Task 1.2: FileProvider Setup for Exports (15 min)
**File:** `app/src/main/AndroidManifest.xml`

Add inside `<application>` tag:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="com.pekempy.ReadAloudbooks.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

**New File:** `app/src/main/res/xml/file_paths.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="cache" path="." />
</paths>
```

**Testing:**
- [ ] Build succeeds
- [ ] No manifest merge errors

---

#### Task 1.3: ViewModel Dependency Injection (90 min) **[COMPLEX]**

**File 1:** `ui/reader/ReaderViewModel.kt` - Add repositories to constructor

```kotlin
class ReaderViewModel(
    private val repository: UserPreferencesRepository,
    private val highlightRepository: HighlightRepository,  // ADD
    private val bookmarkRepository: BookmarkRepository     // ADD
) : ViewModel() {

    // Add highlight methods
    fun addHighlight(
        chapterIndex: Int,
        elementId: String,
        text: String,
        color: String,
        note: String? = null
    ) {
        viewModelScope.launch {
            highlightRepository.addHighlight(
                Highlight(
                    bookId = currentBookId ?: return@launch,
                    chapterIndex = chapterIndex,
                    elementId = elementId,
                    text = text,
                    color = color,
                    note = note
                )
            )
        }
    }

    fun getHighlightsForChapter(chapterIndex: Int): Flow<List<Highlight>> {
        return highlightRepository.getHighlightsForChapter(currentBookId ?: "", chapterIndex)
    }

    // Add bookmark methods
    fun addBookmark(chapterIndex: Int, scrollPercent: Float, label: String?) {
        viewModelScope.launch {
            bookmarkRepository.addBookmark(
                Bookmark(
                    bookId = currentBookId ?: return@launch,
                    chapterIndex = chapterIndex,
                    scrollPercent = scrollPercent,
                    elementId = null,
                    label = label
                )
            )
        }
    }

    fun getBookmarksForBook(): Flow<List<Bookmark>> {
        return bookmarkRepository.getBookmarksForBook(currentBookId ?: "")
    }
}
```

**File 2:** `MainActivity.kt` - Update ReaderViewModel factory

```kotlin
// Find the readerViewModel instantiation and update:
val readerViewModel = viewModel<ReaderViewModel>(
    factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReaderViewModel(
                repository = preferencesRepository,
                highlightRepository = RepositoryProvider.highlightRepository,  // ADD
                bookmarkRepository = RepositoryProvider.bookmarkRepository     // ADD
            ) as T
        }
    }
)
```

**Repeat similar updates for:**
- `AudiobookViewModel` (add audioBookmarkRepository)
- `BookDetailViewModel` (add bookMetadataRepository)
- `LibraryViewModel` (add collectionRepository)

**Testing:**
- [ ] App compiles
- [ ] All ViewModels instantiate without crash
- [ ] Navigation works without errors

---

## 🎯 PHASE 2: HIGH-VALUE QUICK WINS (3-4 hours)

### Task 2.1: Reading Session Tracking (60 min)

**File:** `ui/reader/ReaderViewModel.kt`

```kotlin
private var sessionStartTime: Long? = null
private var sessionId: Long? = null

fun onReaderVisible() {
    sessionStartTime = System.currentTimeMillis()
}

fun onReaderHidden() {
    val startTime = sessionStartTime ?: return
    val bookId = currentBookId ?: return

    viewModelScope.launch {
        readingStatisticsRepository.addSession(
            ReadingSession(
                bookId = bookId,
                bookTitle = epubTitle,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                durationMillis = System.currentTimeMillis() - startTime,
                pagesRead = 0, // Could estimate from progress
                chapterIndex = currentChapterIndex,
                isAudio = false
            )
        )
    }
    sessionStartTime = null
}
```

**File:** `ui/reader/ReaderScreen.kt`

```kotlin
DisposableEffect(Unit) {
    viewModel.onReaderVisible()
    onDispose {
        viewModel.onReaderHidden()
    }
}
```

**Testing:**
- [ ] Reading sessions saved to database
- [ ] Duration calculated correctly

---

### Task 2.2: Add Statistics Screen to Navigation (45 min)

**File:** `MainActivity.kt` - Add composable route

```kotlin
composable("statistics") {
    val statsViewModel = viewModel<ReadingStatisticsViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReadingStatisticsViewModel(
                    RepositoryProvider.readingStatisticsRepository
                ) as T
            }
        }
    )
    ReadingStatisticsScreen(
        viewModel = statsViewModel,
        onBack = { navController.popBackStack() }
    )
}
```

**File:** `ui/settings/SettingsScreen.kt` - Add menu item

```kotlin
// In settings list, add:
SettingsItem(
    title = "Reading Statistics",
    description = "View your reading time, streaks, and progress",
    onClick = { navController.navigate("statistics") }
)
```

**Testing:**
- [ ] Can navigate to statistics screen
- [ ] Statistics display correctly
- [ ] Back button works

---

### Task 2.3: Highlights - Text Selection & Creation (90 min) **[HIGH VALUE]**

**File:** `ui/reader/ReaderScreen.kt`

```kotlin
// Add state
var showHighlightDialog by remember { mutableStateOf(false) }
var selectedText by remember { mutableStateOf("") }
var selectedElementId by remember { mutableStateOf("") }

// Add to ReaderScreen composable
if (showHighlightDialog) {
    HighlightDialog(
        selectedText = selectedText,
        onDismiss = { showHighlightDialog = false },
        onSave = { color, note ->
            viewModel.addHighlight(
                chapterIndex = viewModel.currentChapterIndex,
                elementId = selectedElementId,
                text = selectedText,
                color = color,
                note = note
            )
            showHighlightDialog = false
        }
    )
}
```

**In `wrapHtml()` function - Remove user-select: none:**

```css
/* REMOVE OR COMMENT OUT:
-webkit-user-select: none;
user-select: none;
-webkit-touch-callout: none;
*/
```

**Add JavaScript handler in `addJavascriptInterface`:**

```kotlin
@JavascriptInterface
fun onTextSelected(text: String, elementId: String) {
    // Show highlight dialog
    this@apply.post {
        selectedText = text
        selectedElementId = elementId
        showHighlightDialog = true
    }
}
```

**Add JavaScript in wrapHtml:**

```javascript
document.addEventListener('selectionchange', function() {
    const selection = window.getSelection();
    if (selection.toString().length > 5) {
        const text = selection.toString();
        const range = selection.getRangeAt(0);
        const element = range.commonAncestorContainer.parentElement;
        const elementId = element ? element.id || element.closest('[id]')?.id : '';
        if (elementId && window.Android) {
            window.Android.onTextSelected(text, elementId);
        }
    }
});
```

**Testing:**
- [ ] Can select text
- [ ] Highlight dialog appears
- [ ] Can choose color and add note
- [ ] Highlight saves to database

---

### Task 2.4: Display User Highlights (60 min)

**File:** `ui/reader/ReaderViewModel.kt`

```kotlin
// Add to ViewModel
var userHighlights by mutableStateOf<List<Highlight>>(emptyList())
    private set

fun loadUserHighlights(chapterIndex: Int) {
    viewModelScope.launch {
        getHighlightsForChapter(chapterIndex).collect { highlights ->
            userHighlights = highlights
        }
    }
}
```

**File:** `ui/reader/ReaderScreen.kt` - Inject highlights CSS

```kotlin
// In wrapHtml(), add styles for user highlights
val userHighlightStyles = userHighlights.joinToString("\n") { highlight ->
    """
    #${highlight.elementId} {
        background-color: ${highlight.color}33 !important; /* 20% opacity */
        border-bottom: 2px solid ${highlight.color} !important;
    }
    """
}

// Add to <style> section in wrapHtml
```

**Testing:**
- [ ] Highlights display with correct colors
- [ ] Multiple highlights per chapter work
- [ ] Highlights persist after reload

---

### Task 2.5: Export Highlights (30 min)

**File:** `ui/reader/ReaderScreen.kt`

```kotlin
var showExportMenu by remember { mutableStateOf(false) }
val context = LocalContext.current
val scope = rememberCoroutineScope()

// Add to top bar
IconButton(onClick = { showExportMenu = true }) {
    Text("📤")
}

DropdownMenu(
    expanded = showExportMenu,
    onDismissRequest = { showExportMenu = false }
) {
    DropdownMenuItem(
        text = { Text("Export to Markdown") },
        onClick = {
            scope.launch {
                val exporter = HighlightExporter()
                // Get all highlights for book
                viewModel.getHighlightsForBook().first().let { highlights ->
                    exporter.saveAndShareMarkdown(
                        context = context,
                        book = currentBook,
                        highlights = highlights,
                        chapterTitles = viewModel.getChapterTitles()
                    )
                }
            }
            showExportMenu = false
        }
    )
    DropdownMenuItem(
        text = { Text("Export to CSV") },
        onClick = { /* similar */ }
    )
}
```

**Testing:**
- [ ] Export creates file
- [ ] Share dialog appears
- [ ] Markdown format correct
- [ ] CSV format correct

---

## 📚 PHASE 3: LIBRARY FEATURES (2-3 hours)

### Task 3.1: Reading Status & Ratings (60 min)

**File:** `ui/detail/BookDetailScreen.kt`

```kotlin
// Add to BookDetailScreen
val bookMetadata by viewModel.getBookMetadata(bookId).collectAsState(initial = null)

// Add status selector
ReadingStatusSelector(
    currentStatus = bookMetadata?.readingStatus?.name ?: "NONE",
    onStatusChange = { status ->
        viewModel.updateReadingStatus(bookId, ReadingStatus.valueOf(status))
    }
)

// Add rating stars
RatingStars(
    rating = bookMetadata?.rating,
    onRatingChange = { rating ->
        viewModel.setBookRating(bookId, rating)
    }
)
```

**File:** `ui/detail/BookDetailViewModel.kt`

```kotlin
class BookDetailViewModel(
    // existing params...
    private val bookMetadataRepository: BookMetadataRepository  // ADD
) : ViewModel() {

    fun getBookMetadata(bookId: String) = bookMetadataRepository.getBookMetadata(bookId)

    fun updateReadingStatus(bookId: String, status: ReadingStatus) {
        viewModelScope.launch {
            bookMetadataRepository.updateReadingStatus(bookId, status)
        }
    }

    fun setBookRating(bookId: String, rating: Int) {
        viewModelScope.launch {
            bookMetadataRepository.setBookRating(bookId, rating)
        }
    }
}
```

**Testing:**
- [ ] Can change reading status
- [ ] Status persists
- [ ] Can rate books
- [ ] Rating persists

---

### Task 3.2: Collections Integration (90 min)

**File:** `MainActivity.kt` - Add collections route

```kotlin
composable("collections") {
    val collectionsViewModel = viewModel<CollectionsViewModel>(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CollectionsViewModel(
                    RepositoryProvider.collectionRepository
                ) as T
            }
        }
    )
    CollectionsScreen(
        viewModel = collectionsViewModel,
        onBack = { navController.popBackStack() },
        onCollectionClick = { collection ->
            navController.navigate("collection/${collection.id}")
        }
    )
}
```

**File:** `ui/library/LibraryScreen.kt` - Add Collections tab

```kotlin
// Add to tab row
Tab(
    selected = selectedTab == 5,
    onClick = { navController.navigate("collections") },
    text = { Text("Collections") }
)
```

**File:** `ui/detail/BookDetailScreen.kt` - Add to collection

```kotlin
var showCollectionPicker by remember { mutableStateOf(false) }

// Add menu item
DropdownMenuItem(
    text = { Text("Add to Collection") },
    onClick = { showCollectionPicker = true }
)

if (showCollectionPicker) {
    // Show dialog with collection list
    // Let user select/create collection
    // Call viewModel.addToCollection(collectionId, bookId)
}
```

**Testing:**
- [ ] Can navigate to collections
- [ ] Can create collection
- [ ] Can add books to collection
- [ ] Can view books in collection

---

## 🎵 PHASE 4: AUDIO ENHANCEMENTS (2-3 hours)

### Task 4.1: Advanced Audio Controls (60 min)

**File:** `ui/player/AudiobookPlayerScreen.kt`

```kotlin
var showAudioSettings by remember { mutableStateOf(false) }
val userSettings by preferencesRepository.userSettings.collectAsState(initial = null)

// Add settings button
IconButton(onClick = { showAudioSettings = true }) {
    Text("⚙️")
}

if (showAudioSettings) {
    AdvancedAudioControlsSheet(
        userSettings = userSettings ?: return,
        onSkipBackSecondsChange = { seconds ->
            scope.launch { preferencesRepository.updateSkipBackSeconds(seconds) }
        },
        onSkipForwardSecondsChange = { seconds ->
            scope.launch { preferencesRepository.updateSkipForwardSeconds(seconds) }
        },
        onVolumeBoostChange = { enabled, level ->
            scope.launch { preferencesRepository.updateVolumeBoost(enabled, level) }
            if (enabled) {
                viewModel.setVolumeBoost(level)
            }
        },
        onDismiss = { showAudioSettings = false }
    )
}

// Update skip buttons
IconButton(onClick = {
    viewModel.skipBackward(userSettings.skipBackSeconds * 1000L)
}) {
    Text("↶ ${userSettings.skipBackSeconds}s")
}
```

**File:** `ui/player/AudiobookViewModel.kt`

```kotlin
fun setVolumeBoost(level: Float) {
    player.volume = level
}
```

**Testing:**
- [ ] Can change skip intervals
- [ ] Skip buttons update
- [ ] Volume boost works
- [ ] Settings persist

---

### Task 4.2: Enhanced Sleep Timer with Shake (45 min)

**File:** `ui/player/AudiobookPlayerScreen.kt`

```kotlin
val shakeDetector = remember {
    ShakeDetector(context) {
        // Extend timer by 5 minutes
        viewModel.extendSleepTimer(5)
        Toast.makeText(context, "Timer extended +5 min", Toast.LENGTH_SHORT).show()
    }
}

LaunchedEffect(viewModel.sleepTimerActive) {
    if (viewModel.sleepTimerActive) {
        shakeDetector.start()
    } else {
        shakeDetector.stop()
    }
}

DisposableEffect(Unit) {
    onDispose {
        shakeDetector.stop()
    }
}

// Replace existing sleep timer dialog with enhanced version
if (showSleepTimerDialog) {
    EnhancedSleepTimerDialog(
        currentMinutes = viewModel.sleepTimerMinutes,
        finishChapter = viewModel.finishChapter,
        onSet = { minutes, finishChapter ->
            viewModel.setSleepTimer(minutes, finishChapter)
        },
        onDismiss = { showSleepTimerDialog = false }
    )
}
```

**File:** `ui/player/AudiobookViewModel.kt`

```kotlin
fun extendSleepTimer(additionalMinutes: Int) {
    // Add to existing timer
    sleepTimerMinutes += additionalMinutes
    // Restart countdown
}

fun setSleepTimer(minutes: Int, finishChapter: Boolean) {
    this.sleepTimerMinutes = minutes
    this.finishChapter = finishChapter
    if (minutes > 0) {
        startSleepTimer()
    }
}

private fun startFadeOut() {
    viewModelScope.launch {
        // Fade from 1.0 to 0.0 over 30 seconds
        repeat(30) { step ->
            val volume = 1.0f - (step / 30f)
            player.volume = volume
            delay(1000)
        }
        player.pause()
        player.volume = 1.0f
    }
}
```

**Testing:**
- [ ] Sleep timer presets work
- [ ] Shake extends timer
- [ ] Toast shows on shake
- [ ] Fade-out smooth
- [ ] Finish chapter option works

---

## 📖 PHASE 5: READER ENHANCEMENTS (2 hours)

### Task 5.1: Enhanced Reader Controls (60 min)

**File:** `ui/reader/ReaderScreen.kt`

```kotlin
var showEnhancedControls by remember { mutableStateOf(false) }

// Add to reader controls
Button(onClick = { showEnhancedControls = true }) {
    Text("Advanced")
}

if (showEnhancedControls) {
    EnhancedReaderControlsSheet(
        userSettings = userSettings,
        onBrightnessChange = { brightness ->
            scope.launch { repository.updateReaderBrightness(brightness) }
            // Apply brightness
            val window = (context as? Activity)?.window
            window?.attributes = window?.attributes?.apply {
                screenBrightness = brightness
            }
        },
        onLineSpacingChange = { spacing ->
            scope.launch { repository.updateReaderLineSpacing(spacing) }
        },
        onMarginSizeChange = { size ->
            scope.launch { repository.updateReaderMarginSize(size) }
        },
        onTextAlignmentChange = { alignment ->
            scope.launch { repository.updateReaderTextAlignment(alignment) }
        },
        onFullscreenToggle = { enabled ->
            scope.launch { repository.updateReaderFullscreenMode(enabled) }
        },
        onDismiss = { showEnhancedControls = false }
    )
}
```

**In `wrapHtml()` - Apply settings dynamically:**

```kotlin
fun wrapHtml(..., userSettings: UserSettings): String {
    val marginPadding = when(userSettings.readerMarginSize) {
        0 -> "8px"
        1 -> "16px"
        2 -> "24px"
        else -> "16px"
    }

    return """
    <style>
        body {
            line-height: ${userSettings.readerLineSpacing} !important;
            padding-left: $marginPadding !important;
            padding-right: $marginPadding !important;
            text-align: ${userSettings.readerTextAlignment} !important;
        }
    </style>
    """
}
```

**Testing:**
- [ ] Brightness slider works
- [ ] Line spacing updates
- [ ] Margins change
- [ ] Text alignment changes
- [ ] Settings persist

---

### Task 5.2: Fullscreen Mode (30 min)

**File:** `ui/reader/ReaderScreen.kt`

```kotlin
LaunchedEffect(userSettings.readerFullscreenMode) {
    val window = (context as? Activity)?.window ?: return@LaunchedEffect
    val insetsController = WindowCompat.getInsetsController(window, window.decorView)

    if (userSettings.readerFullscreenMode) {
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}
```

**Testing:**
- [ ] Fullscreen hides status/nav bars
- [ ] Swipe shows bars temporarily
- [ ] Disabling fullscreen restores bars

---

## ✅ TESTING & VALIDATION (3-4 hours)

### Manual Testing Checklist

**Phase 1 - Foundation:**
- [ ] App launches without crash
- [ ] Database initializes
- [ ] All ViewModels work

**Phase 2 - Highlights:**
- [ ] Can select text
- [ ] Can create highlight with color
- [ ] Can add note to highlight
- [ ] Highlights display correctly
- [ ] Can export to Markdown
- [ ] Can export to CSV
- [ ] Share dialog works

**Phase 3 - Bookmarks:**
- [ ] Can create bookmark
- [ ] Can add label to bookmark
- [ ] Bookmarks list displays
- [ ] Can navigate to bookmark
- [ ] Can delete bookmark

**Phase 4 - Statistics:**
- [ ] Reading sessions tracked
- [ ] Statistics screen accessible
- [ ] Time displays correctly
- [ ] Streak calculates correctly
- [ ] Pages counted

**Phase 5 - Collections:**
- [ ] Can create collection
- [ ] Can add books to collection
- [ ] Can view collection
- [ ] Can delete collection
- [ ] Collection colors work

**Phase 6 - Reading Status:**
- [ ] Can set status
- [ ] Status persists
- [ ] Can rate books
- [ ] Ratings display

**Phase 7 - Reader Controls:**
- [ ] Brightness changes
- [ ] Line spacing works
- [ ] Margins adjust
- [ ] Text alignment changes
- [ ] Fullscreen toggles

**Phase 8 - Audio Features:**
- [ ] Skip intervals customizable
- [ ] Volume boost works
- [ ] Sleep timer presets work
- [ ] Shake extends timer
- [ ] Fade-out smooth

**Performance Testing:**
- [ ] 100+ highlights render fast
- [ ] Database queries under 100ms
- [ ] No memory leaks
- [ ] No ANRs
- [ ] Battery usage acceptable

**Edge Cases:**
- [ ] Empty database handles gracefully
- [ ] Network errors don't crash
- [ ] Large books (500+ chapters) work
- [ ] Rapid navigation doesn't crash
- [ ] Orientation changes preserve state

---

## 📊 PROGRESS TRACKING

Use this checklist to track completion:

### Critical Path (Must Complete):
- [ ] Task 1.1 - Database Init
- [ ] Task 1.2 - FileProvider
- [ ] Task 1.3 - ViewModel Deps

### High Priority (User-Facing):
- [ ] Task 2.1 - Session Tracking
- [ ] Task 2.2 - Statistics Navigation
- [ ] Task 2.3 - Highlight Creation
- [ ] Task 2.4 - Display Highlights
- [ ] Task 2.5 - Export Highlights

### Medium Priority (Enhancements):
- [ ] Task 3.1 - Reading Status
- [ ] Task 3.2 - Collections
- [ ] Task 4.1 - Audio Controls
- [ ] Task 4.2 - Sleep Timer
- [ ] Task 5.1 - Reader Controls
- [ ] Task 5.2 - Fullscreen

### Nice to Have (Can Defer):
- [ ] Smart notifications
- [ ] Recommendations
- [ ] Narrator browse
- [ ] Advanced search UI

---

## 🎯 RECOMMENDED EXECUTION SEQUENCE

**Day 1 (4-5 hours):** Foundation & Basics
1. Complete ALL of Phase 1 (Tasks 1.1, 1.2, 1.3)
2. Test: App compiles and runs
3. Complete Task 2.1 (Session Tracking)
4. Complete Task 2.2 (Statistics Navigation)

**Day 2 (4-5 hours):** Highlights (High Value)
1. Complete Task 2.3 (Highlight Creation)
2. Complete Task 2.4 (Display Highlights)
3. Complete Task 2.5 (Export Highlights)
4. Test: Full highlight workflow

**Day 3 (3-4 hours):** Library Features
1. Complete Task 3.1 (Reading Status)
2. Complete Task 3.2 (Collections)
3. Test: Can manage collections and status

**Day 4 (3-4 hours):** Audio & Reader
1. Complete Task 4.1 (Audio Controls)
2. Complete Task 4.2 (Sleep Timer)
3. Complete Task 5.1 (Reader Controls)
4. Complete Task 5.2 (Fullscreen)

**Day 5 (2-3 hours):** Testing & Polish
1. Run full testing checklist
2. Fix bugs
3. Performance optimization
4. Final validation

---

## 🚨 KNOWN RISKS & MITIGATION

| Risk | Impact | Mitigation |
|------|--------|------------|
| Database migration will lose data | HIGH | Add proper migrations before production |
| WebView highlights may be slow | MEDIUM | Implement virtual scrolling, limit visible highlights |
| Shake detector drains battery | MEDIUM | Only active during sleep timer |
| Memory leaks in ViewModels | MEDIUM | Test with LeakCanary, proper cleanup in onCleared |
| Text selection conflicts with pagination | HIGH | Careful JavaScript event handling |

---

## 📈 SUCCESS METRICS

**Minimum Viable Product:**
- All Phase 1 tasks complete
- Highlights working (Tasks 2.3-2.5)
- Statistics tracking (Tasks 2.1-2.2)
- No crashes or data loss

**Full Feature Complete:**
- All tasks through Phase 5 complete
- All 20 features functional
- Performance acceptable
- Ready for user testing

**Production Ready:**
- All tests passing
- Database migrations implemented
- Performance optimized
- Edge cases handled
- User documentation complete

---

**NEXT STEP:** Start with Task 1.1 - Database Initialization. Everything else depends on this.
