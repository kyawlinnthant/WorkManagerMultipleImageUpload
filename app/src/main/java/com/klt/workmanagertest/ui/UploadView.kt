package com.klt.workmanagertest.ui

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.work.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.klt.workmanagertest.data.UploadWorker
import com.klt.workmanagertest.data.Util
import kotlinx.coroutines.*

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UploadView() {
    val context = LocalContext.current
    val vm: MainViewModel = hiltViewModel()
    val photos = vm.state.value.photos
    val photoFiles = vm.state.value.files
    val paths = vm.state.value.paths
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        )
    } else {
        rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    //worker

    val inputData: Data = workDataOf(
        UploadWorker.UPLOAD_URI to paths.toTypedArray()
    )
    val uploadRequest = OneTimeWorkRequestBuilder<UploadWorker>()
        .setInputData(inputData)
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.CONNECTED
                ).build()
        ).build()
    val workManager = WorkManager.getInstance(context)
    val workInfo = workManager
        .getWorkInfosForUniqueWorkLiveData(UploadWorker.WORK_NAME)
        .observeAsState()
        .value
    val uploadInfo = remember(key1 = workInfo) {
        workInfo?.find { it.id == uploadRequest.id }
    }

    DisposableEffect(
        key1 = lifecycleOwner,
    ) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionsState.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val photosLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) {
        scope.launch {
            val bitmaps = getBitmaps(uris = it, context = context)
            Log.d("klt.bitmaps", "${bitmaps.size}")
            val files = Util.getFiles(uris = it, context = context)
            Log.d("klt.files", "${files.size}")

            vm.addPhotos(photos = bitmaps)
            vm.addFiles(files = files)
            vm.addUris(uris = it)

        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.padding(it)) {
            LazyColumn(modifier = Modifier.weight(1f)) {

                item {
                    Text(text = "Total images : ${photos.size}")
                }

                items(photos.size) { index ->
                    val currentItem = photos[index]
                    Image(
                        bitmap = currentItem.asImageBitmap(),
                        contentDescription = "description"
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.size(100.dp),
                    onClick = {
                        if (!permissionsState.allPermissionsGranted) {
                            return@Button
                        }
                        if (photoFiles.isEmpty()) {
                            Log.d("klt.click.empty", "here")
                            photosLauncher.launch("image/*")
                        } else {
                            //todo : send with work manager
                            Log.d("klt.click.isnot", "here")
                            workManager.beginUniqueWork(
                                UploadWorker.WORK_NAME,
                                ExistingWorkPolicy.KEEP,
                                uploadRequest
                            ).enqueue()
                        }

                    },
                    enabled = uploadInfo?.state != WorkInfo.State.RUNNING,
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text(text = if (photoFiles.isEmpty()) "Add" else "Send")
                }
                Spacer(modifier = Modifier.width(50.dp))
                Button(
                    modifier = Modifier.size(100.dp),
                    onClick = {
                        vm.upload()
                    },
                    enabled = uploadInfo?.state != WorkInfo.State.RUNNING,
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Text(text = "Upload")
                }
            }

        }
    }

}

suspend fun getBitmaps(
    uris: List<Uri>,
    context: Context
): List<Bitmap> {

    val bitmaps = mutableListOf<Deferred<Bitmap>>()
    coroutineScope {
        for (uri in uris) {
            val bitmap = async {
                Util.convertBitmap(
                    uri = uri,
                    context = context
                )
            }
            bitmaps.add(bitmap)
        }
    }
    return bitmaps.awaitAll()
}

