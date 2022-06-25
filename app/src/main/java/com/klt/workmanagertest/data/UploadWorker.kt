package com.klt.workmanagertest.data

import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.klt.workmanagertest.R
import com.klt.workmanagertest.ui.App
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

@HiltWorker
class UploadWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val api: UploadService,
) : CoroutineWorker(
    appContext = context,
    params = workerParams
) {


    private var photos: Int = 0

    companion object {
        const val UPLOAD_URI = "upload_uri"
        const val WORK_NAME = "upload"
    }

    override suspend fun doWork(): Result {

        val paths = inputData.getStringArray(UPLOAD_URI)
        photos = paths?.size ?: 0
        startForegroundService()

        val files = Util.getFiles(
            context = context,
            uris = paths?.map { it.toUri() } ?: emptyList()
        )
        //todo : calculation
        delay(5000L)

        val response = api.uploadMultipleImages(files = Util.createMultipartFiles("files", files))
        return if (response.isSuccessful) {
            Log.d("klt.success","${response.body()}")
            Result.success()
        } else {
            Log.d("klt.fail","${response.body()}")
            Result.failure()
        }

        /* val response = safeApiCall {
             api.uploadMultipleImages(files = Util.createMultipartFiles("files[]", files))
         }
         when (response) {
             is RemoteResource.ErrorEvent -> {

             }
             is RemoteResource.LoadingEvent -> {

             }
             is RemoteResource.SuccessEvent -> {

             }
         }*/

    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, App.UPLOAD_CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentText("Uploading $photos Images to Moe...")
                    .setContentTitle("Upload in progress")
                    .build()
            )
        )
    }
}