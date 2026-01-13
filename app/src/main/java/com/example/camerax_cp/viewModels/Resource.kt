package com.example.camerax_cp.viewModels

import android.net.Uri

sealed interface Resource {

    val uri: Uri
    val size: Int
    val name: String
    val date: String

    data class Image(
        override val uri: Uri,
        override val size: Int,
        override val name: String,
        override val date: String,
    ) : Resource

    data class Video(
        override val uri: Uri,
        override val size: Int,
        override val name: String,
        override val date: String,
        val duration: Int,
    ) : Resource
}
