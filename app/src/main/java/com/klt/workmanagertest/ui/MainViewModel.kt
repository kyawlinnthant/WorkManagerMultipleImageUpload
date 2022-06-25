package com.klt.workmanagertest.ui

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.klt.workmanagertest.data.UploadService
import com.klt.workmanagertest.data.Util
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val api : UploadService
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

    fun upload() {
        viewModelScope.launch {
            val response = api.uploadMultipleImages(
                files = Util.createMultipartFiles("files", state.value.files)
            )
            if (response.isSuccessful){
                Log.d("klt.upload.success","${response.body()}")
            }else{
                Log.d("klt.upload.fail","${response.body()}")
            }
        }
    }

}