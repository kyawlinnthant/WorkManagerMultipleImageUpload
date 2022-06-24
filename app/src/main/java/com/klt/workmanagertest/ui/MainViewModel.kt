package com.klt.workmanagertest.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(

) : ViewModel() {
    private val _state = mutableStateOf(PhotoState())
    val state: State<PhotoState> get() = _state

    fun addPhotos(photos: List<Bitmap>) {
        _state.value = state.value.copy(
            photos = photos
        )
    }

    fun addFiles(files: List<File>) {
        _state.value = state.value.copy(
            files = files
        )
    }

    fun addUris(uris: List<Uri>) {
        _state.value = state.value.copy(
            uris = uris,
            paths = uris.map { it.toString() }
        )
    }

}