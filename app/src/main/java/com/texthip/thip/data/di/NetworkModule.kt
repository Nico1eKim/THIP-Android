package com.texthip.thip.data.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.texthip.thip.BuildConfig
import com.texthip.thip.utils.auth.AuthInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideJson(): Json =
        Json {
            isLenient = true // 유연한 JSON 구분 허용
            prettyPrint = true // 출력 JSON 을 예쁘게 들여쓰기해서 가독성을 높임
            encodeDefaults = true // 파라미터의 기본값(default) 을 JSON 으로 인코딩
            explicitNulls = false // null 값을 명시적으로 표시하지 않음
            ignoreUnknownKeys = true // JSON 에 정의하지 않은 키가 있어도 무시하고 파싱
        }

    @Provides
    @Singleton
    fun providesOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
//        authAuthenticator: TokenAuthenticator
    ): OkHttpClient =
        OkHttpClient.Builder().apply {
            connectTimeout(10, TimeUnit.SECONDS)
            writeTimeout(10, TimeUnit.SECONDS)
            readTimeout(10, TimeUnit.SECONDS)
            addInterceptor(loggingInterceptor)
            addInterceptor(authInterceptor)
//            authenticator(authAuthenticator)
        }.build()

    @Provides
    @Singleton
    fun providesLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun providesRetrofit(
        json: Json,
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(
                json.asConverterFactory(requireNotNull("application/json".toMediaType()))
            )
            .client(okHttpClient)
            .build()
}