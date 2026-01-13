package com.example.camerax_cp.viewModels

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class GalleryViewModel : ViewModel() {

    private val _resources = MutableStateFlow<List<Resource>>(emptyList())
    val resources: StateFlow<List<Resource>> = _resources.asStateFlow()

    fun load(context: Context) {
        _resources.value = queryResources(context)
    }

    fun delete(context: Context, uri: Uri) {
        context.contentResolver.delete(uri, null)
//        _resources.value = _resources.value.filterNot { it.uri == uri }
    }

    private fun queryResources(context: Context): List<Resource> {
        val images = context.contentResolver.queryMedia(
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.DISPLAY_NAME,
            ),
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?",
            selectionArgs = arrayOf("%Pictures/CameraX-App/%"),
        ) { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            List(cursor.count) {
                cursor.moveToPosition(it)
                Resource.Image(
                    uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(idCol)
                    ),
                    size = cursor.getInt(sizeCol),
                    name = cursor.getString(nameCol),
                    date = formatDate(cursor.getLong(dateCol))
                )
            }
        }

        val videos = context.contentResolver.queryMedia(
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
            ),
            selection = "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?",
            selectionArgs = arrayOf("%Movies/CameraX-App/%"),
        ) { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            List(cursor.count) {
                cursor.moveToPosition(it)
                Resource.Video(
                    uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(idCol)
                    ),
                    size = cursor.getInt(sizeCol),
                    name = cursor.getString(nameCol),
                    date = formatDate(cursor.getLong(dateCol)),
                    duration = cursor.getInt(durationCol)
                )
            }
        }

        return (images + videos).sortedByDescending { it.date }
    }

    private fun formatDate(epochSeconds: Long): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        return formatter.format(Instant.ofEpochSecond(epochSeconds))
    }

    private fun ContentResolver.queryMedia(
        collection: Uri,
        projection: Array<String>,
        selection: String,
        selectionArgs: Array<String>,
        block: (Cursor) -> List<Resource>
    ): List<Resource> =
        query(collection, projection, selection, selectionArgs, "${MediaStore.MediaColumns.DATE_ADDED} DESC")
            ?.use(block) ?: emptyList()
}
