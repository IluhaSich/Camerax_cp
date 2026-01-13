package com.example.camerax_cp.viewModels

import android.Manifest
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VideoViewModel : ViewModel() {

    private var recording: Recording? = null
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
    private val videoCapture = VideoCapture.withOutput(Recorder.Builder().build())

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    private val _videoRecordEvent = MutableStateFlow<VideoRecordEvent?>(null)
    val videoRecord: StateFlow<VideoRecordEvent?> = _videoRecordEvent.asStateFlow()

    suspend fun bind(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        selector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        provider.unbindAll()
        val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, videoCapture)
        cameraControl = camera.cameraControl

        awaitCancellation()
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun takeVideo(context: Context) {

        val fileName = "Video_${System.currentTimeMillis()}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Movies/Camerax_cp-App")
        }

        recording?.run {
            stop()

            recording = null
            return
        }

        val outputOptions = MediaStoreOutputOptions
            .Builder(
                context.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            )
            .setContentValues(contentValues)
            .build()

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .asPersistentRecording()
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                _videoRecordEvent.value = recordEvent

                if (recordEvent is VideoRecordEvent.Finalize) {
                    if (!recordEvent.hasError()) {
                        val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"

                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        Log.d(TAG, msg)
                    } else {
                        recording?.close()
                        recording = null

                        val msg = "Video capture ends with error: ${recordEvent.error}"

                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        Log.w(TAG, msg)
                    }
                }
            }
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
