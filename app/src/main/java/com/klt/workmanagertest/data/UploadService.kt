package com.klt.workmanagertest.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {

    @Multipart
    @POST("images/upload")
    suspend fun uploadMultipleImages(
        @Part files: List<MultipartBody.Part>
    ): Response<UploadResponse>

}