package com.example.camerax_cp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.example.camerax_cp.activities.BaseActivity
import com.example.camerax_cp.ui.screens.CameraScreen
import com.example.camerax_cp.ui.screens.GalleryScreen
import com.example.camerax_cp.ui.screens.VideoScreen
import com.example.camerax_cp.ui.theme.Camerax_cpTheme
import com.example.camerax_cp.viewModels.GalleryViewModel
import com.example.camerax_cp.viewModels.CameraViewModel
import com.example.camerax_cp.viewModels.VideoViewModel

class MainActivity : BaseActivity() {

    sealed interface Screen {
        data object Photo : Screen
        data object Video : Screen
        data object Gallery : Screen
    }

    private var isCameraGranted by mutableStateOf(false)

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            isCameraGranted = permissions.values.all { it }
            if (!isCameraGranted) {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (hasPermissions()) {
            isCameraGranted = true
        } else {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        val cameraViewModel: CameraViewModel by viewModels()
        val galleryViewModel: GalleryViewModel by viewModels()
        val videoViewModel: VideoViewModel by viewModels()

        enableEdgeToEdge()
        setContent {
            Camerax_cpTheme {

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Photo) }

                if (!isCameraGranted) return@Camerax_cpTheme

                when (currentScreen) {
                    Screen.Photo -> CameraScreen(
                        viewModel = cameraViewModel,
                        onSwitchToVideo = { currentScreen = Screen.Video },
                        onOpenGallery = { currentScreen = Screen.Gallery }
                    )

                    Screen.Video -> VideoScreen(
                        viewModel = videoViewModel,
                        onSwitchToPhoto = { currentScreen = Screen.Photo },
                        onOpenGallery = { currentScreen = Screen.Gallery }
                    )

                    Screen.Gallery -> GalleryScreen(
                        viewModel = galleryViewModel,
                        onBack = { currentScreen = Screen.Photo }
                    )
                }
            }
        }
    }

    private fun hasPermissions(): Boolean =
        REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}
