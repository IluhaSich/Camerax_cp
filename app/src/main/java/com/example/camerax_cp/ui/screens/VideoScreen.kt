package com.example.camerax_cp.ui.screens

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camerax_cp.viewModels.VideoViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VideoScreen(
    viewModel: VideoViewModel,
    onSwitchToPhoto: () -> Unit,
    onOpenGallery: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    var zoom by remember { mutableStateOf(0f) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var isRecording by remember { mutableStateOf(false) }
    var seconds by remember { mutableStateOf(0) }

    LaunchedEffect(cameraSelector) {
        viewModel.bind(context.applicationContext, lifecycleOwner, cameraSelector)
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            seconds = 0
            while (isRecording) {
                delay(1000)
                seconds++
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        surfaceRequest?.let { request ->
            val transformer = remember { MutableCoordinateTransformer() }

            CameraXViewfinder(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tap ->
                            with(transformer) {
                                viewModel.tapToFocus(tap.transform())
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, gestureZoom, _ ->
                            zoom = (zoom - (1f - gestureZoom)).coerceIn(0f, 1f)
                            viewModel.changeZoom(zoom)
                        }
                    },
                surfaceRequest = request
            )
        }

        Text(
            text = "%02d:%02d".format(seconds / 60, seconds % 60),
            color = if (isRecording) Color.Red else Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 24.dp)
        )

        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            onClick = {
                cameraSelector =
                    if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    else CameraSelector.DEFAULT_BACK_CAMERA
            }
        ) {
            Icon(Icons.Default.Cameraswitch, null, tint = Color.White)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onSwitchToPhoto) {
                Icon(Icons.Default.CameraAlt, null, tint = Color.White)
            }

            IconButton(
                modifier = Modifier.size(72.dp),
                onClick = {
                    scope.launch {
                        viewModel.takeVideo(context)
                        isRecording = !isRecording
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = if (isRecording) Color.Red else Color.White
                )
            }

            IconButton(onClick = onOpenGallery) {
                Icon(Icons.Default.Image, null, tint = Color.White)
            }
        }
    }
}
