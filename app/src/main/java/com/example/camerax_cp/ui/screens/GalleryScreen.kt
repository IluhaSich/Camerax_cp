package com.example.camerax_cp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.example.camerax_cp.viewModels.GalleryViewModel
import com.example.camerax_cp.viewModels.Resource

@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val resources by viewModel.resources.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<Resource?>(null) }

    LaunchedEffect(Unit) {
        viewModel.load(context)
    }

    BackHandler(enabled = selected != null) {
        selected = null
    }

    when (val item = selected) {
        null -> GalleryGrid(resources, onClick = { selected = it }, onBack)
        is Resource.Image -> FullImage(
            resource = item,
            onDelete = {
                viewModel.delete(context, item.uri)
                selected = null
            },
            onBack = { selected = null }
        )
        is Resource.Video -> FullVideo(
            resource = item,
            onDelete = {
                viewModel.delete(context, item.uri)
                selected = null
            },
            onBack = { selected = null }
        )
    }

}

@Composable
private fun GalleryGrid(
    resources: List<Resource>,
    onClick: (Resource) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            IconButton(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                onClick = onBack) {
                Icon(Icons.Default.ArrowBackIosNew, null)
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(128.dp),
            modifier = Modifier.padding(padding)
        ) {
            items(resources, key = { it.uri }) { item ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onClick(item) }
                ) {
                    AsyncImage(
                        model = item.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (item is Resource.Video) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center),
                            tint = Color.White
                        )
                    }
                    Text(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        text = item.date,
                    )
                }
            }
        }
    }
}

@Composable
private fun FullImage(
    resource: Resource.Image,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold { padding ->
        Box(
            Modifier
                .fillMaxSize()
        ) {
            AsyncImage(
                model = resource.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )

            IconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                onClick = onBack
            ) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
            }

            IconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                onClick = onDelete
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}
@OptIn(UnstableApi::class)
@Composable
private fun FullVideo(
    resource: Resource.Video,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    val exoPlayer = remember(resource.uri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(resource.uri))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            }
        )

        IconButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            onClick = onBack
        ) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
        }

        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            onClick = onDelete
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
        }
    }
}
