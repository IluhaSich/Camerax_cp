package com.example.camerax_cp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
        is Resource.Image -> FullImage(item, onDelete = {
            viewModel.delete(context, item.uri)
            selected = null
        })
        is Resource.Video -> FullVideo(item, onDelete = {
            viewModel.delete(context, item.uri)
            selected = null
        })
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
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null)
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
                }
            }
        }
    }
}

@Composable
private fun FullImage(
    resource: Resource.Image,
    onDelete: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = resource.uri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onDelete
        ) {
            Icon(Icons.Default.Delete, null, tint = Color.Red)
        }
    }
}

@Composable
private fun FullVideo(
    resource: Resource.Video,
    onDelete: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "Video playback here",
            modifier = Modifier.align(Alignment.Center),
            color = Color.White
        )
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = onDelete
        ) {
            Icon(Icons.Default.Delete, null, tint = Color.Red)
        }
    }
}
