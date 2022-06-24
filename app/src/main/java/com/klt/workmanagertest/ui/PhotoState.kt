package com.klt.workmanagertest.ui

import android.graphics.Bitmap
import android.net.Uri
import java.io.File

data class PhotoState(
    val photos : List<Bitmap> = emptyList(),
    val files : List<File> = emptyList(),
    val uris : List<Uri> = emptyList(),
    val paths : List<String> = listOf()
)
