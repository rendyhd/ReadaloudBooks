package com.pekempy.ReadAloudbooks.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.pekempy.ReadAloudbooks.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.pekempy.ReadAloudbooks.data.Book

@Composable
fun MiniPlayerBar(
    audiobookViewModel: AudiobookViewModel,
    readAloudViewModel: ReadAloudAudioViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val audioBook = audiobookViewModel.currentBook
    val isAudioPlaying = audiobookViewModel.isPlaying
    
    val readAloudBook = readAloudViewModel.currentBook
    val isReadAloudPlaying = readAloudViewModel.isPlaying
    
    val activeBook = readAloudBook ?: audioBook ?: return
    val isPlaying = if (readAloudBook != null) isReadAloudPlaying else isAudioPlaying
    val isReadAloud = readAloudBook != null
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    if (currentRoute?.startsWith("player/") == true || currentRoute?.startsWith("reader/") == true) {
        return
    }

    var showStopConfirmation by remember { mutableStateOf(false) }

    if (showStopConfirmation) {
        AlertDialog(
            onDismissRequest = { showStopConfirmation = false },
            title = { Text("Stop Playback") },
            text = { Text("Are you sure you want to stop playback?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (isReadAloud) readAloudViewModel.stopPlayback() else audiobookViewModel.stopPlayback()
                        showStopConfirmation = false
                    }
                ) {
                    Text("Stop")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(72.dp)
            .clickable {
                if (isReadAloud) {
                    navController.navigate("reader/${activeBook.id}?isReadAloud=true&expandPlayer=true")
                } else {
                    navController.navigate("player/${activeBook.id}")
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column {
            val rawProgress = when {
                isReadAloud && readAloudViewModel.duration > 0 ->
                    readAloudViewModel.currentPosition.toFloat() / readAloudViewModel.duration
                !isReadAloud && audiobookViewModel.duration > 0 ->
                    audiobookViewModel.currentPosition.toFloat() / audiobookViewModel.duration
                else -> 0f
            }
            val animatedProgress by animateFloatAsState(
                targetValue = rawProgress,
                animationSpec = tween(durationMillis = 300),
                label = "miniPlayerProgress"
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = activeBook.audiobookCoverUrl ?: activeBook.ebookCoverUrl ?: activeBook.coverUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                val currentChapterTitle by remember {
                    derivedStateOf {
                        if (isReadAloud) {
                            val pos = readAloudViewModel.currentPosition
                            val chapters = readAloudViewModel.chapters
                            android.util.Log.d("MiniPlayer", "ReadAloud: pos=$pos, chapters.size=${chapters.size}")
                            val found = chapters.find { 
                                pos >= it.startOffset && 
                                pos < it.startOffset + it.duration 
                            }
                            android.util.Log.d("MiniPlayer", "ReadAloud: found chapter: ${found?.title} (start=${found?.startOffset}, dur=${found?.duration})")
                            found?.title
                        } else {
                            val pos = audiobookViewModel.currentPosition
                            val chapters = audiobookViewModel.chapters
                            android.util.Log.d("MiniPlayer", "Audiobook: pos=$pos, chapters.size=${chapters.size}")
                            val found = chapters.find { 
                                pos >= it.startOffset && 
                                pos < it.startOffset + it.duration 
                            }
                            android.util.Log.d("MiniPlayer", "Audiobook: found chapter: ${found?.title} (start=${found?.startOffset}, dur=${found?.duration})")
                            found?.title
                        }
                    }
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activeBook.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentChapterTitle ?: activeBook.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (currentChapterTitle != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                val currentSpeed = if (isReadAloud) readAloudViewModel.playbackSpeed else audiobookViewModel.playbackSpeed
                if (currentSpeed != 1.0f) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Text(
                            text = "${"%.1f".format(currentSpeed)}x",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        if (isReadAloud) {
                            if (isPlaying) readAloudViewModel.pause() else readAloudViewModel.play()
                        } else {
                            if (isPlaying) audiobookViewModel.pause() else audiobookViewModel.play()
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { showStopConfirmation = true }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Stop and Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
