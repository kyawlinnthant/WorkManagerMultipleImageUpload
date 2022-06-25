package com.klt.workmanagertest.data

import com.klt.workmanagertest.BuildConfig
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Di {
    @Singleton
    @Provides
    fun providesOkHttpClient(): OkHttpClient {
       /* return if (BuildConfig.DEBUG) {
            OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(2, TimeUnit.MINUTES)
                .build()
        } else OkHttpClient.Builder().build()*/

        return if (BuildConfig.DEBUG) {
            //this is for logging profiler
            OkHttpClient.Builder()
                .addInterceptor(OkHttpProfilerInterceptor())
                .addNetworkInterceptor(OkHttpProfilerInterceptor())
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

        } else OkHttpClient
            .Builder()
            .build()
    }

    @Singleton
    @Provides
    fun providesRetrofit(
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.100.55:8888/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideService(
        retrofit: Retrofit
    ): UploadService {
        return retrofit.create(UploadService::class.java)
    }

}