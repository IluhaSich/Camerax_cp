package com.example.camerax_cp.ui.screens

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camerax_cp.viewModels.CameraViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onSwitchToVideo: () -> Unit,
    onOpenGallery: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var zoom by remember { mutableStateOf(0f) }
    var flash by remember { mutableStateOf(false) }
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    var focusRequest by remember { mutableStateOf(UUID.randomUUID() to Offset.Unspecified) }


    LaunchedEffect(cameraSelector) {
        viewModel.bind(context.applicationContext, lifecycleOwner, cameraSelector)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        surfaceRequest?.let { request ->
            val coordinateTransformer = remember { MutableCoordinateTransformer() }

            CameraXViewfinder(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapCoordinates ->
                            with(coordinateTransformer) {
                                viewModel.tapToFocus(tapCoordinates.transform())
                            }
                            focusRequest = UUID.randomUUID() to tapCoordinates
                        }
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, gestureZoom, _ ->
                            val newScale = zoom - (1f - gestureZoom)

                            zoom = newScale.coerceIn(0f, 1f)
                            viewModel.changeZoom(zoom)
                        }
                    },
                surfaceRequest = request,
            )
        }

        AnimatedVisibility(
            visible = flash,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }


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

            IconButton(onClick = onSwitchToVideo) {
                Icon(Icons.Default.Videocam, null, tint = Color.White)
            }

            IconButton(
                modifier = Modifier.size(72.dp),
                onClick = {
                    viewModel.takePhoto(context)
                    coroutineScope.launch {
                        flash = true
                        delay(100)
                        flash = false
                    }
                }
            ) {
                Icon(Icons.Default.Camera, null, tint = Color.White)
            }

            IconButton(onClick = onOpenGallery) {
                Icon(Icons.Default.Image, null, tint = Color.White)
            }
        }
    }
}
