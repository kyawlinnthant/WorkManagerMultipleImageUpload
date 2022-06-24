package com.klt.workmanagertest.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object Util {

    fun createMultipartFiles(
        partName: String,
        files: List<File>
    ): List<MultipartBody.Part> {
        /* val requestFile = RequestBody.create(
             "image/jpg".toMediaTypeOrNull(),
             files
         )
         return MultipartBody.Part.createFormData(partName,photo.name,requestFile)*/
        val imagesParts = ArrayList<MultipartBody.Part>(files.size)
        files.forEach {
            val requestBody = it
                .asRequestBody("image/jpg".toMediaTypeOrNull())
            imagesParts.add(
                MultipartBody.Part.createFormData(
                    partName,
                    it.name,
                    requestBody
                )
            )
        }
        return imagesParts
    }
    suspend fun getFiles(
        context: Context,
        uris: List<Uri>
    ): List<File> {
        val files = mutableListOf<Deferred<File>>()
        coroutineScope {
            for (uri in uris) {
                val file = async {
                    convertFile(
                        uri = uri,
                        context = context
                    )
                }
                files.add(file)
            }
        }
        return files.awaitAll()
    }

    fun convertBitmap(
        uri: Uri,
        context: Context
    ): Bitmap {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                context.contentResolver,
                uri
            )
        }
    }

    fun convertFile(
        context: Context,
        uri: Uri
    ): File {
        var file: File? = null
        val tempFile = File.createTempFile(getCurrentTime(), ".png", context.externalCacheDir)
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.let { input ->
            FileOutputStream(tempFile, false).use { output ->
                var read: Int
                val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
                while (input.read(bytes).also { index ->
                        read = index
                    } != -1) {
                    output.write(bytes, 0, read)
                }
                output.close()
            }
            input.close()
            file = tempFile.absoluteFile
        }
        return file!!
    }

    fun getCurrentTime(): String {
        val date = Calendar.getInstance().time
        return date.toFileDate()
    }

    private fun Date.toFileDate(
        format: String = "yyyy-MM-dd-HH:mm:ss:SSSS",
        locale: Locale = Locale.getDefault()
    ): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }
}