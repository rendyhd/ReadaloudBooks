package com.pekempy.ReadAloudbooks.util

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.pekempy.ReadAloudbooks.data.local.AppDatabase
import com.pekempy.ReadAloudbooks.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val highlights: List<Highlight> = emptyList(),
    val bookmarks: List<Bookmark> = emptyList(),
    val audioBookmarks: List<AudioBookmark> = emptyList(),
    val readingSessions: List<ReadingSession> = emptyList(),
    val readingGoals: List<ReadingGoal> = emptyList()
)

object BackupManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackup(context: Context, outputUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getDatabase(context)

            val highlights = database.highlightDao().getAllHighlights().first()
            val bookmarks = database.bookmarkDao().getAllBookmarks().first()
            val audioBookmarks = database.audioBookmarkDao().getAllAudioBookmarks().first()
            val readingSessions = database.readingSessionDao().getSessionsSince(0L)
            val readingGoals = database.readingGoalDao().getAllGoals().first()

            val backup = BackupData(
                highlights = highlights,
                bookmarks = bookmarks,
                audioBookmarks = audioBookmarks,
                readingSessions = readingSessions,
                readingGoals = readingGoals
            )

            val json = gson.toJson(backup)
            val totalItems = highlights.size + bookmarks.size + audioBookmarks.size +
                    readingSessions.size + readingGoals.size

            context.contentResolver.openOutputStream(outputUri)?.use { output ->
                output.write(json.toByteArray(Charsets.UTF_8))
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            android.util.Log.i("BackupManager", "Backup created: $totalItems items")
            Result.success(totalItems)
        } catch (e: Exception) {
            android.util.Log.e("BackupManager", "Backup failed", e)
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(context: Context, inputUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = context.contentResolver.openInputStream(inputUri)?.use { input ->
                input.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("Could not open input stream"))

            val backup = gson.fromJson(json, BackupData::class.java)
                ?: return@withContext Result.failure(Exception("Invalid backup file"))

            val database = AppDatabase.getDatabase(context)
            var restoredCount = 0

            // Restore highlights (skip duplicates by inserting with REPLACE)
            backup.highlights.forEach { highlight ->
                database.highlightDao().insertHighlight(highlight.copy(id = 0))
                restoredCount++
            }

            // Restore bookmarks
            backup.bookmarks.forEach { bookmark ->
                database.bookmarkDao().insertBookmark(bookmark.copy(id = 0))
                restoredCount++
            }

            // Restore audio bookmarks
            backup.audioBookmarks.forEach { audioBookmark ->
                database.audioBookmarkDao().insertAudioBookmark(audioBookmark.copy(id = 0))
                restoredCount++
            }

            // Restore reading sessions
            backup.readingSessions.forEach { session ->
                database.readingSessionDao().insertSession(session.copy(id = 0))
                restoredCount++
            }

            // Restore reading goals
            backup.readingGoals.forEach { goal ->
                database.readingGoalDao().insertGoal(goal.copy(id = 0))
                restoredCount++
            }

            android.util.Log.i("BackupManager", "Restore completed: $restoredCount items")
            Result.success(restoredCount)
        } catch (e: Exception) {
            android.util.Log.e("BackupManager", "Restore failed", e)
            Result.failure(e)
        }
    }
}
