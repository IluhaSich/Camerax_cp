package com.example.camerax_cp.viewModels

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CameraViewModel : ViewModel() {

    private var cameraControl: CameraControl? = null
    private var surfaceMeteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    private val preview = Preview.Builder().build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.value = newSurfaceRequest
            surfaceMeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                newSurfaceRequest.resolution.width.toFloat(),
                newSurfaceRequest.resolution.height.toFloat(),
            )
        }
    }
    private val imageCapture = ImageCapture.Builder().build()

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    suspend fun bind(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        selector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        provider.unbindAll()
        val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
        cameraControl = camera.cameraControl

        awaitCancellation()
    }

    fun takePhoto(context: Context) {
        val fileName = "JPEG_${System.currentTimeMillis()}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Camerax_cp-App")
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"

                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }

    fun tapToFocus(tapCoordinates: Offset) {
        val point = surfaceMeteringPointFactory?.createPoint(tapCoordinates.x, tapCoordinates.y)

        point?.let { point ->
            val meteringAction = FocusMeteringAction.Builder(point).build()
            cameraControl?.startFocusAndMetering(meteringAction)
        }
    }

    fun changeZoom(linear: Float) {
        cameraControl?.setLinearZoom(linear)
    }
}
